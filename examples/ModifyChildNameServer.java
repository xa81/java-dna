/*
 * java-dna example: ModifyChildNameServer
 * Change the IP address of a child nameserver.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes ModifyChildNameServer
 */
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class ModifyChildNameServer {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> result = dna.modifyChildNameServer("example.com", "ns1.example.com", "192.168.1.2");
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
 * {"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.168.1.2"]}}
 */
