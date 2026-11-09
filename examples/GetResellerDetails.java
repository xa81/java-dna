/*
 * java-dna example: GetResellerDetails
 * Reseller account details with USD and TL balances.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes GetResellerDetails
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class GetResellerDetails {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.getResellerDetails();
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
 *     "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
 *     "active": true,
 *     "name": "example-reseller",
 *     "balance": "601.64",
 *     "currency": "USD",
 *     "symbol": "$",
 *     "balances": [
 *         {"balance": "601.64", "currency": "USD", "symbol": "$"},
 *         {"balance": "1250.50", "currency": "TL", "symbol": "TL"}
 *     ]
 * }
 */
