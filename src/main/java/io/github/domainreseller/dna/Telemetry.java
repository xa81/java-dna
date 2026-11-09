package io.github.domainreseller.dna;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of php-dna's Sentry telemetry ({@code SharedApiConfigAndUtilsTrait}).
 *
 * <p>Errors and sampled successful calls are posted to Sentry's store endpoint
 * in the background. Nothing here ever blocks or throws into the caller. Only
 * credentials are masked; URL, operation, domain and contacts are sent, as in
 * php-dna. With an empty DSN everything is a no-op.</p>
 */
final class Telemetry {

    // Filled in when the Sentry project for DomainNameAPI-Java exists.
    static final String DEFAULT_SENTRY_DSN = "";

    static final String APPLICATION = "DomainNameAPI-Java";

    /** Successful calls sampled per 1000 (4%). Calls slower than 1 s are always sent. */
    static final int PERFORMANCE_SAMPLE_RATE = 40;

    static final List<String> DEFAULT_IGNORED_ERRORS = Collections.unmodifiableList(Arrays.asList(
        "Domain not found",
        "ERR_DOMAIN_NOT_FOUND",
        "Reseller not found",
        "Domain is not in updateable status",
        "balance is not sufficient",
        "Price definition not found",
        "TLD is not supported",
        "Invalid API credentials",
        "could not be found",
        "Insufficent reseller balance",
        "Redirect information not found"
    ));

    /** Where events go. Replaceable in tests. */
    interface Sender {
        void post(String url, String json, String authHeader);
    }

    private static final int CACHE_TTL_SECONDS = 512;
    private static volatile String cachedIp;
    private static volatile long cachedIpAt;

    private final SecureRandom random = new SecureRandom();
    private volatile boolean enabled = true;
    private volatile boolean minimal = false;
    private volatile String dsn = DEFAULT_SENTRY_DSN;
    private volatile Sender sender = new HttpSender();
    private volatile java.util.function.Supplier<String> ipResolver = Telemetry::serverIp;
    private String sessionTraceId;

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    boolean isEnabled() {
        return enabled;
    }

    void setMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    boolean isMinimal() {
        return minimal;
    }

    void setDsn(String dsn) {
        this.dsn = dsn == null ? "" : dsn.trim();
    }

    String getDsn() {
        return dsn;
    }

    void setSender(Sender sender) {
        this.sender = sender;
    }

    void setIpResolver(java.util.function.Supplier<String> ipResolver) {
        this.ipResolver = ipResolver;
    }

    // ------------------------------------------------------------------ DSN

    /** Parsed DSN: https://publicKey[:secret]@host/projectId. */
    static final class Dsn {
        final String host;
        final String projectId;
        final String publicKey;
        final String secretKey;

        Dsn(String host, String projectId, String publicKey, String secretKey) {
            this.host = host;
            this.projectId = projectId;
            this.publicKey = publicKey;
            this.secretKey = secretKey;
        }

        String storeUrl() {
            return "https://" + host + "/api/" + projectId + "/store/";
        }
    }

