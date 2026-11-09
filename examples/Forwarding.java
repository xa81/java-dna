/*
 * java-dna example: Forwarding
 * Read, set and remove URL forwarding for a domain.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes Forwarding
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class Forwarding {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        // "Standard" = plain redirect, "Frame" = masked (framed) redirect
        System.out.println(Json.encodePretty(dna.setForwarding("example.com", "https://www.example.org", "Standard")));

        System.out.println(Json.encodePretty(dna.getForwarding("example.com")));

        System.out.println(Json.encodePretty(dna.deleteForwarding("example.com")));
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? fallback : v;
    }
}

/*
 * Sample output:
 *
 * setForwarding:
 * {"result": "OK", "data": {"RedirectAddress": "https://www.example.org", "ForwardType": "Standard"}}
 * getForwarding:
 * {"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Standard"}}
 * (no forwarding set: {"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}})
 * deleteForwarding:
 * {"result": "OK", "data": {"DomainName": "example.com", "Removed": true}}
 * (nothing to remove: "Removed": false)
 */
