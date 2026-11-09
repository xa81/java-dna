/*
 * java-dna example: GetCurrentBalance
 * Account balance in one currency (USD by default; TRY, EUR, GBP).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes GetCurrentBalance
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class GetCurrentBalance {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> usd = dna.getCurrentBalance();
        System.out.println(Json.encodePretty(usd));

        Map<String, Object> tl = dna.getCurrentBalance("TRY");
        System.out.println(Json.encodePretty(tl));
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
 *     "ErrorCode": 0,
 *     "OperationMessage": "Command completed succesfully.",
 *     "OperationResult": "SUCCESS",
 *     "Balance": "601.64",
 *     "CurrencyId": 2,
 *     "CurrencyInfo": null,
 *     "CurrencyName": "USD",
 *     "CurrencySymbol": "$"
 * }
 */
