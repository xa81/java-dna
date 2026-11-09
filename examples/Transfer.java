/*
 * java-dna example: Transfer
 * Start an incoming transfer with the auth (EPP) code.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes Transfer
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class Transfer {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        // Transfers never send contacts: the registrar takes them over with the domain.
        Map<String, Object> result = dna.transfer("example.com", "ABC123XYZ456", 1);
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
 *         "ID": 0,
 *         "Status": "Active",
 *         "DomainName": "",
 *         "AuthCode": "",
 *         "LockStatus": "false",
 *         "PrivacyProtectionStatus": "false",
 *         "IsChildNameServer": "false",
 *         "Contacts": {
 *             "Billing": {"ID": 0},
 *             "Technical": {"ID": 0},
 *             "Administrative": {"ID": 0},
 *             "Registrant": {"ID": 0}
 *         },
 *         "Dates": {"Start": "", "Expiration": "2027-11-09T12:00:00", "RemainingDays": 366},
 *         "NameServers": [],
 *         "Additional": [],
 *         "ChildNameServers": []
 *     },
 *     "result": "OK"
 * }
 * The register and transfer endpoints answer with a short receipt (status and
 * expiry date); call getDetails() afterwards for the full record.
 */
