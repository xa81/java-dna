/*
 * java-dna example: RegisterWithContactInfo
 * Register a domain with contacts and nameservers.
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes RegisterWithContactInfo
 */
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class RegisterWithContactInfo {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("FirstName", "John");
        contact.put("LastName", "Doe");
        contact.put("Company", "Example Corp");
        contact.put("EMail", "john.doe@example.com");
        contact.put("AddressLine1", "123 Main Street");
        contact.put("AddressLine2", "Suite 100");
        contact.put("AddressLine3", "");
        contact.put("City", "Los Angeles");
        contact.put("Country", "US");
        contact.put("Fax", "5559876543");
        contact.put("FaxCountryCode", "1");
        contact.put("Phone", "5551234567");
        contact.put("PhoneCountryCode", 1);
        contact.put("Type", "Contact");
        contact.put("ZipCode", "90001");
        contact.put("State", "California");
        Map<String, Object> contacts = new LinkedHashMap<>();
        contacts.put("Administrative", contact);
        contacts.put("Billing", contact);
        contacts.put("Technical", contact);
        contacts.put("Registrant", contact);

        Map<String, Object> result = dna.registerWithContactInfo("example.com", 1, contacts,
            Arrays.asList("ns1.example.com", "ns2.example.com"), true, false);
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
 *     "data": {
 *         "ID": 0,
 *         "Status": "Active",
 *         "DomainName": "",
 *         "AuthCode": "",
 *         "LockStatus": "false",
 *         "PrivacyProtectionStatus": "false",
 *         "IsChildNameServer": "false",
 *         "Contacts": {
 *             "Billing": {"ID": 0},
 *             "Technical": {"ID": 0},
 *             "Administrative": {"ID": 0},
 *             "Registrant": {"ID": 0}
 *         },
 *         "Dates": {"Start": "", "Expiration": "2027-11-09T12:00:00", "RemainingDays": 366},
 *         "NameServers": [],
 *         "Additional": [],
 *         "ChildNameServers": []
 *     },
 *     "result": "OK"
 * }
 * The register and transfer endpoints answer with a short receipt (status and
 * expiry date); call getDetails() afterwards for the full record.
 */
