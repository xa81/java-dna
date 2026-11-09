package io.github.domainreseller.dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelemetryTest {

    private static final String DSN = "https://publickey@sentry.example.test/42";

    @Test
    void defaultDsnIsEmptyAndTelemetryIsANoOp() {
        assertEquals("", Telemetry.DEFAULT_SENTRY_DSN);
        DomainNameApi api = new DomainNameApi("r", "k");
        assertEquals("", api.engine().telemetry.getDsn());
        assertTrue(api.engine().telemetry.isEnabled(), "on by default");
        assertNull(api.engine().telemetry.buildErrorEvent(api.engine(), "boom", 500, new StackTraceElement[0]));
    }

    @Test
    void parsesDsn() {
        Telemetry.Dsn d = Telemetry.parseDsn("https://pub:sec@sentry.example.test/7");
        assertNotNull(d);
        assertEquals("pub", d.publicKey);
        assertEquals("sec", d.secretKey);
        assertEquals("https://sentry.example.test/api/7/store/", d.storeUrl());
        assertNull(Telemetry.parseDsn(""));
        assertNull(Telemetry.parseDsn("https://sentry.example.test/7"));
        assertNull(Telemetry.parseDsn("not a dsn"));
    }

    @Test
    void masksCredentialsOnly() {
        Map<String, Object> data = Php.map(
            "url", "https://api/x",
            "payload", Php.map(
                "domainName", "example.com",
                "authCode", "Ab1!",
                "eppCode", "",
                "apiKey", "k",
                "api-key", "k",
                "Password", "p",
                "secret", null,
                "token", "t",
                "contacts", Php.list(Php.map("firstName", "Ada", "eMail", "ada@example.com"))));
        @SuppressWarnings("unchecked")
        Map<String, Object> masked = (Map<String, Object>) Telemetry.mask(data);
        Map<?, ?> p = (Map<?, ?>) masked.get("payload");
        assertEquals("https://api/x", masked.get("url"));
        assertEquals("example.com", p.get("domainName"));
        assertEquals("***", p.get("authCode"));
        assertEquals("", p.get("eppCode"), "empty values stay empty");
        assertEquals("***", p.get("apiKey"));
        assertEquals("***", p.get("api-key"));
        assertEquals("***", p.get("Password"));
        assertNull(p.get("secret"));
        assertEquals("***", p.get("token"));
        assertEquals("ada@example.com", ((Map<?, ?>) ((List<?>) p.get("contacts")).get(0)).get("eMail"));
        assertEquals("Ab1!", ((Map<?, ?>) data.get("payload")).get("authCode"), "input is not mutated");
    }

    @Test
    void ignoredErrors() {
        assertTrue(Telemetry.isIgnored("The domain name you requested could not be found. Please check"));
        assertTrue(Telemetry.isIgnored("Invalid API credentials"));
        assertFalse(Telemetry.isIgnored("An internal error has occurred."));
    }

    @Test
    void classificationNormalisesVolatileParts() {
        DomainNameApi api = new DomainNameApi("00000000-0000-4000-8000-000000000001", "k");
        DNARest ctx = api.engine();
        ctx.lastFunction = "request:POST domains/lock";
        ctx.lastRequest = Php.map("url", "u", "method", "POST", "payload", Php.map("domainName", "example.com"));
        Telemetry.Classification c = ctx.telemetry.classify(ctx,
            "Your current IP address (203.0.113.9) is not authorized for 'example.com' 12345", 403);
        assertEquals("API_403: Your current IP address ({ip}) is not authorized for {v} {n}", c.value);
        assertEquals("203.0.113.9", c.tags.get("ip_value"));
        assertEquals("example.com", c.tags.get("domain"));
        assertEquals("00000000-0000-4000-8000-000000000001", c.tags.get("reseller"));
        assertEquals("REST", c.tags.get("api_type"));
        assertEquals("403", c.tags.get("status_code"));
        assertEquals(java.util.Arrays.asList("dna", "DomainNameAPI-Java", "request:POST domains/lock", c.value), c.fingerprint);

        assertEquals("API_500", ctx.telemetry.classify(ctx, "", 500).value);
    }

    @Test
    void traceStatus() {
        assertEquals("ok", Telemetry.mapTraceStatus(true, null));
        assertEquals("unauthenticated", Telemetry.mapTraceStatus(false, "API_401"));
        assertEquals("not_found", Telemetry.mapTraceStatus(false, "API_404"));
        assertEquals("deadline_exceeded", Telemetry.mapTraceStatus(false, "API_504"));
        assertEquals("internal_error", Telemetry.mapTraceStatus(false, "API_500"));
        assertEquals("invalid_argument", Telemetry.mapTraceStatus(false, "API_422"));
        assertEquals("unknown", Telemetry.mapTraceStatus(false, "CREDENTIALS"));
    }

    @Test
    void errorEventCarriesApplicationVersionAndMaskedRequest() throws Exception {
        List<String[]> posts = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);
        DomainNameApi api = new DomainNameApi("r-1", "k-1");
        api.setTelemetryDsn(DSN);
        api.engine().telemetry.setIpResolver(() -> "203.0.113.1");
        api.engine().telemetry.setSender((url, json, auth) -> {
            posts.add(new String[]{url, json, auth});
            latch.countDown();
        });
        api.setTransport(new FakeTransport().respond(500,
            "{\"error\":{\"code\":\"X\",\"message\":\"An internal error has occurred.\"}}"));
        api.checkTransfer("example.com", "secret-epp");

        assertTrue(latch.await(5, TimeUnit.SECONDS), "event posted");
        String[] post = posts.get(0);
        assertEquals("https://sentry.example.test/api/42/store/", post[0]);
        assertTrue(post[2].startsWith("Sentry sentry_version=7, sentry_client=java-dna/1.0.0, sentry_key=publickey"), post[2]);
        @SuppressWarnings("unchecked")
        Map<String, Object> event = (Map<String, Object>) Json.parse(post[1]);
        Map<?, ?> tags = (Map<?, ?>) event.get("tags");
        assertEquals("DomainNameAPI-Java", tags.get("application"));
        assertEquals("1.0.0", tags.get("library_version"));
        assertEquals("1.0.0", event.get("release"));
        assertEquals("production", event.get("environment"));
        assertEquals("example.com", tags.get("domain"));
        assertEquals("203.0.113.1", tags.get("ip_address"));
        assertEquals("API_500", tags.get("api_code"));
        Map<?, ?> payload = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) event.get("extra")).get("request_data")).get("payload");
        assertEquals("***", payload.get("authCode"));
        assertEquals("example.com", payload.get("domainName"));
        assertFalse(post[1].contains("secret-epp"));
        assertFalse(post[1].contains("k-1"), "API key never leaves the client");
    }

    @Test
    void minimalModeDropsRequestDataAndDomain() {
        DomainNameApi api = new DomainNameApi("r-1", "k-1", true);
        api.setTelemetryDsn(DSN);
        api.setTelemetryMinimal(true);
        DNARest ctx = api.engine();
        ctx.lastFunction = "request:GET domains/info";
        ctx.lastRequest = Php.map("url", "https://ote.domainresellerapi.com/api/v1/domains/info", "method", "GET",
            "payload", Php.map("domainName", "example.com"));
        ctx.lastResponseHeaders = Php.map("http_code", 500);
        Map<String, Object> event = ctx.telemetry.buildErrorEvent(ctx, "boom", 500, new Throwable().getStackTrace());
        assertNotNull(event);
        Map<?, ?> tags = (Map<?, ?>) event.get("tags");
        assertFalse(tags.containsKey("domain"));
        assertFalse(tags.containsKey("ip_address"));
        assertFalse(tags.containsKey("vhost_user"));
        assertFalse(tags.containsKey("url"));
        assertEquals("ote", event.get("environment"));
        Map<?, ?> extra = (Map<?, ?>) event.get("extra");
        assertEquals(Php.map("method", "GET", "endpoint", "/api/v1/domains/info"), extra.get("request_data"));
        assertEquals(Php.map("http_code", 500), extra.get("response_data"));
    }

    @Test
    void disabledOrIgnoredSendsNothing() {
        DomainNameApi api = new DomainNameApi("r", "k");
        api.setTelemetryDsn(DSN);
        DNARest ctx = api.engine();
        assertNull(ctx.telemetry.buildErrorEvent(ctx, "Domain not found", 404, new StackTraceElement[0]));
        api.setErrorReportingEnabled(false);
        assertNull(ctx.telemetry.buildErrorEvent(ctx, "boom", 500, new StackTraceElement[0]));
    }

    @Test
    void performanceEvent() {
        DomainNameApi api = new DomainNameApi("r", "k");
        api.setTelemetryDsn(DSN);
        DNARest ctx = api.engine();
        ctx.lastFunction = "request:GET domains/info";
        ctx.lastRequest = Php.map("url", "https://api.domainresellerapi.com/api/v1/domains/info", "method", "GET",
            "payload", Php.map());
        ctx.lastResponseHeaders = Php.map("http_code", 200);
        Map<String, Object> ev = ctx.telemetry.buildPerformanceEvent(ctx, 12.5, 1000L, 1012L);
        assertEquals("transaction", ev.get("type"));
        assertEquals("API.request:GET domains/info", ev.get("transaction"));
        Map<?, ?> span = (Map<?, ?>) ((List<?>) ev.get("spans")).get(0);
        assertEquals("GET https://api.domainresellerapi.com", span.get("description"));
        assertEquals("ok", span.get("status"));
        assertEquals("DomainNameAPI-Java", ((Map<?, ?>) ev.get("tags")).get("application"));
        assertEquals("1970-01-01T00:00:01.000Z", ev.get("start_timestamp"));
    }
}
