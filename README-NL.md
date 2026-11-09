<div align="center">
  <a href="README-TR.md"   >   TR <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/TR.png" alt="TR" height="20" /></a>
  <a href="README.md"> | EN <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/US.png" alt="EN" height="20" /></a>
   <a href="README-DE.md"> | DE <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/DE.png" alt="DE" height="20" /></a>
  <a href="README-SA.md"> | SA <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/SA.png" alt="AR" height="20" /></a>
  <a href="README-NL.md"> | NL <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/NL.png" alt="NL" height="20" /></a>
  <a href="README-AZ.md"> | AZ <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/AZ.png" alt="AZ" height="20" /></a>
  <a href="README-CN.md"> | CN <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/CN.png" alt="CN" height="20" /></a>
  <a href="README-FR.md"> | FR <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/FR.png" alt="FR" height="20" /></a>
  <a href="README-IT.md"> | IT <img style="padding-top: 8px" src="https://raw.githubusercontent.com/yammadev/flag-icons/master/png/IT.png" alt="IT" height="20" /></a>
</div>

# java-dna — DomainNameAPI Java-bibliotheek

Java-client voor de REST API voor domeinresellers van DomainNameAPI. Het is een getrouwe port van
[php-dna](https://github.com/domainreseller/php-dna): dezelfde methoden, dezelfde verzoeken over
de lijn en dezelfde retourwaarden. Kotlin en andere JVM-talen kunnen de bibliotheek direct gebruiken.

## 📦 Downloaden — gebruik altijd de Releases!

⬇️ **Download hier de nieuwste geteste versie: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ Gebruik **niet** de groene knop **Code → Download ZIP** — daarmee downloadt u de ruwe ontwikkelbranch. Releasepakketten zijn voorzien van een versienummer, getest en klaar voor productie.

## Installatie- en integratiegids

### Minimale vereisten

- Java 11 of hoger (aanbevolen: 17 of 21)
- Geen runtime-afhankelijkheden: de bibliotheek gebruikt `java.net.http.HttpClient` en een kleine ingebouwde JSON-codec.

### A) Release-jar

Download `domainnameapi-1.0.0.jar` van de pagina [Releases](https://github.com/domainreseller/java-dna/releases/latest) en plaats het bestand op uw classpath.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Coördinaten: `io.github.domainreseller:domainnameapi:1.0.0`. Zolang het artefact nog niet beschikbaar is in een
openbare repository, installeert u het vanuit de releasebroncode in uw lokale repository:

```bash
mvn -DskipTests install
```

```xml
<dependency>
    <groupId>io.github.domainreseller</groupId>
    <artifactId>domainnameapi</artifactId>
    <version>1.0.0</version>
</dependency>
```

```kotlin
// build.gradle.kts
repositories { mavenLocal(); mavenCentral() }
dependencies { implementation("io.github.domainreseller:domainnameapi:1.0.0") }
```

## 🔑 API-inloggegevens — Reseller ID en API Key

De bibliotheek communiceert met de DomainNameAPI REST API en heeft precies twee inloggegevens nodig, die aan de constructor worden doorgegeven:

| Parameter | Waarde |
|---|---|
| Eerste | Reseller ID — UUID zoals `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Tweede | API Key |

> 💡 U vindt uw **Reseller ID** en **API Key** in uw DomainNameAPI-paneel onder **API-instellingen**.
> ⚠️ Dit zijn **API-inloggegevens** — uw inloggegevens voor het paneel werken hier **niet**.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Live-omgeving
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Testomgeving (OTE)
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Bij beide inloggegevens worden spaties aan het begin en einde verwijderd. De testmodus-vlag mag ook een string zijn, omdat panelen
checkbox-instellingen als tekst opslaan: `"1"`, `"true"`, `"on"` en `"yes"` selecteren de testomgeving, elke andere waarde de live-omgeving.

## Snel aan de slag

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

Map<String, Object> details = dna.getDetails("example.com");
if ("OK".equals(details.get("result"))) {
    Map<?, ?> data = (Map<?, ?>) details.get("data");
    System.out.println(data.get("Status") + " until " + ((Map<?, ?>) data.get("Dates")).get("Expiration"));
} else {
    Map<?, ?> error = (Map<?, ?>) details.get("error");
    System.out.println(error.get("Code") + ": " + error.get("Message"));
}

// Elk resultaat als JSON afdrukken
System.out.println(Json.encodePretty(details));
```

Kotlin:

```kotlin
val dna = DomainNameApi(resellerId, apiKey)
val details = dna.getDetails("example.com")
if (details["result"] == "OK") {
    val data = details["data"] as Map<*, *>
    println(data["Status"])
}
```

### Retourwaarden

De methoden retourneren dezelfde dynamische structuur als php-dna, opgebouwd uit `LinkedHashMap` en
`ArrayList`:

- Sleutelnamen zijn identiek aan die van php-dna (`Status`, `LockStatus`, `NameServers`, ...).
- Vlaggen zoals `LockStatus` zijn de **strings** `"true"` / `"false"`.
- Prijzen en saldi zijn **opgemaakte strings** (`"11.9900"`, `"601.64"`).
- Getallen zijn `Integer`, `Long` of `Double`; JSON-objecten zijn `Map<String, Object>`, arrays `List<Object>`.
- De meeste methoden retourneren `{"result": "OK", "data": {...}}` of `{"result": "ERROR", "error": {...}}`.
  Uitzonderingen: `getCurrentBalance` gebruikt een `OperationResult`-envelop en `checkAvailability`
  retourneert bij succes een kale `List`.

Fouten worden geretourneerd, niet gegooid. Eén clientinstantie bewaart diagnostische gegevens per aanroep; gebruik daarom één
instantie per thread.

## Gebruik

Alle onderstaande voorbeelden zijn ook beschikbaar als uitvoerbare klassen in de map [examples](examples):

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Domeinregistratie

Opmerking: voor .tr-domeinen zijn aanvullende parameters vereist. De parameter `additionalAttributes` wordt
gebruikt voor domeinnamen die extra informatie nodig hebben, zoals .tr.

```java
Map<String, Object> contact = new LinkedHashMap<>();
contact.put("FirstName", "John");
contact.put("LastName", "Doe");
contact.put("Company", "Example Corp");
contact.put("EMail", "john.doe@example.com");
contact.put("AddressLine1", "123 Lorem Street");
contact.put("AddressLine2", "Suite 456");
contact.put("City", "Springfield");
contact.put("Country", "US");
contact.put("Fax", "1234567890");
contact.put("FaxCountryCode", "1");
contact.put("Phone", "9876543210");
contact.put("PhoneCountryCode", 1);
contact.put("ZipCode", "12345");
contact.put("State", "IL");

Map<String, Object> contacts = new LinkedHashMap<>();
contacts.put("Administrative", contact);
contacts.put("Billing", contact);
contacts.put("Technical", contact);
contacts.put("Registrant", contact);

Map<String, Object> result = dna.registerWithContactInfo(
    "example.com",
    1,                                                  // looptijd (jaren)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // lege lijst = ns1/ns2.domainnameapi.com
    true,                                               // registrar-vergrendeling
    false                                               // privacybescherming
);

// .tr-domeinen: TRABIS-attributen als 7e parameter
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = particulier (Owner), 0 = bedrijf (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // particulier: nationaal identificatienummer
// bedrijf: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

Contacten kunnen plat (`AddressLine1`, `Phone`, `PhoneCountryCode`) of genest
(`Address.Line1`, `Phone.Phone.Number`) worden opgegeven. Ontbrekende of rommelige velden worden vóór verzending
aangevuld en genormaliseerd (zie `validateContact`).

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "data": {
        "ID": 0,
        "Status": "Active",
        "DomainName": "",
        "AuthCode": "",
        "LockStatus": "false",
        "PrivacyProtectionStatus": "false",
        "IsChildNameServer": "false",
        "Contacts": {
            "Billing": {"ID": 0},
            "Technical": {"ID": 0},
            "Administrative": {"ID": 0},
            "Registrant": {"ID": 0}
        },
        "Dates": {"Start": "", "Expiration": "2027-11-09T12:00:00", "RemainingDays": 366},
        "NameServers": [],
        "Additional": [],
        "ChildNameServers": []
    },
    "result": "OK"
}
```
Het registratie-endpoint antwoordt met een korte bevestiging; roep daarna `getDetails` aan voor het volledige record.
</details>

### Domeinverlenging

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### Domeintransfer

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Controleer eerst of het domein met deze autorisatiecode kan worden getransfereerd
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Lopende transfers
dna.cancelTransfer("example.com");   // inkomend
dna.approveTransfer("example.com");  // uitgaand
dna.rejectTransfer("example.com");   // uitgaand
```

<details>
<summary>Voorbeelduitvoer</summary>

`transfer` retourneert dezelfde structuur als de registratie. `checkTransfer`:

```json
{
    "result": "OK",
    "data": {
        "TransferAvailabilityStatus": true,
        "AuthCodeIsRequired": true,
        "AuthCodeIsValid": true,
        "UserTransferRequired": false,
        "TransferLock": false,
        "Message": "Domain can be transferred",
        "MessageKey": "TransferAvailable",
        "Additional": []
    }
}
```
Wanneer het domein niet kan worden getransfereerd, is `result` gelijk aan `"ERROR"` en
is `error` gelijk aan `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` retourneren
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Domeinlijst

```java
Map<String, Object> all = dna.getList();   // eerste 200 domeinen

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 0-gebaseerd
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "data": {
        "Domains": [
            {
                "ID": 11,
                "Status": "Active",
                "DomainName": "example.com",
                "AuthCode": "",
                "LockStatus": "true",
                "PrivacyProtectionStatus": "true",
                "IsChildNameServer": "false",
                "Contacts": {
                    "Billing": {"ID": ""},
                    "Technical": {"ID": ""},
                    "Administrative": {"ID": ""},
                    "Registrant": {"ID": ""}
                },
                "Dates": {"Start": "2024-03-15T10:20:30", "Expiration": "2027-03-15T10:20:30", "RemainingDays": 126},
                "NameServers": ["ns1.example.net", "ns2.example.net"],
                "Additional": [],
                "ChildNameServers": []
            }
        ]
    },
    "result": "OK",
    "TotalCount": 1
}
```
</details>

### TLD-lijst

```java
Map<String, Object> result = dna.getTldList(100);   // standaard 20
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "data": [
        {
            "id": 1,
            "status": "Active",
            "maxchar": 63,
            "maxperiod": 10,
            "minchar": 1,
            "minperiod": 1,
            "tld": "aero",
            "pricing": {
                "registration": {"1": "44.9900", "2": "94.9100", "3": "144.8300"},
                "renew": {"1": "49.9200", "2": "99.8400"},
                "transfer": {"1": "44.9900"}
            },
            "currencies": {"registration": "USD", "renew": "USD", "transfer": "USD"}
        }
    ],
    "result": "OK"
}
```
</details>

### Beschikbaarheid van domeinen controleren

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // namen zonder TLD
    Arrays.asList("com", "net"),           // TLD's, met of zonder punt
    1,                                     // looptijd
    "create"                               // commando (standaard "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Maximaal 20 combinaties van domein × TLD per aanroep (limiet van de gateway).

<details>
<summary>Voorbeelduitvoer</summary>

```json
[
    {
        "TLD": "com",
        "DomainName": "example",
        "Status": "notavailable",
        "Command": "create",
        "Period": 1,
        "IsFee": false,
        "Price": "11.9900",
        "Currency": "USD",
        "Reason": "Domain exists"
    },
    {
        "TLD": "net",
        "DomainName": "example2",
        "Status": "available",
        "Command": "create",
        "Period": 1,
        "IsFee": false,
        "Price": "13.9900",
        "Currency": "USD",
        "Reason": null
    }
]
```
</details>

### Domeindetails

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // zelfde structuur
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "data": {
        "ID": 4321,
        "Status": "Active",
        "DomainName": "example.com",
        "AuthCode": "Ab1!xyZ9",
        "LockStatus": "true",
        "PrivacyProtectionStatus": "false",
        "IsChildNameServer": "true",
        "Contacts": {
            "Billing": {"ID": "DNA-B-4"},
            "Technical": {"ID": "DNA-T-3"},
            "Administrative": {"ID": "DNA-A-2"},
            "Registrant": {"ID": "DNA-R-1"}
        },
        "Dates": {"Start": "2024-03-15T10:20:30", "Expiration": "2027-03-15T10:20:30", "RemainingDays": 126},
        "NameServers": ["ns1.example.net", "ns2.example.net"],
        "Additional": {"TRABISDOMAINCATEGORY": "1"},
        "ChildNameServers": [
            {"ns": "ns1.example.com", "ip": "192.0.2.11"},
            {"ns": "ns2.example.com", "ip": ""}
        ]
    },
    "result": "OK"
}
```
</details>

