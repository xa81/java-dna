/*
 * java-dna example: IsTrTLD
 * Check whether a domain is a .tr domain (no network).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes IsTrTLD
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class IsTrTLD {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        System.out.println(dna.isTrTLD("example.com.tr")); // true
        System.out.println(dna.isTrTLD("example.com"));    // false
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? fallback : v;
    }
}

/*
 * Sample output:
 *
 * true
 * false
 */
