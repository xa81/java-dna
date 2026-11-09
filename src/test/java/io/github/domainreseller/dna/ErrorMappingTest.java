package io.github.domainreseller.dna;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of DNARest::request() error mapping. */
class ErrorMappingTest {

    private static DomainNameApi api(FakeTransport t) {
        DomainNameApi api = new DomainNameApi("r", "k");
        api.setErrorReportingEnabled(false);
        api.setTransport(t);
        return api;
    }

    private static Map<?, ?> error(Map<String, Object> result) {
        assertEquals("ERROR", result.get("result"), Json.encode(result));
        return (Map<?, ?>) result.get("error");
    }

    @Test
    void timeoutIsAnErrorNotAnEmptySuccess() {
        FakeTransport t = new FakeTransport().fail(28, "Operation timed out after 30000 milliseconds");
        DomainNameApi api = api(t);
        Map<?, ?> err = error(api.modifyNameServer("example.com", java.util.Arrays.asList("ns1.example.net")));
        assertEquals("API_28", err.get("Code"));
        assertEquals("Could not reach the DomainNameAPI service: the connection timed out. "
            + "(Operation timed out after 30000 milliseconds)", err.get("Message"));
        assertEquals(err.get("Message"), err.get("Details"));
        assertEquals("CURL_28", ((Map<?, ?>) api.getParsedResponseData()).get("Code"));
        assertEquals(0, api.getResponseHeaders().get("http_code"));
    }

    @Test
    void otherTransportFailures() {
        FakeTransport t = new FakeTransport().fail(6, "Could not resolve host: api.domainresellerapi.com");
        DomainNameApi api = api(t);
        Map<?, ?> err = error(api.getDetails("example.com"));
        assertEquals("API_6", err.get("Code"));
        assertEquals("Curl error during request: Could not resolve host: api.domainresellerapi.com", err.get("Message"));
        assertEquals("CURL_6", ((Map<?, ?>) api.getParsedResponseData()).get("Code"));
    }

    @Test
    void transportFailureKeepsBalanceEnvelope() {
        FakeTransport t = new FakeTransport().fail(28, "timeout");
        Map<String, Object> r = api(t).getCurrentBalance("EUR");
        assertEquals("FAILED", r.get("OperationResult"));
        assertEquals("API_28", r.get("ErrorCode"));
        assertEquals("0.00", r.get("Balance"));
        assertEquals(3, r.get("CurrencyId"));
    }

    @Test
    void redirectMeansBadCredentials() {
        Map<?, ?> err = error(api(new FakeTransport().respond(301, "")).getResellerDetails());
        assertEquals("API_301", err.get("Code"));
        assertEquals("Invalid API credentials", err.get("Message"));
        assertEquals("Authentication failed. Check your API key and reseller ID.", err.get("Details"));
    }

    @Test
    void emptyErrorBodyGetsStatusMessage() {
        assertEquals("HTTP 503 - Service Unavailable",
            error(api(new FakeTransport().respond(503, "")).getDetails("x.com")).get("Message"));
        assertEquals("HTTP 418 - Client Error",
            error(api(new FakeTransport().respond(418, "  ")).getDetails("x.com")).get("Message"));
        assertEquals("HTTP 599 - Server Error",
            error(api(new FakeTransport().respond(599, "")).getDetails("x.com")).get("Message"));
    }

    @Test
    void topLevelMessageAndCode() {
        Map<?, ?> err = error(api(new FakeTransport().respond(400,
            "{\"message\":\"Domain is not in updateable status\",\"code\":\"X1\",\"details\":\"locked\"}")).renew("x.com", 1));
        assertEquals("Domain is not in updateable status", err.get("Message"));
        assertEquals("locked", err.get("Details"));
    }

    @Test
    void boilerplateWithoutUsefulDetailsIsSynthesised() {
        Map<?, ?> err = error(api(new FakeTransport().respond(400,
            "{\"error\":{\"code\":\"Dna.DomainService:Dns:10024\",\"message\":\"Exception of type 'Volo.Abp.BusinessException' was thrown.\","
                + "\"details\":\"Exception of type 'Volo.Abp.BusinessException' was thrown.\"}}")).getDetails("x.com"));
        assertEquals("The operation could not be completed by the registry (error: Dna.DomainService:Dns:10024). "
            + "Please try again or contact support.", err.get("Message"));
    }

    @Test
    void messageEqualToCodeUsesDetails() {
        Map<?, ?> err = error(api(new FakeTransport().respond(400,
            "{\"error\":{\"code\":\"E42\",\"message\":\"E42\",\"details\":\"Dna  Host already exists\"}}")).getDetails("x.com"));
        assertEquals("Host already exists", err.get("Message"));
        assertEquals("Dna  Host already exists", err.get("Details"));
    }

    @Test
    void validationErrorsAreFlattened() {
        String body = "{\"error\":{\"code\":null,\"message\":\"Validation failed.\",\"validationErrors\":["
            + "{\"message\":\"'PostalCode' is required\",\"members\":[\"$.administrativeContact.postalCode\",\"$.technicalContact.postalCode\"]},"
            + "{\"message\":\"'PostalCode' is required\",\"members\":[\"$.administrativeContact.postalCode\",\"$.technicalContact.postalCode\"]},"
            + "{\"members\":[\"$.tldAttributes.TRABISCOUNTRYID\"]},"
            + "{\"message\":\"Plain message\"},"
            + "\"A bare string\"]}}";
        Map<?, ?> err = error(api(new FakeTransport().respond(400, body)).getDetails("x.com"));
        assertEquals("Validation failed.", err.get("Message"));
        assertEquals("Admin PostalCode, Tech PostalCode - 'PostalCode' is required | TldAttributes TRABISCOUNTRYID"
            + " | Plain message | A bare string", err.get("Details"));
    }

    @Test
    void htmlBodyIsMessageAndDetails() {
        Map<?, ?> err = error(api(new FakeTransport().respond(504, "<html>Gateway Timeout</html>")).getTldList());
        assertEquals("API_504", err.get("Code"));
        assertEquals("<html>Gateway Timeout</html>", err.get("Message"));
        assertEquals("<html>Gateway Timeout</html>", err.get("Details"));
    }

    @Test
    void emptySuccessBodyIsResponseErrorForDomainInfo() {
        Map<?, ?> err = error(api(new FakeTransport().respond(200, "")).transfer("x.com", "epp", 1));
        assertEquals("RESPONSE", err.get("Code"));
        assertTrue(((String) err.get("Message")).contains("Response is empty"));
    }

    @Test
    void renewWithoutExpirationIsError() {
        Map<?, ?> err = error(api(new FakeTransport().respond(200, "{\"success\":true}")).renew("x.com", 1));
        assertEquals("DOMAIN_RENEW", err.get("Code"));
    }
}
