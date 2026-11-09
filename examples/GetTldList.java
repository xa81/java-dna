/*
 * java-dna example: GetTldList
 * TLDs with pricing per period.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes GetTldList
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class GetTldList {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.getTldList(2);
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
 *     "data": [
 *         {
 *             "id": 1,
 *             "status": "Active",
 *             "maxchar": 63,
 *             "maxperiod": 10,
 *             "minchar": 1,
 *             "minperiod": 1,
 *             "tld": "aero",
 *             "pricing": {
 *                 "registration": {"1": "44.9900", "2": "94.9100", "3": "144.8300"},
 *                 "renew": {"1": "49.9200", "2": "99.8400"},
 *                 "transfer": {"1": "44.9900"}
 *             },
 *             "currencies": {"registration": "USD", "renew": "USD", "transfer": "USD"}
 *         }
 *     ],
 *     "result": "OK"
 * }
 */
