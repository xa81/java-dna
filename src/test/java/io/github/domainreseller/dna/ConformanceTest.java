package io.github.domainreseller.dna;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs every php-dna conformance fixture (src/test/resources/conformance) through
 * the public facade with a fake transport and compares requests and output with
 * what php-dna produced.
 */
class ConformanceTest {

    private static final String RESELLER = "00000000-0000-4000-8000-000000000001";
    private static final String API_KEY = "conformance-api-key";
    private static final String BASE = "http://conformance.test/api/v1";

    static List<Path> fixtureFiles() throws IOException, URISyntaxException {
        URL dir = ConformanceTest.class.getClassLoader().getResource("conformance");
        if (dir == null) {
            throw new IllegalStateException("conformance fixtures not on the test classpath");
        }
        try (Stream<Path> s = Files.list(Paths.get(dir.toURI()))) {
            return s.filter(p -> p.toString().endsWith(".json")).sorted().collect(Collectors.toList());
        }
    }

    @Test
    void allFixturesArePresent() throws Exception {
        assertEquals(126, fixtureFiles().size(), "php-dna ships 126 conformance fixtures");
    }

    @TestFactory
    Stream<DynamicTest> fixtures() throws Exception {
        return fixtureFiles().stream().map(p -> DynamicTest.dynamicTest(
            p.getFileName().toString().replaceAll("\\.json$", ""), () -> run(p)));
    }

    @SuppressWarnings("unchecked")
    private static void run(Path file) throws IOException {
        Map<String, Object> fx = (Map<String, Object>) Json.parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

        FakeTransport transport = new FakeTransport();
        for (Object r : (List<Object>) fx.get("responses")) {
            Map<String, Object> resp = (Map<String, Object>) r;
            transport.respond(((Number) resp.get("status")).intValue(), (String) resp.get("body"));
        }

        DomainNameApi api = new DomainNameApi(RESELLER, API_KEY, false);
        api.setErrorReportingEnabled(false);
        api.setServiceUrl(BASE);
        api.setZoneMinInterval(0);
        api.setTransport(transport);

        Object result = invoke(api, (String) fx.get("call"), (List<Object>) fx.get("args"));

        // Requests: method, path, query, body and the two auth headers, in order.
        List<Object> expectedRequests = (List<Object>) fx.get("requests");
        assertEquals(expectedRequests.size(), transport.requests.size(),
            "number of requests; sent: " + describe(transport.requests));
        for (int i = 0; i < expectedRequests.size(); i++) {
            Map<String, Object> exp = (Map<String, Object>) expectedRequests.get(i);
            HttpTransport.Request got = transport.requests.get(i);
            String label = "request #" + (i + 1) + " ";
            assertEquals(exp.get("method"), got.getMethod(), label + "method");

            String url = got.getUrl();
            if (!url.startsWith(BASE + "/")) {
                fail(label + "url outside the service URL: " + url);
            }
            String rest = url.substring(BASE.length() + 1);
            int q = rest.indexOf('?');
            String path = q >= 0 ? rest.substring(0, q) : rest;
            Map<String, Object> query = parseQuery(q >= 0 ? rest.substring(q + 1) : "");
            assertEquals(exp.get("path"), path, label + "path");
            assertNull(JsonCompare.diff(exp.get("query"), query, "query", Collections.emptySet()), label + "query");

            Object body = got.getBody() == null || got.getBody().isEmpty() ? null : Json.parse(got.getBody());
            assertNull(JsonCompare.diff(exp.get("body"), body, "body", Collections.emptySet()), label + "body");

            Map<String, Object> headers = (Map<String, Object>) exp.get("headers");
            assertEquals(headers.get("X-API-KEY"), got.getHeaders().get("X-API-KEY"), label + "X-API-KEY");
            assertEquals(headers.get("__reseller"), got.getHeaders().get("__reseller"), label + "__reseller");
            assertEquals(DomainNameApi.USER_AGENT, got.getHeaders().get("User-Agent"), label + "User-Agent");
        }

        // Output after a JSON round trip.
        Object actual = Json.parse(Json.encode(result));
        Set<String> ignore = new HashSet<>();
        for (Object o : (List<Object>) fx.get("ignore")) {
            ignore.add((String) o);
        }
        String diff = JsonCompare.diff(fx.get("output"), actual, "", ignore);
        assertNull(diff, () -> "output differs: " + diff + "\nactual: " + Json.encode(result));
    }