### Nameservers wijzigen

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Vóór verzending worden spaties uit de items verwijderd, worden ze naar kleine letters omgezet en worden dubbele items verwijderd. Een `Map` wordt
ook geaccepteerd (de waarden ervan worden gebruikt).

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Domeinvergrendeling in- / uitschakelen

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child-nameservers

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` retourneert `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Domeinprivacy wijzigen

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Domeincontacten

```java
Map<String, Object> current = dna.getContacts("example.com");

Map<String, Object> contacts = new LinkedHashMap<>();
contacts.put("Administrative", contact);
contacts.put("Billing", contact);
contacts.put("Technical", contact);
contacts.put("Registrant", contact);
Map<String, Object> saved = dna.saveContacts("example.com", contacts);
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "data": {
        "contacts": {
            "Administrative": {
                "ID": "DNA-A-2",
                "Status": "Active",
                "AuthCode": "",
                "FirstName": "Grace",
                "LastName": "Hopper",
                "Company": "",
                "EMail": "grace@example.com",
                "Type": "Administrative",
                "Address": {
                    "Line1": "Example Street 2",
                    "Line2": "",
                    "Line3": "",
                    "State": "DC",
                    "City": "Washington",
                    "Country": "US",
                    "ZipCode": "20001"
                },
                "Phone": {
                    "Phone": {"Number": "2025550100", "CountryCode": "1"},
                    "Fax": {"Number": "2025550101", "CountryCode": "1"}
                },
                "Additional": []
            },
            "Billing": {"...": "..."},
            "Registrant": {"...": "..."},
            "Technical": {"...": "..."}
        }
    },
    "result": "OK"
}
```
`saveContacts` retourneert `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### DNS-zonerecords

De gateway slaat records op als sets (naam + type). Bij het schrijven van een set worden al zijn waarden vervangen, en
verwijderen via een willekeurige waarde verwijdert de hele set. De onderstaande hulpmethoden op waardeniveau lezen, voegen samen en schrijven
dit voor u.

```java
// Weergeven (één rij per waarde)
dna.getResourceRecords("example.com");

