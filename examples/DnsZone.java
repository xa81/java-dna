/*
 * java-dna example: DnsZone
 * Manage DNS zone records: list, add, edit one value, remove one value, replace a whole set.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes DnsZone
 */
import java.util.Arrays;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class DnsZone {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        // List every record (one row per value)
        System.out.println(Json.encodePretty(dna.getResourceRecords("example.com")));

        // Add one value next to the existing ones (read, merge, write, apply)
        System.out.println(Json.encodePretty(dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com")));

        // Swap one value, keeping the others
        System.out.println(Json.encodePretty(
            dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20")));

        // Remove one value; removing the last one deletes the set
        System.out.println(Json.encodePretty(dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com")));

        // Make a name+type hold exactly these values (empty list removes it)
        System.out.println(Json.encodePretty(dna.setResourceRecordSet("example.com", "@", "TXT",
            Arrays.asList("v=spf1 include:_spf.example.net ~all"), 3600)));

        // Low-level calls: stage several changes, then commit once
        dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
        dna.addResourceRecord("example.com", "blog", "CNAME", "example.com.", 300, false);
        System.out.println(Json.encodePretty(dna.applyResourceRecords("example.com")));

        // Remove a whole set (every value under a name + type)
        System.out.println(Json.encodePretty(dna.deleteResourceRecordSet("example.com", "blog", "CNAME")));
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? fallback : v;
    }
}

/*
 * Sample output:
 *
 * getResourceRecords:
 * {
 *     "result": "OK",
 *     "data": {
 *         "records": [
 *             {"Name": "example.com.", "Type": "A", "TTL": 3600, "Content": "192.0.2.1", "Disabled": false},
 *             {"Name": "www.example.com.", "Type": "CNAME", "TTL": 300, "Content": "example.com.", "Disabled": false},
 *             {"Name": "example.com.", "Type": "MX", "TTL": 3600, "Content": "10 mx1.example.com.", "Disabled": false}
 *         ]
 *     }
 * }
 * addResourceRecordValue:
 * {
 *     "result": "OK",
 *     "data": {"Name": "", "Type": "MX", "TTL": 3600, "Contents": ["10 mx1.example.com.", "30 mx3.example.com"]}
 * }
 * Supported types: A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT.
 * Zone calls are paced one second apart (the gateway answers 429 to bursts).
 */