    private static String describe(List<HttpTransport.Request> requests) {
        List<String> out = new ArrayList<>();
        for (HttpTransport.Request r : requests) {
            out.add(r.getMethod() + " " + r.getUrl());
        }
        return out.toString();
    }

    private static Map<String, Object> parseQuery(String q) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (q.isEmpty()) {
            return out;
        }
        for (String pair : q.split("&")) {
            int eq = pair.indexOf('=');
            String k = URLDecoder.decode(eq >= 0 ? pair.substring(0, eq) : pair, StandardCharsets.UTF_8);
            String v = eq >= 0 ? URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8) : "";
            out.put(k, v);
        }
        return out;
    }

    // ------------------------------------------------------------ dispatch

    @SuppressWarnings("unchecked")
    static Object invoke(DomainNameApi api, String call, List<Object> a) {
        int n = a.size();
        switch (call) {
            case "getResellerDetails":
                return api.getResellerDetails();
            case "getCurrentBalance":
                return n == 0 ? api.getCurrentBalance() : api.getCurrentBalance(s(a, 0));
            case "checkAvailability":
                return n == 3 ? api.checkAvailability(strList(a, 0), strList(a, 1), i(a, 2))
                    : api.checkAvailability(strList(a, 0), strList(a, 1), i(a, 2), s(a, 3));
            case "getList":
                return n == 0 ? api.getList() : api.getList(m(a, 0));
            case "getTldList":
                return n == 0 ? api.getTldList() : api.getTldList(i(a, 0));
            case "getDetails":
                return api.getDetails(s(a, 0));
            case "syncFromRegistry":
                return api.syncFromRegistry(s(a, 0));
            case "getContacts":
                return api.getContacts(s(a, 0));
            case "saveContacts":
                return api.saveContacts(s(a, 0), m(a, 1));
            case "modifyNameServer":
                return a.get(1) instanceof Map ? api.modifyNameServer(s(a, 0), m(a, 1))
                    : api.modifyNameServer(s(a, 0), strList(a, 1));
            case "enableTheftProtectionLock":
                return api.enableTheftProtectionLock(s(a, 0));
            case "disableTheftProtectionLock":
                return api.disableTheftProtectionLock(s(a, 0));
            case "modifyPrivacyProtectionStatus":
                return n == 2 ? api.modifyPrivacyProtectionStatus(s(a, 0), b(a, 1))
                    : api.modifyPrivacyProtectionStatus(s(a, 0), b(a, 1), s(a, 2));
            case "renew":
                return api.renew(s(a, 0), i(a, 1));
            case "registerWithContactInfo":
                switch (n) {
                    case 3: return api.registerWithContactInfo(s(a, 0), i(a, 1), m(a, 2));
                    case 4: return api.registerWithContactInfo(s(a, 0), i(a, 1), m(a, 2), strList(a, 3));
                    case 5: return api.registerWithContactInfo(s(a, 0), i(a, 1), m(a, 2), strList(a, 3), b(a, 4));
                    case 6: return api.registerWithContactInfo(s(a, 0), i(a, 1), m(a, 2), strList(a, 3), b(a, 4), b(a, 5));
                    default: return api.registerWithContactInfo(s(a, 0), i(a, 1), m(a, 2), strList(a, 3), b(a, 4), b(a, 5), m(a, 6));
                }
            case "addChildNameServer":
                return api.addChildNameServer(s(a, 0), s(a, 1), s(a, 2));
            case "deleteChildNameServer":
                return api.deleteChildNameServer(s(a, 0), s(a, 1));
            case "modifyChildNameServer":
                return api.modifyChildNameServer(s(a, 0), s(a, 1), s(a, 2));
            case "transfer":
                return n == 3 ? api.transfer(s(a, 0), s(a, 1), i(a, 2))
                    : api.transfer(s(a, 0), s(a, 1), i(a, 2), m(a, 3));
            case "cancelTransfer":
                return api.cancelTransfer(s(a, 0));
            case "approveTransfer":
                return api.approveTransfer(s(a, 0));
            case "rejectTransfer":
                return api.rejectTransfer(s(a, 0));
            case "checkTransfer":
                return api.checkTransfer(s(a, 0), s(a, 1));
            case "getResourceRecords":
                return api.getResourceRecords(s(a, 0));
            case "addResourceRecord":
                if (a.get(3) instanceof List) {
                    List<String> c = strList(a, 3);
                    return n == 4 ? api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), c)
                        : n == 5 ? api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), c, i(a, 4))
                        : api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), c, i(a, 4), b(a, 5));
                }
                return n == 4 ? api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3))
                    : n == 5 ? api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), i(a, 4))
                    : api.addResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), i(a, 4), b(a, 5));
            case "editResourceRecord":
                if (a.get(4) instanceof List) {
                    List<String> c = strList(a, 4);
                    return n == 5 ? api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), c)
                        : n == 6 ? api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), c, i(a, 5))
                        : api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), c, i(a, 5), b(a, 6));
                }
                return n == 5 ? api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4))
                    : n == 6 ? api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4), i(a, 5))
                    : api.editResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4), i(a, 5), b(a, 6));
            case "deleteResourceRecord":
                return n == 4 ? api.deleteResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3))
                    : api.deleteResourceRecord(s(a, 0), s(a, 1), s(a, 2), s(a, 3), b(a, 4));
            case "applyResourceRecords":
                return api.applyResourceRecords(s(a, 0));
            case "setResourceRecordSet":
                return n == 4 ? api.setResourceRecordSet(s(a, 0), s(a, 1), s(a, 2), strList(a, 3))
                    : n == 5 ? api.setResourceRecordSet(s(a, 0), s(a, 1), s(a, 2), strList(a, 3), i(a, 4))
                    : api.setResourceRecordSet(s(a, 0), s(a, 1), s(a, 2), strList(a, 3), i(a, 4), b(a, 5));
            case "deleteResourceRecordSet":
                return n == 3 ? api.deleteResourceRecordSet(s(a, 0), s(a, 1), s(a, 2))
                    : n == 4 ? api.deleteResourceRecordSet(s(a, 0), s(a, 1), s(a, 2), b(a, 3))
                    : api.deleteResourceRecordSet(s(a, 0), s(a, 1), s(a, 2), b(a, 3), s(a, 4));
            case "addResourceRecordValue":
                return n == 4 ? api.addResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3))
                    : n == 5 ? api.addResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), nullableInt(a, 4))
                    : api.addResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), nullableInt(a, 4), b(a, 5));
            case "removeResourceRecordValue":
                return n == 4 ? api.removeResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3))
                    : api.removeResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), b(a, 4));
            case "replaceResourceRecordValue":
                return n == 5 ? api.replaceResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4))
                    : n == 6 ? api.replaceResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4), nullableInt(a, 5))
                    : api.replaceResourceRecordValue(s(a, 0), s(a, 1), s(a, 2), s(a, 3), s(a, 4), nullableInt(a, 5), b(a, 6));
            case "getForwarding":
                return api.getForwarding(s(a, 0));
            case "setForwarding":
                return n == 2 ? api.setForwarding(s(a, 0), s(a, 1)) : api.setForwarding(s(a, 0), s(a, 1), s(a, 2));
            case "deleteForwarding":
                return api.deleteForwarding(s(a, 0));
            case "isTrTLD":
                return api.isTrTLD(s(a, 0));
            case "validateContact":
                return api.validateContact(m(a, 0));
            default:
                throw new IllegalArgumentException("Unknown call in fixture: " + call);
        }
    }

    private static String s(List<Object> a, int i) {
        Object v = a.get(i);
        return v == null ? null : v.toString();
    }

    private static int i(List<Object> a, int i) {
        return ((Number) a.get(i)).intValue();
    }

    private static Integer nullableInt(List<Object> a, int i) {
        Object v = a.get(i);
        return v == null ? null : ((Number) v).intValue();
    }

    private static boolean b(List<Object> a, int i) {
        return Boolean.TRUE.equals(a.get(i));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> m(List<Object> a, int i) {
        Object v = a.get(i);
        if (v instanceof Map) {
            return (Map<String, Object>) v;
        }
        return new LinkedHashMap<>(); // php [] (empty array) arrives as an empty JSON list
    }

    private static List<String> strList(List<Object> a, int i) {
        List<String> out = new ArrayList<>();
        for (Object o : (List<?>) a.get(i)) {
            out.add(o == null ? null : o.toString());
        }
        return out;
    }
}
