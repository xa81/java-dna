/*
 * java-dna example: CheckAvailability
 * Check availability of every domain x extension combination (at most 20 per call).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes CheckAvailability
 */
import java.util.Arrays;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class CheckAvailability {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Object result = dna.checkAvailability(Arrays.asList("example", "testdomain"), Arrays.asList("com", "net"), 1, "create");
        // Success: a List of rows. Failure: a Map {"result": "ERROR", "error": {...}}.
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
 * [
 *     {
 *         "TLD": "com",
 *         "DomainName": "example",
 *         "Status": "notavailable",
 *         "Command": "create",
 *         "Period": 1,
 *         "IsFee": false,
 *         "Price": "11.9900",
 *         "Currency": "USD",
 *         "Reason": "Domain exists"
 *     },
 *     {
 *         "TLD": "net",
 *         "DomainName": "testdomain",
 *         "Status": "available",
 *         "Command": "create",
 *         "Period": 1,
 *         "IsFee": false,
 *         "Price": "13.9900",
 *         "Currency": "USD",
 *         "Reason": null
 *     }
 * ]
 */
