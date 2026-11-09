package io.github.domainreseller.dna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static io.github.domainreseller.dna.Php.coalesce;
import static io.github.domainreseller.dna.Php.empty;
import static io.github.domainreseller.dna.Php.get;
import static io.github.domainreseller.dna.Php.list;
import static io.github.domainreseller.dna.Php.map;
import static io.github.domainreseller.dna.Php.str;
import static io.github.domainreseller.dna.Php.truthy;

/**
 * REST engine: a line-by-line port of php-dna's {@code DNARest}. Parameters are
 * loosely typed ({@code Object}) on purpose so PHP's coercions apply exactly as
 * in php-dna; {@link DomainNameApi} is the typed public facade over it.
 */
final class DNARest {

    static final String URL_PROD = "https://api.domainresellerapi.com/api/v1";
    static final String URL_OTE = "https://ote.domainresellerapi.com/api/v1";

    static final String RESULT_OK = "OK";
    static final String RESULT_ERROR = "ERROR";

    static final int DEFAULT_TIMEOUT = 30;
    static final int DEFAULT_CONNECT_TIMEOUT = 10;
    static final String DEFAULT_REASON = "Owner request";

    static final List<String> DEFAULT_NAMESERVERS =
        Collections.unmodifiableList(Arrays.asList("ns1.domainnameapi.com", "ns2.domainnameapi.com"));

    static final List<String> ZONE_RECORD_TYPES = Collections.unmodifiableList(Arrays.asList(
        "A", "AAAA", "ALIAS", "CAA", "CNAME", "DS", "MX", "NAPTR", "PTR", "SRV", "TLSA", "TXT"));

    static final List<String> FORWARD_TYPES = Collections.unmodifiableList(Arrays.asList("Standard", "Frame"));

    private static final Map<Integer, String> DOMAIN_STATUS_MAP = new LinkedHashMap<>();

    static {
        DOMAIN_STATUS_MAP.put(0, "Active");
        DOMAIN_STATUS_MAP.put(1, "WaitingForRegistration");
        DOMAIN_STATUS_MAP.put(2, "WaitingForDocument");
        DOMAIN_STATUS_MAP.put(3, "WaitingForIncomingTransfer");
        DOMAIN_STATUS_MAP.put(4, "TransferredOut");
        DOMAIN_STATUS_MAP.put(7, "PendingDelete");
        DOMAIN_STATUS_MAP.put(8, "Deleted");
        DOMAIN_STATUS_MAP.put(9, "ConfirmationEmailSend");
        DOMAIN_STATUS_MAP.put(11, "WaitingForOutgoingTransfer");
        DOMAIN_STATUS_MAP.put(12, "PendingHold");
        DOMAIN_STATUS_MAP.put(15, "ModificationPending");
        DOMAIN_STATUS_MAP.put(18, "InCase");
        DOMAIN_STATUS_MAP.put(19, "PendingRestore");
    }

    /** php-dna $DEFAULT_ERRORS: code => [message, description]. */
    static final Map<String, String[]> DEFAULT_ERRORS = new LinkedHashMap<>();

    static {
        DEFAULT_ERRORS.put("DOMAIN_DETAILS", new String[]{"Invalid domain details! Details format is not valid",
            "The provided domain details are not in the expected format"});
        DEFAULT_ERRORS.put("CREDENTIALS", new String[]{"Invalid reseller ID or API key",
            "The provided API credentials are invalid"});
        DEFAULT_ERRORS.put("DOMAIN_LIST", new String[]{"The domain list is invalid or contains multiple entries!",
            "The domain list response format is incorrect or unexpected"});
        DEFAULT_ERRORS.put("TLD_LIST", new String[]{"TLD info is not a valid array or more than one TLD info has returned!",
            "The TLD list response is not in the expected format"});
        DEFAULT_ERRORS.put("RESPONSE", new String[]{"Invalid response received from server! Response is empty.",
            "The API response is empty or null"});
        DEFAULT_ERRORS.put("RESPONSE_FORMAT", new String[]{"Invalid response received from server! Response format is not valid.",
            "The API response format is not in the expected structure"});
        DEFAULT_ERRORS.put("RESPONSE_COUNT", new String[]{"Invalid parameters passed to function! Response data contains more than one result!",
            "The API response contains multiple results when only one was expected"});
        DEFAULT_ERRORS.put("RESPONSE_CODE", new String[]{"Invalid parameters passed to function! Operation result or Error code not received from server",
            "The API response is missing required operation result or error code fields"});
        DEFAULT_ERRORS.put("RESPONSE_REST", new String[]{"Invalid parameters passed to function! REST return is not a valid array!",
            "The REST response is not in a valid array format"});
        DEFAULT_ERRORS.put("CONTACT_INFO", new String[]{"Invalid response received from server! Contact info is not a valid array or more than one contact info has returned!",
            "The contact information response is not in the expected format"});
        DEFAULT_ERRORS.put("CONTACT_SAVE", new String[]{"Invalid response received from server! Contact info could not be saved!",
            "The contact information could not be saved on the server"});
        DEFAULT_ERRORS.put("DOMAIN_TRANSFER_REQUEST", new String[]{"Invalid response received from server! Domain transfer request could not be completed!",
            "The domain transfer request failed to complete"});
        DEFAULT_ERRORS.put("DOMAIN_RENEW", new String[]{"Invalid response received from server! Domain renew request could not be completed!",
            "The domain renewal request failed to complete"});
        DEFAULT_ERRORS.put("DOMAIN_REGISTER", new String[]{"Invalid response received from server! Domain register request could not be completed!",
            "The domain registration request failed to complete"});
        DEFAULT_ERRORS.put("DOMAIN_SYNC", new String[]{"Invalid response received from server! Domain sync request could not be completed!",
            "The domain synchronization request failed to complete"});
    }

    // ---------------------------------------------------------------- state

    String serviceUrl = URL_PROD;
    final String resellerId;
    private final String token;
    boolean sslVerify = true;

    Map<String, Object> lastRequest = new LinkedHashMap<>();
    Object lastResponse = new LinkedHashMap<String, Object>();
    Map<String, Object> lastResponseHeaders = new LinkedHashMap<>();
    Object lastParsedResponse = new LinkedHashMap<String, Object>();
    String lastFunction = "";
    long startAtNanos = System.nanoTime();

    HttpTransport transport = new JdkHttpTransport();
    final Telemetry telemetry = new Telemetry();

    private double zoneMinInterval = 1.0;
    private long lastZoneRequestAtNanos = 0L;

    /** A failed gateway call, carrying the HTTP status (or curl error number) as code. */
    static final class ApiException extends Exception {
        private static final long serialVersionUID = 1L;
        final int code;

        ApiException(String message, int code) {
            super(message);
            this.code = code;
        }
    }

    DNARest(String resellerId, String token, boolean testMode) {
        if (testMode) {
            serviceUrl = URL_OTE;
        }
        this.resellerId = resellerId;
        this.token = token;
    }

    void setZoneMinInterval(double seconds) {
        this.zoneMinInterval = Math.max(0.0, seconds);
    }

    // ---------------------------------------------------------------- errors

    static String formatErrorCode(Object code) {
        if (Php.isNumeric(code)) {
            return "API_" + str(code);
        }
        return str(code);
    }

    static Map<String, Object> setError(Object code) {
        return setError(code, "", "");
    }

    static Map<String, Object> setError(Object code, Object message, Object details) {
        Map<String, Object> result = new LinkedHashMap<>();
        String[] def = DEFAULT_ERRORS.get(str(code));
        if (def != null) {
            result.put("Code", str(code));
            result.put("Message", !Php.trim(message).isEmpty() ? message : def[0]);
            result.put("Details", !Php.trim(details).isEmpty() ? details : def[1]);
        } else {
            result.put("Code", code);
            result.put("Message", message);
            result.put("Details", details);
        }
        return result;
    }

    /** The catch-block envelope every network method returns on failure. */
    private Map<String, Object> fail(ApiException e, String fallback) {
        String code = formatErrorCode(e.code);
        if (empty(code)) {
            code = fallback;
        }
        Object details = coalesce(get(lastParsedResponse, "Details"), get(lastResponse, "raw_response"), e.getMessage());
        return map("result", RESULT_ERROR, "error", setError(code, e.getMessage(), details));
    }