// ÉÉN waarde toevoegen / vervangen / verwijderen, de overige blijven behouden
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Laat een set precies deze waarden bevatten (een lege lijst verwijdert de set)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Laag niveau: klaarzetten met apply=false, daarna in één keer doorvoeren
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Namen kunnen `""` / `"@"` (apex), een label (`"www"`) of een volledige naam (`"www.example.com."`) zijn.
Ondersteunde typen: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. De TTL is
1..86400 seconden. Tussen zone-aanroepen zit minstens één seconde, omdat de gateway op snelle reeksen verzoeken
antwoordt met HTTP 429 (`setZoneMinInterval(seconds)` wijzigt de tussenpoos).

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "result": "OK",
    "data": {
        "records": [
            {"Name": "example.com.", "Type": "A", "TTL": 3600, "Content": "192.0.2.1", "Disabled": false},
            {"Name": "www.example.com.", "Type": "CNAME", "TTL": 300, "Content": "example.com.", "Disabled": false},
            {"Name": "example.com.", "Type": "MX", "TTL": 3600, "Content": "10 mx1.example.com.", "Disabled": false}
        ]
    }
}
```
`addResourceRecordValue`:
`{"result": "OK", "data": {"Name": "", "Type": "MX", "TTL": 3600, "Contents": ["10 mx1.example.com.", "30 mx3.example.com"]}}`
— wanneer de waarde al aanwezig is: `{..., "Contents": [...], "Changed": false}`.
</details>

### URL-doorsturing

```java
dna.setForwarding("example.com", "https://www.example.org");           // standaard doorverwijzing
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // gemaskeerd
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Zonder doorsturing: `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### Resellerdetails en saldo ophalen

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "result": "OK",
    "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "active": true,
    "name": "example-reseller",
    "balance": "601.64",
    "currency": "USD",
    "symbol": "$",
    "balances": [
        {"balance": "601.64", "currency": "USD", "symbol": "$"},
        {"balance": "1250.5", "currency": "TL", "symbol": "TL"}
    ]
}
```
`getCurrentBalance`:
```json
{
    "ErrorCode": 0,
    "OperationMessage": "Command completed succesfully.",
    "OperationResult": "SUCCESS",
    "Balance": "601.64",
    "CurrencyId": 2,
    "CurrencyInfo": null,
    "CurrencyName": "USD",
    "CurrencySymbol": "$"
}
```
</details>

