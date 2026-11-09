/*
 * java-dna example: ModifyPrivacyProtectionStatus
 * Turn WHOIS privacy protection on or off.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes ModifyPrivacyProtectionStatus
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class ModifyPrivacyProtectionStatus {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
        System.out.println(Json.encodePretty(result));

        // The status may also be text, as panels store it: "1", "true", "on", "yes" enable.
        // The reason is not sent; the gateway has no such field.
        System.out.println(Json.encodePretty(dna.modifyPrivacyProtectionStatus("example.com", "off")));
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? fallback : v;
    }
}

/*
 * Sample output:
 *
 * {"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
 */
