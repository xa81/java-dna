package io.github.domainreseller.dna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpLayerTest {

    private static DomainNameApi api(FakeTransport t) {
        DomainNameApi api = new DomainNameApi("  reseller-id \n", "\tsecret-key ", false);
        api.setErrorReportingEnabled(false);
        api.setTransport(t);
        api.setZoneMinInterval(0);
        return api;
    }

    @Test
    void sendsAuthHeadersUserAgentAndJson() {
        FakeTransport t = new FakeTransport().respond(200, "{}");
        api(t).enableTheftProtectionLock("example.com");
        HttpTransport.Request r = t.requests.get(0);
        assertEquals("POST", r.getMethod());
        assertEquals("https://api.domainresellerapi.com/api/v1/domains/lock", r.getUrl());
        assertEquals("application/json", r.getHeaders().get("Content-Type"));
        assertEquals("application/json", r.getHeaders().get("Accept"));
        assertEquals("secret-key", r.getHeaders().get("X-API-KEY"), "API key is trimmed");
        assertEquals("reseller-id", r.getHeaders().get("__reseller"), "reseller id is trimmed");
        assertEquals("DomainNameAPI-Java/1.0.0", r.getHeaders().get("User-Agent"));
        assertEquals("{\"domainName\":\"example.com\"}", r.getBody());
        assertEquals(30, r.getTimeoutSeconds());
        assertEquals(10, r.getConnectTimeoutSeconds());
        assertTrue(r.isSslVerify());
    }

    @Test
    void getAndDeleteUsePhpQueryStrings() {
        FakeTransport t = new FakeTransport().respond(200, "{\"items\":[]}").respond(200, "");
        DomainNameApi api = api(t);
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("DomainName", "a b*c");
        filters.put("Flag", true);
        filters.put("Off", false);
        filters.put("Skip", null);
        filters.put("Tags", Arrays.asList("x", "y"));
        api.getList(filters);
        assertEquals("https://api.domainresellerapi.com/api/v1/domains?MaxResultCount=200&SkipCount=0"
            + "&DomainName=a+b%2Ac&Flag=1&Off=0&Tags%5B0%5D=x&Tags%5B1%5D=y", t.requests.get(0).getUrl());
        assertNull(t.requests.get(0).getBody(), "GET has no body");

        api.deleteChildNameServer("example.com", " NS1.Example.com. ");
        assertEquals("https://api.domainresellerapi.com/api/v1/domains/dns/host?domainName=example.com&hostName=ns1.example.com",
            t.requests.get(1).getUrl());
        assertNull(t.requests.get(1).getBody(), "DELETE has no body");
    }

    @Test
    void phpJsonEncodingOnTheWire() {
        FakeTransport t = new FakeTransport().respond(200, "");
        api(t).setForwarding("example.com", "https://ex.org/ü");
        assertEquals("{\"domainName\":\"example.com\",\"redirectAddress\":\"https:\\/\\/ex.org\\/\\u00fc\",\"forwardType\":\"Standard\"}",
            t.requests.get(0).getBody());
    }

    @Test
    void transferNeverSendsContacts() {
        FakeTransport t = new FakeTransport().respond(200, "{\"status\":\"Active\"}");
        api(t).transfer("example.com", "Ab1!", 1);
        assertEquals("{\"domainName\":\"example.com\",\"authCode\":\"Ab1!\",\"period\":1,\"contacts\":[]}",
            t.requests.get(0).getBody());
    }

    @Test
    void privacyStatusAsTextAndNoReasonOnTheWire() {
        FakeTransport t = new FakeTransport().respond(200, "").respond(200, "");
        DomainNameApi api = api(t);
        assertEquals(true, ((Map<?, ?>) api.modifyPrivacyProtectionStatus("example.com", "on").get("data")).get("PrivacyProtectionStatus"));
        assertEquals(false, ((Map<?, ?>) api.modifyPrivacyProtectionStatus("example.com", "no", "Customer asked").get("data")).get("PrivacyProtectionStatus"));
        assertEquals("{\"domainName\":\"example.com\",\"privacyStatus\":true}", t.requests.get(0).getBody());
        assertEquals("{\"domainName\":\"example.com\",\"privacyStatus\":false}", t.requests.get(1).getBody());
    }

    @Test
    void zoneWriteReportsFailedApply() {
        FakeTransport t = new FakeTransport().respond(200, "").respond(429, "");
        Map<String, Object> r = api(t).addResourceRecord("example.com", "www", "a", "192.0.2.5", 999999);
        assertEquals("ERROR", r.get("result"));
        assertEquals("API_429", ((Map<?, ?>) r.get("error")).get("Code"));

        FakeTransport ok = new FakeTransport().respond(200, "");
        Map<?, ?> data = (Map<?, ?>) api(ok).addResourceRecord("example.com", "www", "a", "192.0.2.5", 999999, false).get("data");
        assertEquals("A", data.get("Type"), "upper-cased type that was sent");
        assertEquals(86400, data.get("TTL"), "clamped TTL that was sent");
    }

    @Test
    void forwardingNotSetVersusMissingDomain() {
        String notSet = "{\"error\":{\"code\":\"Dna.DomainService:Domain:10061\",\"message\":\"Redirect information not found\"}}";
        String missing = "{\"error\":{\"code\":\"Dna.DomainService:Domain:10007\",\"message\":\"The domain name you requested could not be found.\"}}";
        FakeTransport t = new FakeTransport().respond(404, notSet).respond(404, missing).respond(404, notSet).respond(200, "");
        DomainNameApi api = api(t);
        assertEquals("OK", api.getForwarding("example.com").get("result"));
        assertEquals("ERROR", api.getForwarding("missing.com").get("result"));
        assertEquals(false, ((Map<?, ?>) api.deleteForwarding("example.com").get("data")).get("Removed"));
        assertEquals(true, ((Map<?, ?>) api.deleteForwarding("example.com").get("data")).get("Removed"));
    }

    @Test
    void testModeAndServiceUrl() {
        assertEquals(DomainNameApi.URL_PROD, new DomainNameApi("r", "k").getServiceUrl());
        assertEquals(DomainNameApi.URL_OTE, new DomainNameApi("r", "k", true).getServiceUrl());
        for (String on : new String[]{"true", "1", "on", "yes", " TRUE ", "Yes"}) {
            assertEquals(DomainNameApi.URL_OTE, new DomainNameApi("r", "k", on).getServiceUrl(), on);
        }
        for (String off : new String[]{"false", "0", "off", "no", "", "maybe"}) {
            assertEquals(DomainNameApi.URL_PROD, new DomainNameApi("r", "k", off).getServiceUrl(), off);
        }
        String nullFlag = null;
        assertEquals(DomainNameApi.URL_PROD, new DomainNameApi("r", "k", nullFlag).getServiceUrl());

        FakeTransport t = new FakeTransport().respond(200, "{}");
        DomainNameApi api = api(t);
        api.setServiceUrl("http://localhost:9999/api/v1");
        api.getDetails("example.com");
        assertEquals("http://localhost:9999/api/v1/domains/info?DomainName=example.com", t.requests.get(0).getUrl());
    }

    @Test
    void sslVerificationFlagReachesTransport() {
        FakeTransport t = new FakeTransport().respond(200, "{}");
        DomainNameApi api = api(t);
        api.setSslVerification(false);
        api.getResellerDetails();
        assertFalse(t.requests.get(0).isSslVerify());
    }

    @Test
    void diagnosticsAreRecorded() {
        FakeTransport t = new FakeTransport().respond(200, "{\"status\":\"Active\",\"id\":5}");
        DomainNameApi api = api(t);
        api.getDetails("example.com");
        assertEquals("request:GET domains/info", api.getLastFunction());
        assertEquals("https://api.domainresellerapi.com/api/v1/domains/info", api.getRequestData().get("url"));
        assertEquals("GET", api.getRequestData().get("method"));
        assertEquals(Collections.singletonMap("DomainName", "example.com"), api.getRequestData().get("payload"));
        assertEquals(200, api.getResponseHeaders().get("http_code"));
        assertEquals("Active", ((Map<?, ?>) api.getResponseData()).get("status"));
        assertEquals(5, ((Map<?, ?>) api.getParsedResponseData()).get("id"));

        t.respond(502, "<html>bad</html>");
        api.getDetails("example.com");
        assertEquals("<html>bad</html>", ((Map<?, ?>) api.getResponseData()).get("raw_response"));
    }

    @Test
    void zoneCallsArePacedOtherCallsAreNot() {
        FakeTransport t = new FakeTransport();
        for (int i = 0; i < 4; i++) {
            t.respond(200, "[]");
        }
        DomainNameApi api = api(t);
        api.setZoneMinInterval(0.3);
        long start = System.nanoTime();
        api.getResourceRecords("example.com");
        api.getResourceRecords("example.com");
        long zoneMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(zoneMs >= 280, "second zone call waited, took " + zoneMs + " ms");

        start = System.nanoTime();
        api.getDetails("example.com");
        api.getDetails("example.com");
        long otherMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(otherMs < 250, "non-zone calls are not paced, took " + otherMs + " ms");
    }

    @Test
    void jdkTransportOnTheWire() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        List<String> seen = new ArrayList<>();
        server.createContext("/", ex -> {
            String body;
            try (InputStream in = ex.getRequestBody()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            seen.add(ex.getRequestMethod() + " " + ex.getRequestURI() + " ua=" + ex.getRequestHeaders().getFirst("User-Agent")
                + " key=" + ex.getRequestHeaders().getFirst("X-API-KEY")
                + " reseller=" + ex.getRequestHeaders().getFirst("__reseller") + " body=" + body);
            byte[] out;
            int status;
            if (ex.getRequestURI().getPath().contains("/redirect/")) {
                status = 302;
                out = new byte[0];
                ex.getResponseHeaders().add("Location", "/login");
            } else {
                status = 200;
                out = "{\"resellerId\":\"r-1\",\"resellerName\":\"n\",\"usdBalance\":1.5,\"tryBalance\":2}".getBytes(StandardCharsets.UTF_8);
            }
            ex.sendResponseHeaders(status, out.length == 0 ? -1 : out.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(out);
            }
        });
        server.start();
        try {
            DomainNameApi api = new DomainNameApi("r-1", "k-1");
            api.setErrorReportingEnabled(false);
            api.setServiceUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1");
            Map<String, Object> details = api.getResellerDetails();
            assertEquals("OK", details.get("result"));
            assertEquals("1.50", details.get("balance"));
            assertEquals("GET /api/v1/deposit/accounts/me ua=DomainNameAPI-Java/1.0.0 key=k-1 reseller=r-1 body=", seen.get(0));

            api.deleteForwarding("example.com");
            assertEquals("DELETE /api/v1/domains/forwards?DomainName=example.com ua=DomainNameAPI-Java/1.0.0 key=k-1 reseller=r-1 body=",
                seen.get(1));

            api.setServiceUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1/redirect");
            Map<String, Object> redirected = api.getResellerDetails();
            assertEquals("ERROR", redirected.get("result"));
            assertEquals("Invalid API credentials", ((Map<?, ?>) redirected.get("error")).get("Message"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void connectionRefusedIsAnError() throws IOException {
        int port;
        try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
            port = s.getLocalPort();
        }
        DomainNameApi api = new DomainNameApi("r", "k");
        api.setErrorReportingEnabled(false);
        api.setServiceUrl("http://127.0.0.1:" + port + "/api/v1");
        Map<String, Object> r = api.enableTheftProtectionLock("example.com");
        assertEquals("ERROR", r.get("result"));
        Map<?, ?> err = (Map<?, ?>) r.get("error");
        assertEquals("CURL_7", err.get("Code"));
        assertTrue(((String) err.get("Message")).startsWith("Curl error during request: "), (String) err.get("Message"));
    }
}
