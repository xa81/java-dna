/*
 * java-dna example: GetContacts
 * Read the four contacts of a domain.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes GetContacts
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class GetContacts {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.getContacts("example.com");
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
 *         "contacts": {
 *             "Administrative": {
 *                 "ID": "DNA-A-2",
 *                 "Status": "Active",
 *                 "AuthCode": "",
 *                 "FirstName": "John",
 *                 "LastName": "Doe",
 *                 "Company": "Example Corp",
 *                 "EMail": "john.doe@example.com",
 *                 "Type": "Administrative",
 *                 "Address": {
 *                     "Line1": "123 Main Street Suite 100",
 *                     "Line2": "",
 *                     "Line3": "",
 *                     "State": "California",
 *                     "City": "Los Angeles",
 *                     "Country": "US",
 *                     "ZipCode": "90001"
 *                 },
 *                 "Phone": {
 *                     "Phone": {"Number": "5551234567", "CountryCode": "1"},
 *                     "Fax": {"Number": "5559876543", "CountryCode": "1"}
 *                 },
 *                 "Additional": []
 *             },
 *             "Billing": {...},
 *             "Registrant": {...},
 *             "Technical": {...}
 *         }
 *     },
 *     "result": "OK"
 * }
 */
