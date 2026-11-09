/*
 * java-dna example: CheckTransfer
 * Check whether a domain can be transferred in with an auth code.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes CheckTransfer
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class CheckTransfer {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.checkTransfer("example.com", "ABC123XYZ456");
        System.out.println(Json.encodePretty(result));
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? fallback : v;
    }
}

/*
 * Sample output:
 *
 * {
 *     "result": "OK",
 *     "data": {
 *         "TransferAvailabilityStatus": true,
 *         "AuthCodeIsRequired": true,
 *         "AuthCodeIsValid": true,
 *         "UserTransferRequired": false,
 *         "TransferLock": false,
 *         "Message": "Domain can be transferred",
 *         "MessageKey": "TransferAvailable",
 *         "Additional": []
 *     }
 * }
 * When the domain cannot be transferred, "result" is "ERROR" and "error" holds
 * {"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}.
 */