    static Dsn parseDsn(String dsn) {
        if (dsn == null || dsn.trim().isEmpty()) {
            return null;
        }
        try {
            URI u = URI.create(dsn.trim());
            String host = u.getHost();
            String path = u.getPath() == null ? "" : Php.ltrim(u.getPath(), "/");
            String userInfo = u.getRawUserInfo();
            if (host == null || path.isEmpty() || userInfo == null || userInfo.isEmpty()) {
                return null;
            }
            String pub = userInfo;
            String secret = null;
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                pub = userInfo.substring(0, colon);
                secret = userInfo.substring(colon + 1);
                if (secret.isEmpty()) {
                    secret = null;
                }
            }
            if (pub.isEmpty()) {
                return null;
            }
            return new Dsn(host, path, pub, secret);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ masking

    private static final Pattern SENSITIVE_KEY =
        Pattern.compile("auth|epp|password|passwd|secret|token|api[_-]?key", Pattern.CASE_INSENSITIVE);

    /**
     * Mask credentials before reporting: auth/EPP codes, API keys, passwords,
     * secrets and tokens become {@code ***}. Everything else is kept.
     */
    static Object mask(Object data) {
        return mask(data, 0);
    }

    private static Object mask(Object data, int depth) {
        if (depth > 8) {
            return "[…]";
        }
        if (data instanceof Map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) data).entrySet()) {
                String k = String.valueOf(e.getKey());
                Object v = e.getValue();
                if (SENSITIVE_KEY.matcher(k).find()) {
                    out.put(k, (v == null || "".equals(v)) ? v : "***");
                } else {
                    out.put(k, mask(v, depth + 1));
                }
            }
            return out;
        }
        if (data instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object o : (List<?>) data) {
                out.add(mask(o, depth + 1));
            }
            return out;
        }
        return data;
    }

    // ------------------------------------------------------------------ classification

    static boolean isIgnored(String message) {
        String m = message == null ? "" : message;
        for (String ev : DEFAULT_IGNORED_ERRORS) {
            if (m.contains(ev)) {
                return true;
            }
        }
        return false;
    }

    /** Tags shared by error and transaction events. */
    Map<String, Object> commonTags(DNARest ctx, String message, Integer code) {
        Map<String, Object> tags = new LinkedHashMap<>();
        if (ctx.resellerId != null && !ctx.resellerId.isEmpty()) {
            tags.put("reseller", ctx.resellerId);
        }
        Object domain = Php.get(ctx.lastRequest, "payload", "domainName");
        if (domain instanceof String && !((String) domain).isEmpty() && !minimal) {
            tags.put("domain", domain);
        }
        if (ctx.lastFunction != null && !ctx.lastFunction.isEmpty()) {
            tags.put("operation", ctx.lastFunction);
        }
        tags.put("api_type", "REST");
        if (message != null) {
            Matcher m = Pattern.compile("API_(-?\\d+)_ERROR").matcher(message);
            if (m.find()) {
                tags.put("api_code", "API_" + m.group(1));
            } else if (code != null && code != 0) {
                tags.put("api_code", "API_" + code);
            }
            if (code != null && code != 0) {
                tags.put("status_code", String.valueOf(code));
            }
        }
        return tags;
    }

    /** Normalised title, tags and fingerprint for an error. */
    static final class Classification {
        final String value;
        final Map<String, Object> tags;
        final List<String> fingerprint;

        Classification(String value, Map<String, Object> tags, List<String> fingerprint) {
            this.value = value;
            this.tags = tags;
            this.fingerprint = fingerprint;
        }
    }

    Classification classify(DNARest ctx, String message, int code) {
        String msg = message == null ? "" : message;
        Map<String, Object> tags = commonTags(ctx, msg, code);
        Matcher ip = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,3}){3})\\b").matcher(msg);
        if (ip.find()) {
            tags.put("ip_value", ip.group(1));
        }
        String apiCode = (String) tags.get("api_code");
        String n = msg;
        n = n.replaceFirst("(?i)^\\[API_ERROR\\]:\\s*", "");
        n = n.replaceAll("(?i)API_-?\\d+_ERROR\\s*-\\s*Failed\\s*-\\s*", "");
        n = n.replaceAll("(?i)\\s*\\(Reseller Id[^)]*\\)\\s*", "");
        n = n.replaceAll("\\b\\d{1,3}(?:\\.\\d{1,3}){3}\\b", "{ip}");
        n = n.replaceAll("'[^']{0,80}'|\"[^\"]{0,80}\"", "{v}");
        n = n.replaceAll("\\b\\d{2,}\\b", "{n}");
        n = n.replaceAll("\\s+", " ").trim();
        if (n.isEmpty()) {
            n = apiCode != null ? apiCode : "ApiException";
        } else if (apiCode != null && !n.startsWith(apiCode)) {
            n = apiCode + ": " + n;
        }
        if (n.length() > 160) {
            n = n.substring(0, 157) + "...";
        }
        Object op = tags.get("operation");
        List<String> fingerprint = Arrays.asList("dna", APPLICATION, op == null ? "unknown" : op.toString(), n);
        return new Classification(n, tags, fingerprint);
    }

    /** OTel-compatible trace status. */
    static String mapTraceStatus(boolean success, String apiCode) {
        if (success) {
            return "ok";
        }
        if (apiCode == null) {
            return "unknown";
        }
        Matcher m = Pattern.compile("-?\\d+").matcher(apiCode);
        if (!m.find()) {
            return "unknown";
        }
        long code;
        try {
            code = Long.parseLong(m.group());
        } catch (NumberFormatException e) {
            return "unknown";
        }
        if (code == 401 || code == 2202) return "unauthenticated";
        if (code == 403) return "permission_denied";
        if (code == 404 || code == 510) return "not_found";
        if (code == 408 || code == 504) return "deadline_exceeded";
        if (code == -1) return "unavailable";
        if (code >= 500 && code < 600) return "internal_error";
        if (code >= 400 && code < 500) return "invalid_argument";
        return "unknown";
    }

    static String environment(String serviceUrl) {
        return serviceUrl != null && serviceUrl.toLowerCase(Locale.ROOT).contains("ote.") ? "ote" : "production";
    }

    // ------------------------------------------------------------------ events

    /**
     * Report an error. Never throws.
     *
     * @param ctx     client state (request, response, operation)
     * @param message error message
     * @param code    HTTP status, curl error number or 0
     */
    void sendError(DNARest ctx, String message, int code) {
        try {
            Map<String, Object> event = buildErrorEvent(ctx, message, code, new Throwable().getStackTrace());
            if (event == null) {
                return;
            }
            dispatch(event, "sentry_client=java-dna/" + DomainNameApi.VERSION);
        } catch (Throwable ignored) {
            // A telemetry failure must never surface in the host application.
        }
    }

    /** Builds the Sentry error event, or {@code null} when nothing should be sent. */
    Map<String, Object> buildErrorEvent(DNARest ctx, String message, int code, StackTraceElement[] trace) {
        if (!enabled || isIgnored(message)) {
            return null;
        }
        if (parseDsn(dsn) == null) {
            return null;
        }
        double elapsed = (System.nanoTime() - ctx.startAtNanos) / 1e9;
        Classification c = classify(ctx, message, code);

        List<Object> frames = new ArrayList<>();
        for (int i = trace.length - 1; i >= 0; i--) {
            StackTraceElement f = trace[i];
            frames.add(Php.map(
                "filename", f.getFileName() == null ? "" : f.getFileName(),
                "lineno", f.getLineNumber() < 0 ? 0 : f.getLineNumber(),
                "function", f.getClassName() + "::" + f.getMethodName()));
        }

        Map<String, Object> tags = Php.map(
            "handled", "yes",
            "level", "error",
            "url", "NA",
            "transaction", "NA",
            "trace_id", hex(8),
            "runtime_name", "Java",
            "runtime_version", System.getProperty("java.version", ""),
            "elapsed_time", Php.numberFormat(elapsed, 4),
            "application", APPLICATION,
            "library_version", DomainNameApi.VERSION,
            "ip_address", minimal ? "" : "{ip}",
            "vhost_user", System.getProperty("user.name", ""));
        tags.putAll(c.tags);

        Map<String, Object> extra = Php.map(
            "request_data", mask(ctx.lastRequest),
            "response_data", mask(ctx.lastResponse),
            "java_vendor", System.getProperty("java.vendor", ""),
            "os", System.getProperty("os.name", "") + " " + System.getProperty("os.version", ""));
        extra.put("original_message", message);

        Map<String, Object> event = Php.map(
            "event_id", hex(16),
            "timestamp", DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC).format(Instant.now()),
            "level", "error",
            "logger", "java",
            "platform", "java",
            "culprit", DomainNameApi.class.getName() + " via DNARest.java",
            "message", Php.map("formatted", c.value),
            "exception", Php.map("values", Php.list(Php.map(
                "type", APPLICATION + " Exception",
                "value", c.value,
                "stacktrace", Php.map("frames", frames)))),
            "tags", tags,
            "extra", extra,
            "release", DomainNameApi.VERSION,
            "environment", environment(ctx.serviceUrl),
            "fingerprint", c.fingerprint);

        if (minimal) {
            tags.remove("url");
            tags.remove("ip_address");
            tags.remove("vhost_user");
            tags.remove("ip_value");
            Object url = Php.get(ctx.lastRequest, "url");
            String endpoint = "";
            try {
                String p = URI.create(Php.str(url)).getPath();
                endpoint = p == null ? "" : p;
            } catch (IllegalArgumentException ignored) {
                // keep empty
            }
            extra.put("request_data", Php.map(
                "method", Php.str(Php.get(ctx.lastRequest, "method")),
                "endpoint", endpoint));
            extra.put("response_data", Php.map(
                "http_code", Php.intValue(Php.toInt(Php.get(ctx.lastResponseHeaders, "http_code")))));
        }
        return event;
    }

    /**
     * Report a successful call for performance metrics. Never throws.
     *
     * @param ctx        client state
     * @param durationMs call duration in milliseconds
     * @param startMs    wall-clock start (epoch ms)
     * @param endMs      wall-clock end (epoch ms)
     */
    void sendPerformance(DNARest ctx, double durationMs, long startMs, long endMs) {
        try {
            if (!enabled || parseDsn(dsn) == null) {
                return;
            }
            if (!(durationMs > 1000 || ThreadLocalRandom.current().nextInt(1, 1001) <= PERFORMANCE_SAMPLE_RATE)) {
                return;
            }
            Map<String, Object> event = buildPerformanceEvent(ctx, durationMs, startMs, endMs);
            dispatch(event, "sentry_client=java-api/" + DomainNameApi.VERSION);
        } catch (Throwable ignored) {
            // Fail silently.
        }
    }

    Map<String, Object> buildPerformanceEvent(DNARest ctx, double durationMs, long startMs, long endMs) {
        String traceId = traceId();
        String rootSpan = hex(8);
        String childSpan = hex(8);
        String op = "http.client";

        String apiCode = null;
        Object parsedCode = Php.get(ctx.lastParsedResponse, "Code");
        Object respCode = Php.get(ctx.lastResponse, "error", "code");
        if (parsedCode instanceof String) {
            apiCode = (String) parsedCode;
        } else if (respCode instanceof String) {
            apiCode = (String) respCode;
        }
        String status = mapTraceStatus(true, apiCode);

        Map<String, Object> common = commonTags(ctx, null, null);
        if (apiCode != null && !common.containsKey("api_code")) {
            common.put("api_code", apiCode);
        }
        long httpCode = Php.toInt(Php.get(ctx.lastResponseHeaders, "http_code"));
        if (httpCode != 0) {
            common.put("status_code", String.valueOf(httpCode));
        }

        String method = Php.str(Php.coalesce(Php.get(ctx.lastRequest, "method"), "GET"));
        String url = Php.str(Php.get(ctx.lastRequest, "url"));
        String host = "";
        try {
            String h = URI.create(url.isEmpty() ? ctx.serviceUrl : url).getHost();
            host = h == null ? "" : h;
        } catch (IllegalArgumentException ignored) {
            // keep empty
        }
        Map<String, Object> spanData = Php.map("http.request.method", method);
        if (!host.isEmpty()) {
            spanData.put("server.address", host);
        }
        if (!url.isEmpty() && !minimal) {
            int q = url.indexOf('?');
            spanData.put("url.full", q >= 0 ? url.substring(0, q) : url);
        }
        if (common.containsKey("status_code")) {
            spanData.put("http.response.status_code", Php.intValue(httpCode));
        }
        String description = !host.isEmpty() ? method + " https://" + host : ctx.lastFunction;

        Map<String, Object> tags = Php.map(
            "application", APPLICATION,
            "library_version", DomainNameApi.VERSION,
            "ip_address", minimal ? "" : "{ip}",
            "vhost_user", minimal ? "" : System.getProperty("user.name", ""));
        tags.putAll(common);

        String start = isoMillis(startMs);
        String end = isoMillis(endMs);
        return Php.map(
            "event_id", hex(16),
            "platform", "java",
            "level", "info",
            "type", "transaction",
            "transaction", "API." + ctx.lastFunction,
            "transaction_info", Php.map("source", "custom"),
            "release", DomainNameApi.VERSION,
            "environment", environment(ctx.serviceUrl),
            "timestamp", end,
            "start_timestamp", start,
            "contexts", Php.map(
                "trace", Php.map("trace_id", traceId, "span_id", rootSpan, "op", op, "status", status),
                "runtime", Php.map("name", "java", "version", System.getProperty("java.version", ""))),
            "tags", tags,
            "measurements", Php.map(
                "duration", Php.map("value", durationMs, "unit", "millisecond"),
                "memory", Php.map("value", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), "unit", "byte")),
            "spans", Php.list(Php.map(
                "span_id", childSpan,
                "parent_span_id", rootSpan,
                "trace_id", traceId,
                "op", op,
                "description", description,
                "status", status,
                "data", spanData,
                "start_timestamp", start,
                "timestamp", end)));
    }

    // ------------------------------------------------------------------ transport

    /**
     * Fill the server IP placeholder and post in the background. The IP lookup
     * (api.ipify.org, cached) also runs off the caller's thread.
     */
    private void dispatch(Map<String, Object> event, String client) {
        Dsn d = parseDsn(dsn);
        if (d == null) {
            return;
        }
        StringBuilder auth = new StringBuilder("Sentry sentry_version=7, ")
            .append(client).append(", sentry_key=").append(d.publicKey);
        if (d.secretKey != null) {
            auth.append(", sentry_secret=").append(d.secretKey);
        }
        String authHeader = auth.toString();
        String url = d.storeUrl();
        Sender s = sender;
        java.util.function.Supplier<String> ip = ipResolver;
        Thread t = new Thread(() -> {
            try {
                Object tags = event.get("tags");
                if (tags instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tm = (Map<String, Object>) tags;
                    if ("{ip}".equals(tm.get("ip_address"))) {
                        tm.put("ip_address", ip.get());
                    }
                }
                s.post(url, Json.encode(event), authHeader);
            } catch (Throwable ignored) {
                // never surface
            }
        }, "dna-telemetry");
        t.setDaemon(true);
        t.start();
    }

    private static String serverIp() {
        long now = System.currentTimeMillis() / 1000;
        String ip = cachedIp;
        if (ip != null && now - cachedIpAt < CACHE_TTL_SECONDS) {
            return ip;
        }
        try {
            HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
            HttpResponse<String> r = c.send(HttpRequest.newBuilder(URI.create("https://api.ipify.org"))
                .timeout(Duration.ofSeconds(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
            String body = r.body() == null ? "" : r.body().trim();
            if (r.statusCode() == 200 && body.matches("[0-9a-fA-F:.]{3,45}")) {
                cachedIp = body;
                cachedIpAt = now;
                return body;
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return "unknown";
    }

    /** Posts with a short timeout and ignores the result. */
    static final class HttpSender implements Sender {
        private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500)).build();

        @Override
        public void post(String url, String json, String authHeader) {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .header("X-Sentry-Auth", authHeader)
                .header("User-Agent", DomainNameApi.USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
            CLIENT.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        }
    }

    // ------------------------------------------------------------------ helpers

    private synchronized String traceId() {
        if (sessionTraceId == null) {
            sessionTraceId = hex(16);
        }
        return sessionTraceId;
    }

    private String hex(int bytes) {
        byte[] b = new byte[bytes];
        random.nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    private static String isoMillis(long epochMs) {
        return DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(epochMs));
    }
}