### Hulpfuncties (zonder netwerk)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` vult ontbrekende velden met placeholders, verwijdert cijfers en symbolen uit namen,
zet landnamen om in ISO-codes en splitst telefoonnummers in nummer en landcode — dezelfde
normalisatie die ook bij registratie wordt toegepast.

<details>
<summary>Voorbeelduitvoer</summary>

```json
{
    "FirstName": "Jo",
    "Phone": "5323743388",
    "Country": "TR",
    "LastName": "Isimyok",
    "AddressLine1": "Addres yok",
    "City": "ISTANBUL",
    "State": "ISTANBUL",
    "ZipCode": "34000",
    "PhoneCountryCode": "90",
    "Fax": "",
    "FaxCountryCode": ""
}
```
</details>

## Foutafhandeling

Netwerkmethoden gooien nooit een exceptie bij API- of netwerkproblemen; ze retourneren in plaats daarvan een foutenvelop:

```json
{
    "result": "ERROR",
    "error": {
        "Code": "API_404",
        "Message": "The domain name you requested could not be found. Please check and try again.",
        "Details": "The domain could not be found. Please check and try again."
    }
}
```

- `Code` is `API_<HTTP-status>` bij gatewayfouten (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  Een antwoord met 301/302 betekent onjuiste inloggegevens (`Invalid API credentials`).
- Wanneer de gateway validatiefouten per veld vermeldt, noemt `Details` de betreffende velden, bijvoorbeeld
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Een lege foutbody wordt `HTTP 429 - Too Many Requests` en vergelijkbaar.
- Time-outs en DNS- en TLS-storingen zijn ook fouten, nooit een leeg succes. Een time-out luidt
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` met code `API_28`;
  andere transportfouten luiden `Curl error during request: ...` (de curl-foutnummers blijven behouden zodat
  de codes overeenkomen met php-dna; `getParsedResponseData()` toont ze als `CURL_<n>`).
- Controles aan de kant van de bibliotheek gebruiken eigen codes: `RESPONSE` (leeg antwoord), `DOMAIN_RENEW`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Diagnostische gegevens van de laatste aanroep:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // gedecodeerde body, of {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // gedecodeerde body, of {Code, Message, Details} van de fout
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Testmodus

Geef `true` (of `"true"`, `"1"`, `"on"`, `"yes"`) mee als derde constructorargument om de
OTE-testomgeving `https://ote.domainresellerapi.com/api/v1` te gebruiken. Met `setServiceUrl(url)` richt u de
client op een willekeurige andere basis-URL.

## Foutrapportage (telemetrie)

Wanneer een API-aanroep mislukt, kan de bibliotheek een foutrapport sturen naar de eigen fouttracker van DomainNameAPI
(Sentry). Een klein, willekeurig deel van de geslaagde aanroepen (4%, plus elke aanroep die langer dan 1 seconde duurt) wordt
verzonden als prestatierecord. Rapporten worden op de achtergrond verzonden en blokkeren of verstoren uw
aanroep nooit. Foutrapportage staat **standaard aan**; gebeurtenissen worden gelabeld met `application=DomainNameAPI-Java`
en de versie van de bibliotheek.

> ℹ️ In versie 1.0.0 is het ingebouwde Sentry-adres nog leeg (het Java-project wordt nog
> ingericht), dus er wordt niets verzonden tenzij u met `setTelemetryDsn(...)` een adres configureert.

**Wat wordt verzonden:**

- foutcode en -bericht
- pad van het aangeroepen endpoint en HTTP-methode
- duur van de aanroep
- bibliotheekversie en Java-versie
- reseller ID en domeinnaam
- het openbare IP-adres van de server (opgevraagd via `api.ipify.org`)
- de gebruikersnaam van het besturingssysteem
- het API-verzoek en -antwoord, inclusief de contactgegevens van het domein; alleen inloggegevens worden gemaskeerd — EPP-/autorisatiecodes, API-sleutels, wachtwoorden en tokens worden vervangen door `***`

**Wat nooit wordt verzonden:** EPP-/autorisatiecodes, API-sleutels, wachtwoorden of tokens in leesbare vorm.

**Waarom en hoe lang:** de gegevens worden uitsluitend gebruikt voor gebruiksmeting en het vroegtijdig opsporen van problemen, en worden 30 dagen bewaard op de eigen Sentry-server van DomainNameAPI, waarna ze automatisch worden verwijderd.

**Zo schakelt u het uit:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // niets verzenden
dna.setTelemetryMinimal(true);         // of: alleen code, bericht, endpoint, timing, versies, reseller ID
```

> 🔒 **Beveiliging:** verificatie van TLS-certificaten staat aan. Op een host met een verouderde CA-store kan deze
> worden uitgeschakeld met `dna.setSslVerification(false);` — dit wordt niet aanbevolen; het bijwerken van de
> Java-CA-store is de betere oplossing.

## Geavanceerd

- **Eigen HTTP-laag:** `dna.setTransport(HttpTransport)` vervangt het standaard
  `java.net.http`-transport (proxy's, eigen TLS, opnemen, tests).
- **Time-outs:** 30 s per aanroep, 10 s voor het opzetten van de verbinding.
- **User-Agent:** elk verzoek verstuurt `User-Agent: DomainNameAPI-Java/1.0.0`; Cloudflare vóór de
  gateway weigert standaard client-agents.

## Testen

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Beschrijving |
|------|-------------|
| `ConformanceTest` | Voert alle 126 conformiteitsfixtures van php-dna uit (`src/test/resources/conformance/`): de verzonden verzoeken en de geretourneerde waarden moeten overeenkomen met php-dna |
| `HttpLayerTest` | Headers, querystrings, JSON-bodies, testmodus, pacing, het echte `java.net.http`-transport |
| `ErrorMappingTest` | Vertaling van gatewayfouten, transportfouten, validatiefouten |
| `TelemetryTest` | Maskeren van inloggegevens, classificatie, minimale modus, structuur van gebeurtenissen |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, PHP-compatibele opmaak, JSON-codec |

Geen enkele test maakt verbinding met het netwerk buiten de lokale machine.

<hr style="border: 4px solid #000; border-style: dashed;">

## Retour- en foutcodes met beschrijvingen

| Code | Beschrijving                                   | Detail                                                                                                                                                         |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | Commando succesvol voltooid                    | Bewerking geslaagd.                                                                                                                                            |
| 1001 | Commando succesvol voltooid; actie in behandeling | Bewerking geslaagd. De bewerking staat momenteel echter in de wachtrij om te worden voltooid.                                                               |
| 2003 | Vereiste parameter ontbreekt                   | Fout door ontbrekende parameter. Bijvoorbeeld geen telefoonnummer in de contactgegevens.                                                                       |
| 2105 | Object komt niet in aanmerking voor verlenging | De domeinstatus is niet geschikt voor verlenging, vergrendeld voor bijwerkingen. De status mag niet "clientupdateprohibited" zijn. Kan ook komen door andere statusvoorwaarden. |
| 2200 | Authenticatiefout                              | Autorisatiefout, de beveiligingscode is onjuist of het domein is geregistreerd bij een andere registrar.                                                      |
| 2302 | Object bestaat al                              | De domeinnaam of nameserverinformatie bestaat al in de database. Registratie is niet mogelijk.                                                                 |
| 2303 | Object bestaat niet                            | De domeinnaam of nameserverinformatie bestaat niet in de database. Nieuwe registratie vereist.                                                                 |
| 2304 | Objectstatus verhindert de bewerking           | De domeinstatus is niet geschikt voor bijwerkingen, vergrendeld voor bijwerkingen. De status mag niet "clientupdateprohibited" zijn. Kan ook komen door andere statusvoorwaarden. |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | Systeemfout, details voor ({0})!                                                                                                     |
| 102  | Meerdere fouten, details voor ({0})!                                                                                                 |
| 103  | Onbekende fouten, details voor ({0})!                                                                                                |
| 200  | API-commando niet gevonden ({0})!                                                                                                    |
| 210  | API-service niet gevonden ({0})!                                                                                                     |
| 211  | API-serviceprovider niet ingesteld!                                                                                                  |
| 300  | Reseller niet gevonden!                                                                                                              |
| 301  | Uw huidige IP-adres heeft geen toegang. Zorg ervoor dat u verbinding maakt vanaf een geautoriseerd IP-adres en probeer het opnieuw   |
| 310  | TLD wordt niet ondersteund!                                                                                                          |
| 320  | API niet gevonden!                                                                                                                   |
| 321  | Valuta wordt niet ondersteund!                                                                                                       |
| 330  | Vereiste parameter(s) niet ingesteld ({0}).                                                                                          |
| –    | Zorg ervoor dat u alle contactobjecten volledig verzendt                                                                             |
| 340  | Prijsdefinitie niet gevonden ({0}[{1}]-{2}{3}).                                                                                      |
| 350  | Onvoldoende resellersaldo. (Reseller-ID: {0} - Huidig saldo: {1} {2}).                                                               |
| 350  | De boekhoudvaluta komt niet overeen of het saldo is niet toereikend.                                                                 |
| 360  | Ongeldig API-verzoek voor veld ({0}).                                                                                                |
| 360  | API-quotum overschreden!                                                                                                             |
| 361  | Throttling-fout!                                                                                                                     |
| 362  | Het premiumdomein kan op dit moment niet worden geregistreerd.                                                                       |
| 363  | Bewerking geannuleerd omdat het domein zich in de periode van automatische verlenging bevindt                                        |
| 364  | Dit domein is momenteel niet beschikbaar voor transacties vanwege een probleem bij het register                                      |

| CODE | DETAIL                                                                                      |
|------|---------------------------------------------------------------------------------------------|
| 400  | Ongeldig contact ({0}).                                                                     |
| 401  | Contactgegevens kunnen niet worden gesynchroniseerd.                                        |
| 402  | Geen toegang tot contactgegevens van het register.                                          |
| 403  | ER ONTBREEKT INFORMATIE IN HET SYSTEEM. VOER UW STANDAARDGEGEVENS IN OF NEEM CONTACT OP MET HET SUPPORTTEAM. |
| 404  | DOMEINCONTACT KAN NIET WORDEN BIJGEWERKT. TOESTEMMING VAN HET REGISTER VEREIST.             |
| 410  | Contact niet gevonden.                                                                      |
| 410  | Contact niet gevonden ({0}).                                                                |
| 420  | Ongeldig API-commando voor contact {0}.                                                     |
| 430  | Contact-API niet gevonden.                                                                  |
| 440  | Contact is niet gesynchroniseerd.                                                           |
| 450  | Te veel domeincontacten!                                                                    |
| 451  | Het bijwerken van het contact kon niet worden uitgevoerd.                                   |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | Ongeldig domein-ID.                                                                                                                  |
| 500  | Ongeldig domein-ID ({0}).                                                                                                            |
| 501  | Domein kon niet worden gesynchroniseerd ({0})                                                                                        |
| 502  | Interne transfer mislukt                                                                                                             |
| 503  | Domeinregistratie is niet beschikbaar.                                                                                               |
| 504  | Domeininformatie komt niet overeen. Voorheen: {0}                                                                                    |
| 505  | Voer het IP-adres in.                                                                                                                |
| 506  | De domeintransfer is gestart, maar de contactgegevens zijn niet gelezen.                                                             |
| 507  | Autorisatiefout.                                                                                                                     |
| 510  | Domein niet gevonden.                                                                                                                |
| 511  | Verlopen domeinen kunnen niet worden gevonden.                                                                                       |
| 512  | Domein kan niet worden verlengd.                                                                                                     |
| 513  | Het domein heeft geen status die bijwerken toestaat. Het moet actief zijn om te kunnen worden bijgewerkt                             |
| 514  | Redemption-periode verwacht.                                                                                                         |
| 520  | Ongeldig API-commando voor domein "{0}".                                                                                             |
| 530  | Ongeldige domeinlooptijd. De looptijd moet {0} tot {1} jaar zijn. De gevraagde looptijd is {2} jaar.                                 |
| 540  | Het domein kan niet verder dan {0} jaar vanaf de huidige datum worden verlengd.                                                      |
| 550  | Ongeldige domeinnaam. Het domein moet {0} tot {1} tekens lang zijn. De gevraagde domeinnaam heeft {2} tekens.                        |
| 560  | Ongeldig aantal nameservers. Het domein moet {0} tot {1} nameservers hebben. Het gevraagde aantal nameservers is {2}.                |
| 561  | Er is geen nameserverinformatie gevonden in het binnenkomende verzoek                                                                |
| 570  | IDN wordt niet ondersteund voor de TLD "{0}".                                                                                        |
| 571  | Looptijd ongeldig.                                                                                                                   |
| 572  | Commando ongeldig.                                                                                                                   |
| 573  | Domeinnamen niet gevonden.                                                                                                           |
| 574  | TLD-namen niet gevonden.                                                                                                             |
| 575  | Het domein heeft geen status die bijwerken toestaat. Het moet actief zijn om de nameservers te kunnen bijwerken                      |
| 576  | Het domein kan alleen in de laatste maand vóór de vervaldatum worden verlengd                                                        |
| 580  | Transfer wordt niet ondersteund voor de TLD "{0}".                                                                                   |
| 581  | Child-nameserver niet gevonden                                                                                                       |
| 582  | De transfer is gestart door een andere reseller.                                                                                     |
| 583  | Transfer niet geïnitialiseerd. Neem contact op met het supportteam.                                                                  |
| 584  | Objectstatus verhindert de bewerking                                                                                                 |
| 590  | Voor deze transfer is een autorisatiecode vereist.                                                                                   |
| 591  | De autorisatiecode is ongeldig.                                                                                                      |
| 592  | Er rust een transfervergrendeling op het domein.                                                                                     |
| 593  | Het domein kan niet worden getransfereerd; statusinformatie en transfervergrendeling moeten aan de vereiste criteria voldoen. (Statusinformatie: #ok) |
| 594  | Het nameserveradres van het domein kon niet worden opgelost ({0})                                                                    |
| 595  | Contactgegevens kunnen niet worden gelezen (whois.registrar.tld). Zorg ervoor dat de status van de privacybescherming open is        |
| 596  | Contactgegevens konden niet worden geverifieerd                                                                                      |
| 597  | TLD niet gevonden                                                                                                                    |
| 598  | Er is een onbekende fout opgetreden                                                                                                  |
| 599  | Domeindoorsturing niet gevonden                                                                                                      |

## Licentie

Apache License 2.0 — zie [LICENSE](LICENSE).
