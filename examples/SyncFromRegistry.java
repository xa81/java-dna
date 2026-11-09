/*
 * java-dna example: SyncFromRegistry
 * Refresh a domain from the registry (same shape as getDetails).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes SyncFromRegistry
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class SyncFromRegistry {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.syncFromRegistry("example.com");
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
 *     "data": {
 *         "ID": 123456,
 *         "Status": "Active",
 *         "DomainName": "example.com",
 *         "AuthCode": "ABC123XYZ456DEF789GHI012JKL345MNO",
 *         "LockStatus": "true",
 *         "PrivacyProtectionStatus": "false",
 *         "IsChildNameServer": "false",
 *         "Contacts": {
 *             "Billing": {"ID": "DNA-B-4"},
 *             "Technical": {"ID": "DNA-T-3"},
 *             "Administrative": {"ID": "DNA-A-2"},
 *             "Registrant": {"ID": "DNA-R-1"}
 *         },
 *         "Dates": {
 *             "Start": "2022-09-09T00:00:00",
 *             "Expiration": "2026-09-08T00:00:00",
 *             "RemainingDays": 1284
 *         },
 *         "NameServers": ["ns1.example.com", "ns2.example.com"],
 *         "Additional": [],
 *         "ChildNameServers": []
 *     },
 *     "result": "OK"
 * }
 */
