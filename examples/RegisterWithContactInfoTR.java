/*
 * java-dna example: RegisterWithContactInfoTR
 * Register a .tr domain with TRABIS attributes (individual or company owner).
 *
 * Build and run:
 *   mvn -Pexamples -DskipTests compile
 *   java -cp target/classes RegisterWithContactInfoTR
 */
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

public class RegisterWithContactInfoTR {

    public static void main(String[] args) {
        // Credentials: your Reseller ID (UUID) + API Key from Panel -> API Settings.
        // Panel login credentials will NOT work. DNA_RESELLER_ID / DNA_API_KEY override them.
        String resellerId = env("DNA_RESELLER_ID", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); // Reseller ID (UUID)
        String apiKey = env("DNA_API_KEY", "your-api-key");                                  // API Key

        DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("FirstName", "Ahmet");
        contact.put("LastName", "Yilmaz");
        contact.put("Company", "");
        contact.put("EMail", "ahmet.yilmaz@example.com");
        contact.put("AddressLine1", "Ornek Sokak No: 123");
        contact.put("AddressLine2", "Daire 4");
        contact.put("AddressLine3", "");
        contact.put("City", "Istanbul");
        contact.put("Country", "TR");
        contact.put("Fax", "2121234567");
        contact.put("FaxCountryCode", "90");
        contact.put("Phone", "5551234567");
        contact.put("PhoneCountryCode", 90);
        contact.put("Type", "Contact");
        contact.put("ZipCode", "34000");
        contact.put("State", "Istanbul");
        Map<String, Object> contacts = new LinkedHashMap<>();
        contacts.put("Administrative", contact);
        contacts.put("Billing", contact);
        contacts.put("Technical", contact);
        contacts.put("Registrant", contact);

        // Individual owner
        Map<String, Object> individual = new LinkedHashMap<>();
        individual.put("TRABISDOMAINCATEGORY", 1);
        individual.put("TRABISCITIZIENID", "11111111111");
        individual.put("TRABISNAMESURNAME", "Ahmet Yilmaz");
        individual.put("TRABISCOUNTRYID", "215");
        individual.put("TRABISCITYID", "34");
        individual.put("TRABISCOUNTRYNAME", "Türkiye");
        individual.put("TRABISCITYNAME", "Istanbul");

        // Company owner
        Map<String, Object> commercial = new LinkedHashMap<>();
        commercial.put("TRABISDOMAINCATEGORY", 0);
        commercial.put("TRABISORGANIZATION", "Example Ltd. Sti.");
        commercial.put("TRABISTAXOFFICE", "Istanbul VD");
        commercial.put("TRABISTAXNUMBER", "1234567890");
        commercial.put("TRABISCOUNTRYID", "215");
        commercial.put("TRABISCITYID", "34");
        commercial.put("TRABISCOUNTRYNAME", "Türkiye");
        commercial.put("TRABISCITYNAME", "Istanbul");

        // The library sends only what the gateway accepts: TRABISDOMAINCATEGORY
        // (1 -> Owner, 0 -> Corporate), TRABISCITIZENID (the TRABISCITIZIENID
        // spelling is accepted), TRABISTAXOFFICE and TRABISTAXNUMBER.
        Map<String, Object> result = dna.registerWithContactInfo("example.com.tr", 1, contacts,
            Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, individual);
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
