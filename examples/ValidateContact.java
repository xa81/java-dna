/*
 * java-dna example: ValidateContact
 * Complete and normalise a contact the way registration does (no network).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes ValidateContact
 */
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class ValidateContact {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("FirstName", " Jo ");
        contact.put("Phone", "+1 (202) 555-0100");
        contact.put("Country", "Turkey");
        contact.put("City", "");
        contact.put("Fax", "0090 212 123 45 67");
        System.out.println(Json.encodePretty(dna.validateContact(contact)));
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
 *     "FirstName": "Jo",
 *     "Phone": "2025550100",
 *     "Country": "TR",
 *     "City": "ISTANBUL",
 *     "Fax": "2121234567",
 *     "LastName": "Isimyok",
 *     "AddressLine1": "Addres yok",
 *     "State": "ISTANBUL",
 *     "ZipCode": "34000",
 *     "PhoneCountryCode": "1",
 *     "FaxCountryCode": "90"
 * }
 */
