/*
 * java-dna example: GetList
 * Domains in the account, with paging and sorting.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes GetList
 */
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class GetList {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        // First 200 domains
        Map<String, Object> result = dna.getList();
        System.out.println(Json.encodePretty(result));

        // Page 2 (0-based) of 50, newest expiry first, filtered by name
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("PageNumber", 1);
        filters.put("PageSize", 50);
        filters.put("OrderColumn", "ExpirationDate");
        filters.put("OrderDirection", "DESC");
        filters.put("DomainName", "example");
        System.out.println(Json.encodePretty(dna.getList(filters)));
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
 *         "Domains": [
 *             {
 *                 "ID": 11,
 *                 "Status": "Active",
 *                 "DomainName": "example.com",
 *                 "AuthCode": "",
 *                 "LockStatus": "true",
 *                 "PrivacyProtectionStatus": "false",
 *                 "IsChildNameServer": "false",
 *                 "Contacts": {
 *                     "Billing": {"ID": ""},
 *                     "Technical": {"ID": ""},
 *                     "Administrative": {"ID": ""},
 *                     "Registrant": {"ID": ""}
 *                 },
 *                 "Dates": {"Start": "2024-03-15T10:20:30", "Expiration": "2027-03-15T10:20:30", "RemainingDays": 126},
 *                 "NameServers": ["ns1.example.net", "ns2.example.net"],
 *                 "Additional": [],
 *                 "ChildNameServers": []
 *             }
 *         ]
 *     },
 *     "result": "OK",
 *     "TotalCount": 1
 * }
 */