    private static String httpStatusReason(int status) {
        switch (status) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 402: return "Payment Required";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default:
                if (status >= 500) {
                    return "Server Error";
                }
                if (status >= 400) {
                    return "Client Error";
                }
                return "Unexpected Response";
        }
    }

    private static String formatApiDate(Object value) {
        if (value == null || "".equals(value)) {
            return "";
        }
        Long ts = Php.strtotime(str(value));
        return ts == null ? "" : Php.formatDate(ts);
    }

    private static long remainingDaysFromDate(Object value) {
        if (value == null || "".equals(value)) {
            return 0;
        }
        Long ts = Php.strtotime(str(value));
        if (ts == null) {
            return 0;
        }
        long now = System.currentTimeMillis() / 1000L;
        return Math.max(0L, (long) Math.ceil((ts - now) / 86400.0));
    }

    // ---------------------------------------------------------------- HTTP

    private static final Pattern BOILERPLATE = Pattern.compile("Exception of type '[^']*' was thrown", Pattern.CASE_INSENSITIVE);

    /**
     * Port of {@code DNARest::request()}: sends the call, records the
     * diagnostics, maps any non-2xx status or transport failure to an
     * {@link ApiException} and reports it to telemetry.
     */
    Object request(String method, String endpoint, Object data) throws ApiException {
        paceZoneRequest(endpoint);

        startAtNanos = System.nanoTime();
        long startWallMs = System.currentTimeMillis();

        String ep = endpoint == null ? "" : endpoint;
        String opPath = Php.ltrim(ep, "?");
        int q = opPath.indexOf('?');
        lastFunction = "request:" + method + " " + (q >= 0 ? opPath.substring(0, q) : opPath);

        String url = serviceUrl + "/" + Php.ltrim(ep, "/");
        lastRequest = map("url", url, "method", method, "payload", data);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("X-API-KEY", token);
        headers.put("__reseller", resellerId);
        headers.put("User-Agent", DomainNameApi.USER_AGENT);

        String body = null;
        if ("GET".equals(method) || "DELETE".equals(method)) {
            if (!empty(data) && data instanceof Map) {
                url += "?" + Php.httpBuildQuery((Map<?, ?>) data);
            }
        } else {
            body = Json.encodePhp(data);
        }

        HttpTransport.Response resp;
        try {
            resp = transport.send(new HttpTransport.Request(method, url, headers, body, sslVerify,
                DEFAULT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT));
        } catch (HttpTransport.TransportException te) {
            // Transport failure (timeout, DNS, TLS, reset): an error, never an empty success.
            int errno = te.getCode();
            String detail = te.getMessage() == null ? "" : te.getMessage();
            String errorMessage = errno == HttpTransport.TransportException.TIMEOUT
                ? "Could not reach the DomainNameAPI service: the connection timed out. (" + detail + ")"
                : "Curl error during request: " + detail;
            lastResponseHeaders = map("url", url, "http_code", 0,
                "total_time", (System.nanoTime() - startAtNanos) / 1e9);
            lastResponse = map("raw_response", Boolean.FALSE);
            lastParsedResponse = setError("CURL_" + errno, errorMessage, errorMessage);
            telemetry.sendError(this, errorMessage, errno);
            throw new ApiException(errorMessage, errno);
        }

        int status = resp.getStatus();
        String responseBody = resp.getBody();
        Map<String, Object> info = map("url", url, "http_code", status,
            "content_type", resp.getHeaders().get("content-type"),
            "total_time", (System.nanoTime() - startAtNanos) / 1e9);
        info.put("headers", new LinkedHashMap<String, Object>(resp.getHeaders()));
        lastResponseHeaders = info;
        Object decoded = Json.decode(responseBody);
        lastResponse = decoded != null ? decoded : map("raw_response", responseBody);

        if (status >= 200 && status <= 299) {
            lastParsedResponse = decoded;
            double durationMs = (System.nanoTime() - startAtNanos) / 1e6;
            telemetry.sendPerformance(this, durationMs, startWallMs, System.currentTimeMillis());
            return decoded;
        }

        Object parsed = decoded;
        if (status == 302 || status == 301) {
            parsed = map("message", "Invalid API credentials", "code", "CREDENTIALS",
                "details", "Authentication failed. Check your API key and reseller ID.");
        }

        Object errorMessage = coalesce(get(parsed, "message"), get(parsed, "error", "message"), responseBody);
        Object errorCode = coalesce(get(parsed, "code"), get(parsed, "error", "code"), "HTTP_" + status);
        Object errorDetails = coalesce(get(parsed, "details"), get(parsed, "error", "details"), responseBody);

        if (Php.trim(errorMessage).isEmpty()) {
            errorMessage = "HTTP " + status + " - " + httpStatusReason(status);
            if (Php.trim(errorDetails).isEmpty()) {
                errorDetails = errorMessage;
            }
        }

        boolean isBoilerplate = BOILERPLATE.matcher(str(errorMessage)).find();
        if (isBoilerplate || str(errorMessage).equals(str(errorCode))) {
            String detailsClean = Php.trim(str(errorDetails).replaceFirst("^Dna\\s+", ""));
            if (!detailsClean.isEmpty()
                && !detailsClean.equals(str(errorMessage))
                && !detailsClean.equals(str(errorCode))
                && !BOILERPLATE.matcher(detailsClean).find()) {
                errorMessage = detailsClean;
            } else {
                errorMessage = "The operation could not be completed by the registry (error: " + str(errorCode)
                    + "). Please try again or contact support.";
            }
        }

        Object validationErrors = coalesce(get(parsed, "error", "validationErrors"), get(parsed, "validationErrors"));
        if (Php.isArray(validationErrors) && !empty(validationErrors)) {
            String flattened = flattenValidationErrors(validationErrors);
            if (!flattened.isEmpty()) {
                errorDetails = flattened;
            }
        }

        lastParsedResponse = setError(errorCode, errorMessage, errorDetails);
        String msg = str(errorMessage);
        telemetry.sendError(this, msg, status);
        throw new ApiException(msg, status);
    }

    private void paceZoneRequest(String endpoint) {
        if (!Php.ltrim(endpoint == null ? "" : endpoint, "/").startsWith("domains/zones")) {
            return;
        }
        if (lastZoneRequestAtNanos != 0L && zoneMinInterval > 0) {
            long waitNanos = (long) (zoneMinInterval * 1e9) - (System.nanoTime() - lastZoneRequestAtNanos);
            if (waitNanos > 0) {
                try {
                    Thread.sleep(waitNanos / 1_000_000L, (int) (waitNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        lastZoneRequestAtNanos = System.nanoTime();
    }

    // ---------------------------------------------------------------- account

    Map<String, Object> getResellerDetails() {
        try {
            Object response = request("GET", "deposit/accounts/me", map());
            Map<String, Object> resp = new LinkedHashMap<>();
            if (get(response, "resellerId") != null) {
                resp.put("result", RESULT_OK);
                resp.put("id", get(response, "resellerId"));
                resp.put("active", true);
                resp.put("name", coalesce(get(response, "resellerName"), ""));
                resp.put("balance", str(coalesce(get(response, "usdBalance"), 0)));
                resp.put("currency", "USD");
                resp.put("symbol", "$");
                resp.put("balances", list(
                    map("balance", str(coalesce(get(response, "usdBalance"), 0)), "currency", "USD", "symbol", "$"),
                    map("balance", str(coalesce(get(response, "tryBalance"), 0)), "currency", "TL", "symbol", "TL")));
            } else {
                resp.put("result", RESULT_ERROR);
                resp.put("error", setError("CREDENTIALS", "Invalid response format",
                    "Response does not contain required fields"));
            }
            return resp;
        } catch (ApiException e) {
            return fail(e, "RESELLER_DETAILS");
        }
    }

    Map<String, Object> getCurrentBalance(Object currencyId) {
        String currencyName = Php.strtoupper(currencyId);
        Object[] mapped;
        switch (currencyName) {
            case "USD": mapped = new Object[]{2, "USD", "$"}; break;
            case "TRY": mapped = new Object[]{1, "TL", "TL"}; break;
            case "EUR": mapped = new Object[]{3, "EUR", "€"}; break;
            case "GBP": mapped = new Object[]{4, "GBP", "£"}; break;
            default: mapped = new Object[]{0, currencyName, ""};
        }
        try {
            Object response = request("GET", "deposit/accounts/me", map("currency", currencyName));
            String balanceKey = Php.strtolower(currencyId) + "Balance";
            return map(
                "ErrorCode", 0,
                "OperationMessage", "Command completed succesfully.",
                "OperationResult", "SUCCESS",
                "Balance", Php.numberFormat(Php.toFloat(coalesce(get(response, balanceKey), 0)), 2),
                "CurrencyId", mapped[0],
                "CurrencyInfo", null,
                "CurrencyName", mapped[1],
                "CurrencySymbol", mapped[2]);
        } catch (ApiException e) {
            String code = formatErrorCode(e.code);
            return map(
                "ErrorCode", empty(code) ? "BALANCE" : code,
                "OperationMessage", e.getMessage(),
                "OperationResult", "FAILED",
                "Balance", "0.00",
                "CurrencyId", mapped[0],
                "CurrencyInfo", null,
                "CurrencyName", mapped[1],
                "CurrencySymbol", mapped[2]);
        }
    }

    // ---------------------------------------------------------------- search and lists

    Object checkAvailability(Object domains, Object extensions, Object period, Object command) {
        try {
            List<Object> queries = new ArrayList<>();
            for (Object domain : Php.values(domains)) {
                for (Object tld : Php.values(extensions)) {
                    queries.add(map("domainName", str(domain) + "." + Php.ltrim(str(tld), ".")));
                }
            }
            if (queries.isEmpty()) {
                return map("result", RESULT_ERROR, "error", setError("AVAILABILITY", "Domain names not found.",
                    "No domain names provided for availability check"));
            }

            Object response = request("POST", "domains/bulk-search", queries);

            List<Object> availability = new ArrayList<>();
            Object items = coalesce(get(response, "infos"), response);
            if (Php.isArray(items)) {
                for (Object item : Php.values(items)) {
                    if (!Php.isArray(item)) {
                        continue;
                    }
                    Object tldRaw = get(item, "tld");
                    String tld;
                    if (tldRaw != null) {
                        tld = Php.strtolower(tldRaw);
                    } else {
                        String dn = str(coalesce(get(item, "domainName"), ""));
                        int dot = dn.lastIndexOf('.');
                        tld = Php.strtolower(dot >= 0 ? dn.substring(dot + 1) : "");
                    }
                    Object domainRaw = coalesce(get(item, "domainName"), "");
                    String domainName = Php.strtolower(domainRaw).replace("." + tld, "");
                    String status = Php.strtolower(coalesce(get(item, "status"), ""));
                    boolean available = status.equals("available") || status.equals("1") || status.equals("true");

                    Object price = get(item, "price");
                    availability.add(map(
                        "TLD", tld,
                        "DomainName", domainName,
                        "Status", available ? "available" : "notavailable",
                        "Command", command,
                        "Period", coalesce(get(item, "period"), period),
                        "IsFee", coalesce(get(item, "isPremium"), false),
                        "Price", price != null ? Php.numberFormat(Php.toFloat(price), 4) : null,
                        "Currency", get(item, "currency"),
                        "Reason", get(item, "reason")));
                }
            }
            return availability;
        } catch (ApiException e) {
            return fail(e, "AVAILABILITY");
        }
    }

    Map<String, Object> getList(Object extraParameters) {
        try {
            Map<String, Object> extra = assoc(extraParameters);
            if (extra.get("PageSize") != null) {
                extra.put("MaxResultCount", Php.intValue(Php.toInt(extra.get("PageSize"))));
            }
            if (extra.get("PageNumber") != null) {
                long pageSize = Php.toInt(coalesce(extra.get("PageSize"), 200));
                extra.put("SkipCount", Php.intValue(Php.toInt(extra.get("PageNumber")) * pageSize));
            }
            if (extra.get("OrderColumn") != null) {
                String direction = Php.strtoupper(str(coalesce(extra.get("OrderDirection"), "ASC"))).equals("DESC") ? "DESC" : "ASC";
                extra.put("Sorting", str(extra.get("OrderColumn")) + " " + direction);
            }
            extra.remove("PageNumber");
            extra.remove("PageSize");
            extra.remove("OrderColumn");
            extra.remove("OrderDirection");

            Map<String, Object> params = map("MaxResultCount", 200, "SkipCount", 0);
            params.putAll(extra);
            Object response = request("GET", "domains", params);

            Object domains = get(response, "items") != null
                ? Php.mapValues(get(response, "items"), this::listItem)
                : new ArrayList<>();
            return map(
                "data", map("Domains", domains),
                "result", RESULT_OK,
                "TotalCount", Php.intValue(Php.toInt(coalesce(get(response, "totalCount"), 0))));
        } catch (ApiException e) {
            return fail(e, "DOMAIN_LIST");
        }
    }

    private Object listItem(Object item) {
        Object statusCode = get(item, "statusCode");
        Object statusText = get(item, "statusText");
        String status;
        if (statusCode != null && !"".equals(statusCode) && !Php.isNumeric(statusCode)) {
            status = str(statusCode);
        } else if (statusText != null && !"".equals(statusText) && !Php.isNumeric(statusText)) {
            status = str(statusText);
        } else {
            status = normalizeDomainStatus(coalesce(get(item, "status"), ""));
        }
        return map(
            "ID", Php.intValue(Php.toInt(coalesce(get(item, "id"), 0))),
            "Status", status,
            "DomainName", coalesce(get(item, "domainName"), ""),
            "AuthCode", coalesce(get(item, "authCode"), ""),
            "LockStatus", truthy(get(item, "lockStatus")) ? "true" : "false",
            "PrivacyProtectionStatus", truthy(get(item, "privacyProtectionStatus")) ? "true" : "false",
            "IsChildNameServer", truthy(get(item, "hosts")) ? "true" : "false",
            "Contacts", map(
                "Billing", map("ID", ""),
                "Technical", map("ID", ""),
                "Administrative", map("ID", ""),
                "Registrant", map("ID", "")),
            "Dates", map(
                "Start", formatApiDate(get(item, "startDate")),
                "Expiration", formatApiDate(get(item, "expirationDate")),
                "RemainingDays", Php.intValue(Php.toInt(coalesce(get(item, "remainingDay"), 0)))),
            "NameServers", coalesce(get(item, "nameServers"), new ArrayList<>()),
            "Additional", new ArrayList<>(),
            "ChildNameServers", new ArrayList<>());
    }

    Map<String, Object> getTldList(Object count) {
        try {
            Object response = request("GET", "products/tlds", map("MaxResultCount", count, "SkipCount", 0));

            List<Object> tldData = new ArrayList<>();
            int idCounter = 1;
            Object items = get(response, "items");
            if (items != null && Php.isArray(items)) {
                String[][] priceTypes = {
                    {"register", "registration"}, {"renew", "renew"}, {"transfer", "transfer"},
                    {"restore", "restore"}, {"refund", "refund"}, {"backorder", "backorder"}};
                for (Object tld : Php.values(items)) {
                    Map<String, TreeMap<Long, String>> pricing = new LinkedHashMap<>();
                    Map<String, Object> currencies = new LinkedHashMap<>();

                    Object p0 = get(tld, "prices", 0);
                    if (p0 != null && Php.isArray(p0)) {
                        for (String[] pt : priceTypes) {
                            Object apiValue = get(p0, pt[0]);
                            if (apiValue == null) {
                                continue;
                            }
                            if (Php.isArray(apiValue) && get(apiValue, 0) != null) {
                                for (Object priceInfo : Php.values(apiValue)) {
                                    if (Php.isArray(priceInfo)) {
                                        addPrice(pricing, currencies, pt[1], priceInfo);
                                    }
                                }
                            } else if (Php.isArray(apiValue)) {
                                addPrice(pricing, currencies, pt[1], apiValue);
                            }
                        }
                    }

                    Map<String, Object> pricingOut = new LinkedHashMap<>();
                    for (Map.Entry<String, TreeMap<Long, String>> e : pricing.entrySet()) {
                        Map<String, Object> periods = new LinkedHashMap<>();
                        for (Map.Entry<Long, String> p : e.getValue().entrySet()) {
                            periods.put(String.valueOf(p.getKey()), p.getValue());
                        }
                        pricingOut.put(e.getKey(), periods);
                    }

                    tldData.add(map(
                        "id", idCounter++,
                        "status", coalesce(get(tld, "status"), "Active"),
                        "maxchar", coalesce(get(tld, "constraints", "maxLenght"), 63),
                        "maxperiod", coalesce(get(tld, "maxRegistrationPeriod"), 10),
                        "minchar", coalesce(get(tld, "constraints", "minLength"), 1),
                        "minperiod", coalesce(get(tld, "minRegistrationPeriod"), 1),
                        "tld", get(tld, "name"),
                        "pricing", pricingOut,
                        "currencies", currencies));
                }
            }
            return map("data", tldData, "result", RESULT_OK);
        } catch (ApiException e) {
            return fail(e, "TLD_LIST");
        }
    }

    private static void addPrice(Map<String, TreeMap<Long, String>> pricing, Map<String, Object> currencies,
                                 String outType, Object priceInfo) {
        long period = Php.toInt(coalesce(get(priceInfo, "period"), 1));
        if (period < 1) {
            period = 1;
        }
        Object priceRaw = get(priceInfo, "price");
        String price = priceRaw != null ? Php.numberFormat(Php.toFloat(priceRaw), 4) : "0.0000";
        pricing.computeIfAbsent(outType, k -> new TreeMap<>()).put(period, price);
        currencies.put(outType, coalesce(get(priceInfo, "currency"), ""));
    }

    // ---------------------------------------------------------------- domain

    Map<String, Object> getDetails(Object domainName) {
        try {
            Object response = request("GET", "domains/info", map("DomainName", domainName));
            return parseDomainInfo(response);
        } catch (ApiException e) {
            return fail(e, "DOMAIN_DETAILS");
        }
    }

    Map<String, Object> modifyNameServer(Object domainName, Object nameServers) {
        try {
            Map<String, Object> payload = map("domainName", domainName, "nameServers", normalizeHostNameList(nameServers));
            Object response = request("PUT", "domains/dns/name-server", payload);
            return map("result", RESULT_OK, "data", map("NameServers", coalesce(get(response, "nameServers"), nameServers)));
        } catch (ApiException e) {
            return fail(e, "MODIFY_NS");
        }
    }

    Map<String, Object> enableTheftProtectionLock(Object domainName) {
        try {
            request("POST", "domains/lock", map("domainName", domainName));
            return map("result", RESULT_OK, "data", map("LockStatus", true));
        } catch (ApiException e) {
            return fail(e, "ENABLE_LOCK");
        }
    }

    Map<String, Object> disableTheftProtectionLock(Object domainName) {
        try {
            request("POST", "domains/unlock", map("domainName", domainName));
            return map("result", RESULT_OK, "data", map("LockStatus", false));
        } catch (ApiException e) {
            return fail(e, "DISABLE_LOCK");
        }
    }

    // ---------------------------------------------------------------- child nameservers

    Map<String, Object> addChildNameServer(Object domainName, Object nameServer, Object ipAddress) {
        try {
            Map<String, Object> payload = map(
                "domainName", domainName,
                "hostName", normalizeHostName(nameServer),
                "ipAddresses", list(map("ipAddress", ipAddress, "ipVersion", Php.isIpv4(ipAddress) ? "v4" : "v6")));
            request("POST", "domains/dns/host", payload);
            return map("result", RESULT_OK, "data", map("NameServer", nameServer, "IPAdresses", list(ipAddress)));
        } catch (ApiException e) {
            return fail(e, "ADD_CHILD_NS");
        }
    }

    Map<String, Object> deleteChildNameServer(Object domainName, Object nameServer) {
        try {
            request("DELETE", "domains/dns/host", map("domainName", domainName, "hostName", normalizeHostName(nameServer)));
            return map("result", RESULT_OK, "data", map("NameServer", nameServer));
        } catch (ApiException e) {
            return fail(e, "DELETE_CHILD_NS");
        }
    }

    Map<String, Object> modifyChildNameServer(Object domainName, Object nameServer, Object ipAddress) {
        try {
            String ns = normalizeHostName(nameServer);
            Map<String, Object> payload = map(
                "domainName", domainName,
                "hostName", ns,
                "newHostName", ns,
                "ipAddresses", list(map("ipAddress", ipAddress, "ipVersion", Php.isIpv4(ipAddress) ? "v4" : "v6")));
            Object response = request("PUT", "domains/dns/host", payload);
            return map("result", RESULT_OK, "data", map(
                "NameServer", coalesce(get(response, "hostName"), ns),
                "IPAdresses", coalesce(get(response, "ipAddresses"), list(ipAddress))));
        } catch (ApiException e) {
            return fail(e, "MODIFY_CHILD_NS");
        }
    }

    // ---------------------------------------------------------------- DNS zone

    private static String relativizeRecordName(Object name, Object domainName) {
        String n = Php.trim(name);
        String domain = Php.rtrim(Php.strtolower(Php.trim(domainName)), ".");
        if (n.isEmpty() || n.equals("@")) {
            return "";
        }
        String bare = Php.rtrim(n, ".");
        if (Php.strcaseEquals(bare, domain)) {
            return "";
        }
        String suffix = "." + domain;
        if (bare.length() > suffix.length()
            && Php.strcaseEquals(bare.substring(bare.length() - suffix.length()), suffix)) {
            return bare.substring(0, bare.length() - suffix.length());
        }
        return bare;
    }

    private static String unsupportedZoneType(Object type) {
        String t = Php.strtoupper(Php.trim(type));
        if (t.isEmpty()) {
            return "Record type is required.";
        }
        if (!ZONE_RECORD_TYPES.contains(t)) {
            return "Record type \"" + t + "\" is not supported by the DNS zone API. Supported types: "
                + String.join(", ", ZONE_RECORD_TYPES) + ".";
        }
        return null;
    }

    private static Map<String, Object> plainError(String code, String why) {
        return map("result", RESULT_ERROR, "error", setError(code, why, why));
    }

    Map<String, Object> getResourceRecords(Object domainName) {
        try {
            Object response = request("GET", "domains/zones", map("domainName", domainName));
            List<Object> records = new ArrayList<>();
            for (Object set : Php.values(Php.toArray(response))) {
                if (!Php.isArray(set)) {
                    continue;
                }
                Object name = coalesce(get(set, "name"), "");
                Object type = coalesce(get(set, "type"), "");
                Number ttl = Php.intValue(Php.toInt(coalesce(get(set, "ttl"), 0)));
                Object rows = coalesce(get(set, "records"), new ArrayList<>());
                if (!Php.isArray(rows) || empty(rows)) {
                    records.add(map("Name", name, "Type", type, "TTL", ttl, "Content", "", "Disabled", false));
                    continue;
                }
                for (Object rec : Php.values(rows)) {
                    records.add(map(
                        "Name", name,
                        "Type", type,
                        "TTL", ttl,
                        "Content", coalesce(get(rec, "content"), ""),
                        "Disabled", truthy(get(rec, "disabled"))));
                }
            }
            return map("result", RESULT_OK, "data", map("records", records));
        } catch (ApiException e) {
            return fail(e, "GET_ZONE");
        }
    }

    Map<String, Object> addResourceRecord(Object domainName, Object name, Object type, Object content, Object ttl, Object apply) {
        try {
            String why = unsupportedZoneType(type);
            if (why != null) {
                return plainError("ADD_ZONE", why);
            }
            String n = relativizeRecordName(name, domainName);
            Map<String, Object> body = map("zoneStruct", buildZoneStruct(n, type, content, ttl));
            String endpoint = "domains/zones?" + Php.httpBuildQuery(map("domainName", domainName));
            request("POST", endpoint, body);
            if (truthy(apply)) {
                applyResourceRecords(domainName);
            }
            return map("result", RESULT_OK, "data", map("Name", n, "Type", type,
                "TTL", Php.intValue(Php.toInt(ttl)), "Content", content));
        } catch (ApiException e) {
            return fail(e, "ADD_ZONE");
        }
    }

    Map<String, Object> editResourceRecord(Object domainName, Object recordName, Object name, Object type,
                                           Object content, Object ttl, Object apply) {
        try {
            String why = unsupportedZoneType(type);
            if (why != null) {
                return plainError("EDIT_ZONE", why);
            }
            String n = relativizeRecordName(name, domainName);
            Map<String, Object> body = map("zoneStruct", buildZoneStruct(n, type, content, ttl));
            String endpoint = "domains/zones?" + Php.httpBuildQuery(map("domainName", domainName, "recordName", recordName));
            request("PUT", endpoint, body);
            if (truthy(apply)) {
                applyResourceRecords(domainName);
            }
            return map("result", RESULT_OK, "data", map("Name", n, "Type", type,
                "TTL", Php.intValue(Php.toInt(ttl)), "Content", content));
        } catch (ApiException e) {
            return fail(e, "EDIT_ZONE");
        }
    }

    Map<String, Object> deleteResourceRecord(Object domainName, Object name, Object record, Object recordType, Object apply) {
        try {
            request("DELETE", "domains/zones", map(
                "domainName", domainName,
                "Name", name,
                "Record", record,
                "RecordType", recordType));
            if (truthy(apply)) {
                applyResourceRecords(domainName);
            }
            return map("result", RESULT_OK, "data", map("Name", name, "Type", recordType, "Content", record));
        } catch (ApiException e) {
            return fail(e, "DELETE_ZONE");
        }
    }

    Map<String, Object> applyResourceRecords(Object domainName) {
        try {
            String endpoint = "domains/zones/apply?" + Php.httpBuildQuery(map("domainName", domainName));
            request("POST", endpoint, new ArrayList<>());
            return map("result", RESULT_OK, "data", map("DomainName", domainName));
        } catch (ApiException e) {
            return fail(e, "APPLY_ZONE");
        }
    }

    Map<String, Object> setResourceRecordSet(Object domainName, Object name, Object type, Object contents,
                                             Object ttl, Object apply) {
        try {
            String why = unsupportedZoneType(type);
            if (why != null) {
                return plainError("SET_ZONE", why);
            }
            String n = relativizeRecordName(name, domainName);
            List<Object> values = new ArrayList<>();
            for (Object v : Php.values(contents)) {
                String s = str(v);
                if (!Php.trim(s).isEmpty()) {
                    values.add(s);
                }
            }
            if (values.isEmpty()) {
                return deleteResourceRecordSet(domainName, n, type, apply, "");
            }
            Map<String, Object> body = map("zoneStruct", buildZoneStruct(n, type, values, ttl));
            String endpoint = "domains/zones?" + Php.httpBuildQuery(map("domainName", domainName));
            request("POST", endpoint, body);
            if (truthy(apply)) {
                applyResourceRecords(domainName);
            }
            return map("result", RESULT_OK, "data", map("Name", n, "Type", Php.strtoupper(type),
                "TTL", Php.intValue(Php.toInt(ttl)), "Contents", values));
        } catch (ApiException e) {
            return fail(e, "SET_ZONE");
        }
    }

    Map<String, Object> deleteResourceRecordSet(Object domainName, Object name, Object type, Object apply, Object knownValue) {
        String t = Php.strtoupper(Php.trim(type));
        String value = str(knownValue);
        if (value.isEmpty()) {
            Map<String, Object> current = getResourceRecords(domainName);
            if (!RESULT_OK.equals(current.get("result"))) {
                return current;
            }
            String fqdn = qualifyRecordName(name, domainName);
            for (Object rec : Php.values(get(current, "data", "records"))) {
                if (Php.strcaseEquals(get(rec, "Name"), fqdn) && Php.strcaseEquals(get(rec, "Type"), t)) {
                    value = str(get(rec, "Content"));
                    break;
                }
            }
            if (value.isEmpty()) {
                return map("result", RESULT_OK, "data", map("Name", name, "Type", t, "Removed", false));
            }
        }
        return deleteResourceRecord(domainName, qualifyRecordName(name, domainName), value, t, apply);
    }

    Map<String, Object> addResourceRecordValue(Object domainName, Object name, Object type, Object value,
                                               Object ttl, Object apply) {
        String why = unsupportedZoneType(type);
        if (why != null) {
            return plainError("ADD_ZONE", why);
        }
        String n = relativizeRecordName(name, domainName);
        RecordSet set = readResourceRecordSet(domainName, n, type);
        if (set.error != null) {
            return set.error;
        }
        for (String existing : set.contents) {
            if (sameRecordValue(type, existing, value)) {
                return map("result", RESULT_OK, "data", map("Name", n, "Type", Php.strtoupper(type),
                    "Contents", set.contents, "Changed", false));
            }
        }
        List<Object> contents = new ArrayList<>(set.contents);
        contents.add(str(value));
        return setResourceRecordSet(domainName, n, type, contents,
            ttl != null ? ttl : (set.ttl != 0 ? set.ttl : 3600), apply);
    }

    Map<String, Object> removeResourceRecordValue(Object domainName, Object name, Object type, Object value, Object apply) {
        String why = unsupportedZoneType(type);
        if (why != null) {
            return plainError("DELETE_ZONE", why);
        }
        String n = relativizeRecordName(name, domainName);
        RecordSet set = readResourceRecordSet(domainName, n, type);
        if (set.error != null) {
            return set.error;
        }
        List<Object> remaining = new ArrayList<>();
        boolean found = false;
        for (String existing : set.contents) {
            if (!found && sameRecordValue(type, existing, value)) {
                found = true;
                continue;
            }
            remaining.add(existing);
        }
        if (!found) {
            return map("result", RESULT_OK, "data", map("Name", n, "Type", Php.strtoupper(type),
                "Contents", set.contents, "Changed", false));
        }
        if (remaining.isEmpty()) {
            return deleteResourceRecordSet(domainName, n, type, apply, str(value));
        }
        return setResourceRecordSet(domainName, n, type, remaining, set.ttl != 0 ? set.ttl : 3600, apply);
    }

    Map<String, Object> replaceResourceRecordValue(Object domainName, Object name, Object type, Object oldValue,
                                                   Object newValue, Object ttl, Object apply) {
        String why = unsupportedZoneType(type);
        if (why != null) {
            return plainError("EDIT_ZONE", why);
        }
        String n = relativizeRecordName(name, domainName);
        RecordSet set = readResourceRecordSet(domainName, n, type);
        if (set.error != null) {
            return set.error;
        }
        List<Object> contents = new ArrayList<>();
        boolean found = false;
        for (String existing : set.contents) {
            if (!found && sameRecordValue(type, existing, oldValue)) {
                found = true;
                contents.add(str(newValue));
                continue;
            }
            contents.add(existing);
        }
        if (!found) {
            String msg = "No " + Php.strtoupper(type) + " record \"" + n + "\" with value \"" + str(oldValue) + "\" to edit.";
            return plainError("EDIT_ZONE", msg);
        }
        return setResourceRecordSet(domainName, n, type, contents,
            ttl != null ? ttl : (set.ttl != 0 ? set.ttl : 3600), apply);
    }

    /** One resource-record set, or the error envelope of the read. */
    private static final class RecordSet {
        long ttl;
        final List<String> contents = new ArrayList<>();
        Map<String, Object> error;
    }

    private RecordSet readResourceRecordSet(Object domainName, Object name, Object type) {
        RecordSet out = new RecordSet();
        Map<String, Object> current = getResourceRecords(domainName);
        if (!RESULT_OK.equals(current.get("result"))) {
            out.error = current;
            return out;
        }
        String fqdn = qualifyRecordName(name, domainName);
        String t = Php.strtoupper(Php.trim(type));
        for (Object rec : Php.values(get(current, "data", "records"))) {
            if (!Php.strcaseEquals(get(rec, "Name"), fqdn) || !Php.strcaseEquals(get(rec, "Type"), t)) {
                continue;
            }
            String content = str(get(rec, "Content"));
            if (!content.isEmpty()) {
                out.contents.add(content);
            }
            out.ttl = Php.toInt(get(rec, "TTL"));
        }
        return out;
    }

    private static boolean sameRecordValue(Object type, Object a, Object b) {
        boolean txt = Php.strtoupper(type).equals("TXT");
        return normalizeRecordValue(txt, a).equals(normalizeRecordValue(txt, b));
    }

    private static String normalizeRecordValue(boolean txt, Object v) {
        String s = Php.trim(v);
        if (txt) {
            if (s.length() > 1 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                s = s.substring(1, s.length() - 1);
            }
            return s;
        }
        return Php.strtolower(Php.rtrim(s, "."));
    }

    private static String qualifyRecordName(Object name, Object domainName) {
        String n = Php.trim(name);
        String domain = Php.rtrim(Php.strtolower(Php.trim(domainName)), ".");
        String apex = domain + ".";
        if (n.isEmpty() || n.equals("@")) {
            return apex;
        }
        String bare = Php.rtrim(n, ".");
        if (Php.strcaseEquals(bare, domain) || Php.strtolower(bare).endsWith("." + domain)) {
            return bare + ".";
        }
        return bare + "." + apex;
    }

    private static Map<String, Object> buildZoneStruct(Object name, Object type, Object content, Object ttl) {
        long t = Php.toInt(ttl);
        if (t < 1) {
            t = 3600;
        }
        if (t > 86400) {
            t = 86400;
        }
        List<Object> contents = new ArrayList<>();
        for (Object c : Php.values(Php.toArray(content))) {
            contents.add(str(c));
        }
        return map("name", str(name), "ttl", Php.intValue(t), "type", Php.strtoupper(type), "contents", contents);
    }

    // ---------------------------------------------------------------- forwarding

    Map<String, Object> getForwarding(Object domainName) {
        try {
            Object response = request("GET", "domains/forwards", map("domainName", domainName));
            return map("result", RESULT_OK, "data", map(
                "Enabled", truthy(get(response, "redirectAlias")),
                "RedirectAddress", str(coalesce(get(response, "redirectAlias"), "")),
                "ForwardType", str(coalesce(get(response, "redirectType"), ""))));
        } catch (ApiException e) {
            Object raw = coalesce(get(lastResponse, "raw_response"), e.getMessage());
            if (str(raw).toLowerCase(java.util.Locale.ROOT).contains("not found")
                || str(e.getMessage()).toLowerCase(java.util.Locale.ROOT).contains("not found")) {
                return map("result", RESULT_OK, "data", map("Enabled", false, "RedirectAddress", "", "ForwardType", ""));
            }
            String code = formatErrorCode(e.code);
            return map("result", RESULT_ERROR, "error", setError(empty(code) ? "GET_FORWARD" : code, e.getMessage(),
                coalesce(get(lastParsedResponse, "Details"), raw)));
        }
    }

    Map<String, Object> setForwarding(Object domainName, Object redirectAddress, Object forwardType) {
        try {
            String type = Php.ucfirst(Php.strtolower(Php.trim(forwardType)));
            if (!FORWARD_TYPES.contains(type)) {
                return plainError("SET_FORWARD", "Forward type \"" + str(forwardType) + "\" is not supported. Supported types: "
                    + String.join(", ", FORWARD_TYPES) + ".");
            }
            if (Php.trim(redirectAddress).isEmpty()) {
                return plainError("SET_FORWARD", "Redirect address is required.");
            }
            request("POST", "domains/forwards", map(
                "domainName", domainName,
                "redirectAddress", redirectAddress,
                "forwardType", type));
            return map("result", RESULT_OK, "data", map("RedirectAddress", redirectAddress, "ForwardType", type));
        } catch (ApiException e) {
            return fail(e, "SET_FORWARD");
        }
    }

    Map<String, Object> deleteForwarding(Object domainName) {
        try {
            request("DELETE", "domains/forwards", map("DomainName", domainName));
            return map("result", RESULT_OK, "data", map("DomainName", domainName));
        } catch (ApiException e) {
            return fail(e, "DELETE_FORWARD");
        }
    }

    // ---------------------------------------------------------------- contacts

    Map<String, Object> getContacts(Object domainName) {
        try {
            Object domainInfo = request("GET", "domains/info", map("DomainName", domainName));
            Map<String, String> typeMap = new LinkedHashMap<>();
            typeMap.put("Admin", "Administrative");
            typeMap.put("Billing", "Billing");
            typeMap.put("Tech", "Technical");
            typeMap.put("Registrant", "Registrant");

            Map<String, Object> contacts = new LinkedHashMap<>();
            Object items = get(domainInfo, "contacts");
            if (items != null && Php.isArray(items)) {
                for (Object item : Php.values(items)) {
                    if (!(item instanceof Map)) {
                        continue;
                    }
                    String apiType = str(coalesce(get(item, "contactType"), ""));
                    String key = typeMap.containsKey(apiType) ? typeMap.get(apiType) : Php.ucfirst(Php.strtolower(apiType));
                    Map<String, Object> merged = assoc(item);
                    merged.put("type", key);
                    contacts.put(key, parseContactInfo(merged));
                }
            }
            Map<String, Object> ordered = new LinkedHashMap<>();
            for (String k : new String[]{"Administrative", "Billing", "Registrant", "Technical"}) {
                if (contacts.get(k) != null) {
                    ordered.put(k, contacts.get(k));
                }
            }
            return map("data", map("contacts", ordered), "result", RESULT_OK);
        } catch (ApiException e) {
            return fail(e, "GET_CONTACTS");
        }
    }

    Map<String, Object> saveContacts(Object domainName, Object contacts) {
        try {
            List<Object> payloadContacts = new ArrayList<>();
            for (Map.Entry<String, Object> e : assoc(contacts).entrySet()) {
                payloadContacts.add(parseContact(e.getValue(), Php.ucfirst(Php.strtolower(e.getKey()))));
            }
            Object response = request("PUT", "domains/contacts/update",
                map("domainName", domainName, "contacts", payloadContacts));

            Map<String, Object> parsed = new LinkedHashMap<>();
            Object rc = get(response, "contacts");
            if (rc != null && Php.isArray(rc)) {
                for (Object contact : Php.values(rc)) {
                    parsed.put(Php.ucfirst(Php.strtolower(get(contact, "type"))), parseContactInfo(contact));
                }
            }
            return map("result", RESULT_OK, "data", map("contacts", parsed));
        } catch (ApiException e) {
            return fail(e, "SAVE_CONTACTS");
        }
    }

    // ---------------------------------------------------------------- transfers

    Map<String, Object> transfer(Object domainName, Object eppCode, Object period, Object contacts) {
        try {
            List<Object> payloadContacts = new ArrayList<>();
            if (!empty(contacts)) {
                for (Map.Entry<String, Object> e : assoc(contacts).entrySet()) {
                    payloadContacts.add(parseContact(e.getValue(), Php.ucfirst(Php.strtolower(e.getKey()))));
                }
            }
            Map<String, Object> payload = map(
                "domainName", domainName,
                "authCode", eppCode,
                "period", period,
                "contacts", payloadContacts);
            Object response = request("POST", "domains/transfer", payload);
            return parseDomainInfo(response);
        } catch (ApiException e) {
            return fail(e, "TRANSFER_DOMAIN");
        }
    }

    Map<String, Object> cancelTransfer(Object domainName) {
        return transferAction(domainName, "domains/transfers/cancel", "Cancelled", "CANCEL_TRANSFER");
    }

    Map<String, Object> approveTransfer(Object domainName) {
        return transferAction(domainName, "domains/transfers/approve", "Approved", "APPROVE_TRANSFER");
    }

    Map<String, Object> rejectTransfer(Object domainName) {
        return transferAction(domainName, "domains/transfers/reject", "Rejected", "REJECT_TRANSFER");
    }

    private Map<String, Object> transferAction(Object domainName, String endpoint, String defaultStatus, String fallback) {
        try {
            Object response = request("POST", endpoint, map("domainName", domainName));
            return map("result", RESULT_OK, "data", map(
                "DomainName", domainName,
                "Status", coalesce(get(response, "status"), defaultStatus)));
        } catch (ApiException e) {
            return fail(e, fallback);
        }
    }

    Map<String, Object> checkTransfer(Object domainName, Object authCode) {
        try {
            Object response = request("POST", "domains/transfers/check", map("domainName", domainName, "authCode", authCode));
            boolean available = truthy(get(response, "transferAvailabilityStatus"));
            Object additional = get(response, "additionalAttributes");
            Map<String, Object> envelope = map(
                "result", available ? RESULT_OK : RESULT_ERROR,
                "data", map(
                    "TransferAvailabilityStatus", available,
                    "AuthCodeIsRequired", truthy(get(response, "authCodeIsRequired")),
                    "AuthCodeIsValid", truthy(get(response, "authCodeIsValid")),
                    "UserTransferRequired", truthy(get(response, "userTransferRequired")),
                    "TransferLock", truthy(get(response, "transferLock")),
                    "Message", str(coalesce(get(response, "message"), "")),
                    "MessageKey", str(coalesce(get(response, "messageKey"), "")),
                    "Additional", additional != null ? Php.toArray(additional) : new ArrayList<>()));
            if (!available) {
                envelope.put("error", setError("TRANSFER_NOT_AVAILABLE",
                    coalesce(get(response, "message"), "Domain is not transferable"),
                    coalesce(get(response, "messageKey"), get(response, "message"), "")));
            }
            return envelope;
        } catch (ApiException e) {
            return fail(e, "CHECK_TRANSFER");
        }
    }

    // ---------------------------------------------------------------- lifecycle

    Map<String, Object> renew(Object domainName, Object period) {
        try {
            Object response = request("POST", "domains/renew", map("domainName", domainName, "period", period));
            if (truthy(get(response, "expirationDate"))) {
                return map("result", RESULT_OK, "data", map("ExpirationDate", formatApiDate(get(response, "expirationDate"))));
            }
            telemetry.sendError(this, "[DOMAIN_RENEW] " + DEFAULT_ERRORS.get("DOMAIN_RENEW")[1], 0);
            return map("result", RESULT_ERROR, "error", setError("DOMAIN_RENEW"));
        } catch (ApiException e) {
            return fail(e, "RENEW_DOMAIN");
        }
    }

    Map<String, Object> registerWithContactInfo(Object domainName, Object period, Object contacts, Object nameServers,
                                                Object eppLock, Object privacyLock, Object additionalAttributes) {
        try {
            List<Object> payloadContacts = new ArrayList<>();
            for (Map.Entry<String, Object> e : assoc(contacts).entrySet()) {
                payloadContacts.add(parseContact(e.getValue(), Php.ucfirst(Php.strtolower(e.getKey()))));
            }

            List<Object> ns = normalizeHostNameList(nameServers);
            if (ns.isEmpty()) {
                ns = new ArrayList<>(DEFAULT_NAMESERVERS);
            }

            Map<String, Object> attrs = assoc(additionalAttributes);
            if (Php.strtolower(domainName).endsWith(".tr")) {
                attrs = normalizeTrAttributes(attrs);
            }
            attrs = stringifyAttributes(attrs);

            Map<String, Object> payload = map(
                "domainName", domainName,
                "period", period,
                "nameServers", ns,
                "isLocked", eppLock,
                "privacyEnabled", privacyLock,
                "contacts", payloadContacts,
                "tldAttributes", attrs);
            Object response = request("POST", "domains/register-with-contacts", payload);
            return parseDomainInfo(response);
        } catch (ApiException e) {
            return fail(e, "REGISTER_DOMAIN");
        }
    }

    private static Map<String, Object> stringifyAttributes(Map<String, Object> attrs) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Boolean) {
                out.put(e.getKey(), ((Boolean) v) ? "true" : "false");
            } else if (v == null) {
                out.put(e.getKey(), "");
            } else if (v instanceof Number || v instanceof String) {
                out.put(e.getKey(), str(v));
            } else {
                out.put(e.getKey(), Json.encodePhp(v));
            }
        }
        return out;
    }

    private static String normalizeCountryCode(Object country) {
        String c = Php.trim(country);
        if (c.isEmpty()) {
            return "";
        }
        if (Php.strlen(c) == 2) {
            return Php.strtoupper(c);
        }
        switch (Php.strtolower(c)) {
            case "turkey": case "türkiye": case "turkiye": return "TR";
            case "united states": case "united states of america": case "usa": return "US";
            case "united kingdom": case "great britain": case "england": return "GB";
            case "germany": case "deutschland": return "DE";
            case "france": return "FR";
            case "spain": return "ES";
            case "italy": return "IT";
            case "netherlands": return "NL";
            case "india": return "IN";
            case "china": return "CN";
            case "russia": return "RU";
            case "canada": return "CA";
            case "australia": return "AU";
            case "brazil": return "BR";
            default: return c;
        }
    }

    private static final Pattern INDEX = Pattern.compile("\\[\\d+\\]");
    private static final Pattern SEGMENT_SPLIT = Pattern.compile("[./]+");

    private static String flattenValidationErrors(Object validationErrors) {
        List<String> parts = new ArrayList<>();
        for (Object ve : Php.values(validationErrors)) {
            if (ve instanceof String) {
                String t = Php.trim(ve);
                if (!t.isEmpty()) {
                    parts.add(t);
                }
                continue;
            }
            if (!Php.isArray(ve)) {
                continue;
            }
            Object m = get(ve, "message");
            String message = m != null ? Php.trim(m) : "";
            List<String> members = new ArrayList<>();
            Object mem = get(ve, "members");
            if (mem != null && Php.isArray(mem)) {
                for (Object member : Php.values(mem)) {
                    String label = humanizeValidationMember(str(member));
                    if (!label.isEmpty()) {
                        members.add(label);
                    }
                }
            }
            if (!members.isEmpty() && !message.isEmpty()) {
                parts.add(String.join(", ", members) + " - " + message);
            } else if (!members.isEmpty()) {
                parts.add(String.join(", ", members));
            } else if (!message.isEmpty()) {
                parts.add(message);
            }
        }
        List<String> unique = new ArrayList<>();
        for (String p : parts) {
            if (!unique.contains(p)) {
                unique.add(p);
            }
        }
        return String.join(" | ", unique);
    }

    private static String humanizeValidationMember(String member) {
        String m = Php.trim(member);
        if (m.isEmpty()) {
            return "";
        }
        m = Php.ltrim(m, "$.");
        m = INDEX.matcher(m).replaceAll("");
        List<String> segments = new ArrayList<>();
        for (String s : SEGMENT_SPLIT.split(m)) {
            if (!s.isEmpty()) {
                segments.add(s);
            }
        }
        if (segments.isEmpty()) {
            return "";
        }
        Map<String, String> roles = new LinkedHashMap<>();
        roles.put("administrativecontact", "Admin");
        roles.put("administrative", "Admin");
        roles.put("technicalcontact", "Tech");
        roles.put("technical", "Tech");
        roles.put("registrantcontact", "Registrant");
        roles.put("registrant", "Registrant");
        roles.put("billingcontact", "Billing");
        roles.put("billing", "Billing");
        List<String> out = new ArrayList<>();
        for (String seg : segments) {
            String key = Php.strtolower(seg);
            out.add(roles.containsKey(key) ? roles.get(key) : Php.ucfirst(seg));
        }
        return Php.trim(String.join(" ", out));
    }

    private static Map<String, Object> normalizeTrAttributes(Map<String, Object> attrs) {
        if (attrs.isEmpty()) {
            return attrs;
        }
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("TRABISDOMAINCATEGORY", "TRABISDOMAINCATEGORY");
        keyMap.put("TRABISCITIZENID", "TRABISCITIZENID");
        keyMap.put("TRABISCITIZIENID", "TRABISCITIZENID");
        keyMap.put("TRABISTAXOFFICE", "TRABISTAXOFFICE");
        keyMap.put("TRABISTAXNUMBER", "TRABISTAXNUMBER");

        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            String key = e.getKey();
            if (!keyMap.containsKey(key)) {
                continue;
            }
            if (key.equals("TRABISCITIZIENID") && normalized.containsKey("TRABISCITIZENID")) {
                continue;
            }
            normalized.put(keyMap.get(key), e.getValue());
        }
        Object cat = normalized.get("TRABISDOMAINCATEGORY");
        if (cat != null) {
            String c = Php.strtolower(Php.trim(cat));
            if (Arrays.asList("1", "owner", "personal", "individual").contains(c)) {
                normalized.put("TRABISDOMAINCATEGORY", "Owner");
            } else if (Arrays.asList("0", "corporate", "company").contains(c)) {
                normalized.put("TRABISDOMAINCATEGORY", "Corporate");
            }
        }
        return normalized;
    }

    Map<String, Object> modifyPrivacyProtectionStatus(Object domainName, Object status, Object reason) {
        try {
            // php-dna fills an empty reason with the default; the gateway call does not carry it.
            if (empty(reason)) {
                reason = DEFAULT_REASON;
            }
            request("POST", "domains/privacy", map("domainName", domainName, "privacyStatus", Boolean.TRUE.equals(status)));
            return map("data", map("PrivacyProtectionStatus", status), "result", RESULT_OK);
        } catch (ApiException e) {
            return fail(e, "MODIFY_PRIVACY");
        }
    }

    Map<String, Object> syncFromRegistry(Object domainName) {
        return getDetails(domainName);
    }

    // ---------------------------------------------------------------- parsing

    private static Map<String, Object> parseContactIds(Object contacts) {
        Map<String, Object> result = map(
            "Billing", map("ID", 0),
            "Technical", map("ID", 0),
            "Administrative", map("ID", 0),
            "Registrant", map("ID", 0));
        if (!Php.isArray(contacts)) {
            return result;
        }
        Map<String, String> typeMap = new LinkedHashMap<>();
        typeMap.put("Billing", "Billing");
        typeMap.put("Tech", "Technical");
        typeMap.put("Admin", "Administrative");
        typeMap.put("Registrant", "Registrant");
        for (Object c : Php.values(contacts)) {
            String apiType = str(coalesce(get(c, "ContactType"), get(c, "contactType"), ""));
            String key = typeMap.get(apiType);
            if (key != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> slot = (Map<String, Object>) result.get(key);
                slot.put("ID", coalesce(get(c, "handle"), get(c, "id"), 0));
            }
        }
        return result;
    }

    private static String normalizeDomainStatus(Object value) {
        if (Php.isNumeric(value)) {
            String label = DOMAIN_STATUS_MAP.get((int) Php.toInt(value));
            return label != null ? label : str(value);
        }
        return str(value);
    }

    private static Map<String, Object> parseDomainInfo(Object data) {
        if (empty(data)) {
            return map("result", RESULT_ERROR, "error", setError("RESPONSE"));
        }
        Object nameservers = get(data, "nameservers");
        Object additional = get(data, "additionalAttributes");
        Object hosts = get(data, "hosts");
        Object remaining = get(data, "remainingDay");
        return map(
            "data", map(
                "ID", Php.intValue(Php.toInt(coalesce(get(data, "id"), 0))),
                "Status", normalizeDomainStatus(coalesce(get(data, "status"), "")),
                "DomainName", str(coalesce(get(data, "domainName"), get(data, "name"), "")),
                "AuthCode", str(coalesce(get(data, "authCode"), get(data, "eppCode"), "")),
                "LockStatus", truthy(get(data, "lockStatus")) ? "true" : "false",
                "PrivacyProtectionStatus", truthy(get(data, "privacyProtectionStatus")) ? "true" : "false",
                "IsChildNameServer", truthy(hosts) ? "true" : "false",
                "Contacts", parseContactIds(coalesce(get(data, "contacts"), new ArrayList<>())),
                "Dates", map(
                    "Start", formatApiDate(get(data, "startDate")),
                    "Expiration", formatApiDate(get(data, "expirationDate")),
                    "RemainingDays", remaining != null
                        ? Php.intValue(Php.toInt(remaining))
                        : Php.intValue(remainingDaysFromDate(get(data, "expirationDate")))),
                "NameServers", nameservers != null ? Php.mapValues(nameservers, Php::str) : new ArrayList<>(),
                "Additional", additional != null ? Php.toArray(additional) : new ArrayList<>(),
                "ChildNameServers", hosts != null ? Php.mapValues(hosts, ns -> {
                    List<Object> ips = new ArrayList<>();
                    for (Object ip : Php.values(coalesce(get(ns, "ipAddresses"), new ArrayList<>()))) {
                        ips.add(get(ip, "ipAddress"));
                    }
                    return map("ns", get(ns, "name"), "ip", !ips.isEmpty() ? ips.get(ips.size() - 1) : "");
                }) : new ArrayList<>()),
            "result", RESULT_OK);
    }

    private static Object parseContactInfo(Object data) {
        if (empty(data)) {
            return new ArrayList<>();
        }
        return map(
            "ID", coalesce(get(data, "handle"), get(data, "id"), ""),
            "Status", coalesce(get(data, "status"), "Active"),
            "AuthCode", coalesce(get(data, "authCode"), ""),
            "FirstName", coalesce(get(data, "firstName"), ""),
            "LastName", coalesce(get(data, "lastName"), ""),
            "Company", coalesce(get(data, "companyName"), get(data, "organizationName"), get(data, "company"), ""),
            "EMail", coalesce(get(data, "eMail"), get(data, "emailAddress"), get(data, "email"), ""),
            "Type", coalesce(get(data, "type"), ""),
            "Address", map(
                "Line1", coalesce(get(data, "address"), get(data, "addressLine1"), ""),
                "Line2", coalesce(get(data, "addressLine2"), ""),
                "Line3", coalesce(get(data, "addressLine3"), ""),
                "State", coalesce(get(data, "state"), get(data, "stateOrProvince"), ""),
                "City", coalesce(get(data, "city"), ""),
                "Country", coalesce(get(data, "country"), get(data, "countryCode"), ""),
                "ZipCode", coalesce(get(data, "postalCode"), get(data, "zipCode"), "")),
            "Phone", map(
                "Phone", map(
                    "Number", coalesce(get(data, "phone"), get(data, "phoneNumber"), ""),
                    "CountryCode", coalesce(get(data, "phoneCountryCode"), "")),
                "Fax", map(
                    "Number", coalesce(get(data, "fax"), get(data, "faxNumber"), ""),
                    "CountryCode", coalesce(get(data, "faxCountryCode"), ""))),
            "Additional", coalesce(get(data, "additionalAttributes"), new ArrayList<>()));
    }

    private Map<String, Object> parseContact(Object contact, String type) {
        String apiType;
        switch (type) {
            case "Administrative": apiType = "Admin"; break;
            case "Technical": apiType = "Tech"; break;
            default: apiType = type;
        }

        Object address = coalesce(get(contact, "AddressLine1"), get(contact, "Address", "Line1"), "");
        Object line2 = coalesce(get(contact, "AddressLine2"), get(contact, "Address", "Line2"), "");
        if (truthy(line2)) {
            address = str(address) + " " + str(line2);
        }

        Object phone = coalesce(get(contact, "Phone"), get(contact, "Phone", "Phone", "Number"), "");
        Object phoneCc = coalesce(get(contact, "PhoneCountryCode"), get(contact, "Phone", "Phone", "CountryCode"), "");
        Object fax = coalesce(get(contact, "Fax"), get(contact, "Phone", "Fax", "Number"), "");
        Object faxCc = coalesce(get(contact, "FaxCountryCode"), get(contact, "Phone", "Fax", "CountryCode"), "");
        if (Php.isArray(phone)) {
            phoneCc = coalesce(get(phone, "Phone", "CountryCode"), "");
            phone = coalesce(get(phone, "Phone", "Number"), "");
        }
        if (Php.isArray(fax)) {
            faxCc = coalesce(get(fax, "Fax", "CountryCode"), "");
            fax = coalesce(get(fax, "Fax", "Number"), "");
        }

        Map<String, Object> validated = validateContact(map(
            "FirstName", coalesce(get(contact, "FirstName"), ""),
            "LastName", coalesce(get(contact, "LastName"), ""),
            "AddressLine1", address,
            "City", coalesce(get(contact, "City"), get(contact, "Address", "City"), ""),
            "State", coalesce(get(contact, "State"), get(contact, "Address", "State"), ""),
            "Country", coalesce(get(contact, "Country"), get(contact, "Address", "Country"), ""),
            "ZipCode", coalesce(get(contact, "ZipCode"), get(contact, "Address", "ZipCode"), ""),
            "Phone", phone,
            "PhoneCountryCode", phoneCc,
            "Fax", fax,
            "FaxCountryCode", faxCc));

        return map(
            "contactType", apiType,
            "firstName", validated.get("FirstName"),
            "lastName", validated.get("LastName"),
            "companyName", coalesce(get(contact, "Company"), ""),
            "eMail", coalesce(get(contact, "EMail"), ""),
            "address", validated.get("AddressLine1"),
            "city", validated.get("City"),
            "state", validated.get("State"),
            "country", validated.get("Country"),
            "postalCode", validated.get("ZipCode"),
            "phoneCountryCode", validated.get("PhoneCountryCode"),
            "phone", validated.get("Phone"),
            "faxCountryCode", validated.get("FaxCountryCode"),
            "fax", validated.get("Fax"),
            "isHidden", Php.toBool(coalesce(get(contact, "IsHidden"), false)));
    }

    private static final Pattern INTL_PREFIX = Pattern.compile("^\\s*(\\+|00)");

    /** Split a free-form phone/fax number into {number, countryCode}. */
    static String[] normalizePhone(Object number, Object countryCode) {
        boolean hasIntlPrefix = INTL_PREFIX.matcher(str(number)).find();
        String digits = str(number).replaceAll("[^0-9]", "");
        String cc = str(countryCode).replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return new String[]{"", cc};
        }
        if (hasIntlPrefix) {
            if (digits.startsWith("00")) {
                digits = digits.substring(2);
            }
            if (!cc.isEmpty() && digits.startsWith(cc) && digits.length() > cc.length()) {
                digits = digits.substring(cc.length());
            }
        } else if (!cc.isEmpty() && digits.startsWith(cc) && digits.length() > 10) {
            digits = digits.substring(cc.length());
        }
        if (cc.isEmpty()) {
            if (digits.length() == 12 && digits.startsWith("90")) {
                cc = "90";
                digits = digits.substring(2);
            } else {
                cc = "90";
            }
        }
        if (digits.length() > 1 && digits.charAt(0) == '0') {
            digits = Php.ltrim(digits, "0");
        }
        return new String[]{digits, cc};
    }

    boolean isTrTLD(Object domain) {
        String d = str(domain);
        String tail = d.length() >= 3 ? d.substring(d.length() - 3) : d;
        return Php.strtolower(tail).equals(".tr");
    }

    Map<String, Object> validateContact(Object input) {
        Map<String, Object> contact = assoc(input);
        Map<String, Object> defaults = map(
            "FirstName", "Isimyok",
            "LastName", "Isimyok",
            "AddressLine1", "Addres yok",
            "City", "ISTANBUL",
            "State", "ISTANBUL",
            "Country", "TR",
            "ZipCode", "34000",
            "Phone", "5555555555",
            "PhoneCountryCode", "90");
        for (Map.Entry<String, Object> e : defaults.entrySet()) {
            if (contact.get(e.getKey()) == null) {
                contact.put(e.getKey(), e.getValue());
            }
        }

        contact.put("FirstName", sanitizePersonName(contact.get("FirstName")));
        contact.put("LastName", sanitizePersonName(contact.get("LastName")));

        if (Php.strlen(Php.trim(contact.get("FirstName"))) == 0) {
            contact.put("FirstName", defaults.get("FirstName"));
        }
        if (Php.strlen(Php.trim(contact.get("LastName"))) == 0) {
            contact.put("LastName", contact.get("FirstName"));
        }
        if (Php.strlen(Php.trim(contact.get("AddressLine1"))) == 0) {
            contact.put("AddressLine1", defaults.get("AddressLine1"));
        }
        if (Php.strlen(Php.trim(contact.get("City"))) == 0) {
            contact.put("City", defaults.get("City"));
        }
        if (Php.strlen(Php.trim(contact.get("State"))) == 0) {
            contact.put("State", contact.get("City"));
        }
        contact.put("Country", normalizeCountryCode(contact.get("Country")));
        if (Php.strlen(Php.trim(contact.get("Country"))) == 0) {
            contact.put("Country", defaults.get("Country"));
        }
        if (Php.strlen(Php.trim(contact.get("ZipCode"))) == 0) {
            contact.put("ZipCode", defaults.get("ZipCode"));
        }

        String[] phone = normalizePhone(contact.get("Phone"), contact.get("PhoneCountryCode"));
        contact.put("Phone", phone[0]);
        contact.put("PhoneCountryCode", phone[1]);
        if (Php.strlen(Php.trim(contact.get("PhoneCountryCode"))) == 0) {
            contact.put("PhoneCountryCode", defaults.get("PhoneCountryCode"));
        }
        if (Php.strlen(Php.trim(contact.get("Phone"))) == 0) {
            contact.put("Phone", defaults.get("Phone"));
        }

        String[] fax = normalizePhone(coalesce(contact.get("Fax"), ""), coalesce(contact.get("FaxCountryCode"), ""));
        contact.put("Fax", fax[0]);
        contact.put("FaxCountryCode", fax[0].isEmpty() ? "" : fax[1]);
        return contact;
    }

    private static final Pattern NAME_STRIP = Pattern.compile("[^\\p{L}\\s'\\-]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private static String sanitizePersonName(Object name) {
        String clean = NAME_STRIP.matcher(str(name)).replaceAll("");
        return Php.trim(MULTI_SPACE.matcher(clean).replaceAll(" "));
    }

    // ---------------------------------------------------------------- helpers

    private static String normalizeHostName(Object hostName) {
        return Php.strtolower(Php.rtrim(Php.trim(hostName), "."));
    }

    private static List<Object> normalizeHostNameList(Object hostNames) {
        List<Object> normalized = new ArrayList<>();
        if (!Php.isArray(hostNames)) {
            return normalized;
        }
        for (Object h : Php.values(hostNames)) {
            if (!(h instanceof String)) {
                continue;
            }
            String n = normalizeHostName(h);
            if (!n.isEmpty() && !normalized.contains(n)) {
                normalized.add(n);
            }
        }
        return normalized;
    }

    /** A PHP associative array as an ordered, mutable String-keyed map (copy). */
    static Map<String, Object> assoc(Object v) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (v instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
        } else if (v instanceof List) {
            List<?> l = (List<?>) v;
            for (int i = 0; i < l.size(); i++) {
                out.put(String.valueOf(i), l.get(i));
            }
        } else if (v != null) {
            out.put("0", v);
        }
        return out;
    }
}
