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

# java-dna — DomainNameAPI Java-Bibliothek

Java-Client für die REST-API für Domain-Reseller von DomainNameAPI. Er ist eine originalgetreue Portierung von
[php-dna](https://github.com/domainreseller/php-dna): dieselben Methoden, dieselben Anfragen auf
der Leitung und dieselben Rückgabewerte. Kotlin und andere JVM-Sprachen können ihn direkt verwenden.

## 📦 Download — immer die Releases verwenden!

⬇️ **Die neueste getestete Version erhalten Sie hier: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ Verwenden Sie **nicht** den grünen Button **Code → Download ZIP** — damit laden Sie den rohen Entwicklungszweig herunter. Release-Pakete sind versioniert, getestet und produktionsreif.

## Installations- und Integrationsanleitung

### Mindestanforderungen

- Java 11 oder höher (empfohlen 17 oder 21)
- Keine Laufzeitabhängigkeiten: Die Bibliothek verwendet `java.net.http.HttpClient` und einen kleinen integrierten JSON-Codec.

### A) Release-Jar

Laden Sie `domainnameapi-1.0.0.jar` von der Seite [Releases](https://github.com/domainreseller/java-dna/releases/latest) herunter und legen Sie die Datei in Ihren Classpath.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Koordinaten: `io.github.domainreseller:domainnameapi:1.0.0`. Solange das Artefakt noch nicht in einem
öffentlichen Repository verfügbar ist, installieren Sie es aus dem Release-Quellcode in Ihr lokales Repository:

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

## 🔑 API-Zugangsdaten — Reseller-ID & API-Key

Die Bibliothek kommuniziert mit der DomainNameAPI REST-API und benötigt genau zwei Zugangsdaten, die dem Konstruktor übergeben werden:

| Parameter | Wert |
|---|---|
| Erster | Reseller-ID — UUID wie `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Zweiter | API-Key |

> 💡 Ihre **Reseller-ID** und Ihren **API-Key** finden Sie in Ihrem DomainNameAPI-Panel unter **API-Einstellungen**.
> ⚠️ Dies sind **API-Zugangsdaten** — Ihre Panel-Anmeldedaten funktionieren hier **nicht**.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Live-Umgebung
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Test-Umgebung (OTE)
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Bei beiden Zugangsdaten werden führende und nachgestellte Leerzeichen entfernt. Das Testmodus-Flag kann auch ein String sein, da Panels
Checkbox-Einstellungen als Text speichern: `"1"`, `"true"`, `"on"` und `"yes"` wählen die Testumgebung, alle anderen Werte die Live-Umgebung.

## Schnellstart

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

// Beliebiges Ergebnis als JSON ausgeben
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

### Rückgabewerte

Die Methoden liefern dieselbe dynamische Struktur wie php-dna, aufgebaut aus `LinkedHashMap` und
`ArrayList`:

- Die Schlüsselnamen sind identisch mit php-dna (`Status`, `LockStatus`, `NameServers`, ...).
- Flags wie `LockStatus` sind die **Strings** `"true"` / `"false"`.
- Preise und Guthaben sind **formatierte Strings** (`"11.9900"`, `"601.64"`).
- Zahlen sind `Integer`, `Long` oder `Double`; JSON-Objekte sind `Map<String, Object>`, Arrays `List<Object>`.
- Die meisten Methoden liefern `{"result": "OK", "data": {...}}` oder `{"result": "ERROR", "error": {...}}`.
  Ausnahmen: `getCurrentBalance` verwendet einen `OperationResult`-Umschlag, und `checkAvailability`
  liefert bei Erfolg eine einfache `List`.

Fehler werden zurückgegeben, nicht geworfen. Eine Client-Instanz speichert Diagnosedaten pro Aufruf; verwenden Sie daher eine
Instanz pro Thread.

## Verwendung

Alle folgenden Beispiele stehen auch als ausführbare Klassen im Ordner [examples](examples) zur Verfügung:

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Domain-Registrierung

Hinweis: Für .tr-Domains sind zusätzliche Parameter erforderlich. Der Parameter `additionalAttributes` wird
für Domainnamen verwendet, die zusätzliche Angaben benötigen, z. B. .tr.

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
    1,                                                  // Laufzeit (Jahre)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // leere Liste = ns1/ns2.domainnameapi.com
    true,                                               // Registrar-Sperre
    false                                               // Datenschutz (Privacy Protection)
);

// .tr-Domains: TRABIS-Attribute als 7. Parameter
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = Privatperson (Owner), 0 = Unternehmen (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // Privatperson: nationale ID-Nummer
// Unternehmen: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

Kontakte können flach (`AddressLine1`, `Phone`, `PhoneCountryCode`) oder verschachtelt
(`Address.Line1`, `Phone.Phone.Number`) angegeben werden. Fehlende oder unsaubere Felder werden vor dem Senden
ergänzt und normalisiert (siehe `validateContact`).

<details>
<summary>Beispielausgabe</summary>

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
Der Registrierungs-Endpunkt antwortet mit einer kurzen Bestätigung; rufen Sie anschließend `getDetails` auf, um den vollständigen Datensatz zu erhalten.
Lehnt das Gateway die Bestellung ab (`"success": false`), ist das Ergebnis ein Fehler mit dem Code
`REGISTER_DOMAIN` und der Meldung des Gateways.
</details>

### Domain-Verlängerung

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Beispielausgabe</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
Eine abgelehnte Verlängerung liefert einen Fehler mit dem Code `RENEW_DOMAIN`.
</details>

### Domain-Transfer

```java
// Bei einem Transfer werden nie Kontakte gesendet: Der Registrar übernimmt sie mit der Domain
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Vorab prüfen, ob die Domain mit diesem Auth-Code transferiert werden kann
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Ausstehende Transfers
dna.cancelTransfer("example.com");   // eingehend
dna.approveTransfer("example.com");  // ausgehend
dna.rejectTransfer("example.com");   // ausgehend
```

<details>
<summary>Beispielausgabe</summary>

`transfer` liefert dieselbe Struktur wie die Registrierung (einen Fehler mit dem Code `TRANSFER_DOMAIN`, wenn
das Gateway den Transfer ablehnt). `checkTransfer`:

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
Wenn die Domain nicht transferierbar ist, ist `result` gleich `"ERROR"` und
`error` lautet `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` liefern
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Domain-Liste

```java
Map<String, Object> all = dna.getList();   // erste 200 Domains

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 0-basiert
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Beispielausgabe</summary>

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

### TLD-Liste

```java
Map<String, Object> result = dna.getTldList(100);   // Standard 20
```

<details>
<summary>Beispielausgabe</summary>

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

### Verfügbarkeitsprüfung für Domains

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // Namen ohne TLD
    Arrays.asList("com", "net"),           // TLDs, mit oder ohne Punkt
    1,                                     // Laufzeit
    "create"                               // Befehl (Standard "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Höchstens 20 Kombinationen aus Domain × TLD pro Aufruf (Limit des Gateways).

<details>
<summary>Beispielausgabe</summary>

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

### Domain-Details

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // gleiche Struktur
```

<details>
<summary>Beispielausgabe</summary>

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

### Nameserver-Änderung

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Die Einträge werden vor dem Senden von Leerzeichen bereinigt, in Kleinbuchstaben umgewandelt und dedupliziert. Eine `Map` wird
ebenfalls akzeptiert (deren Werte werden verwendet).

<details>
<summary>Beispielausgabe</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Domain-Sperre aktivieren / deaktivieren

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Beispielausgabe</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child-Nameserver

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Beispielausgabe</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` liefert `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Domain-Datenschutz ändern

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true);

// Der Status kann auch Text sein, wie Panels ihn speichern: "1", "true", "on", "yes" aktivieren den Schutz
dna.modifyPrivacyProtectionStatus("example.com", "off");
```

Das optionale dritte Argument (`reason`) wird aus Kompatibilität mit php-dna akzeptiert, aber nicht gesendet:
das Gateway kennt kein solches Feld.

<details>
<summary>Beispielausgabe</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Domain-Kontakte

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
<summary>Beispielausgabe</summary>

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
Kontakttypen werden ohne Beachtung der Groß-/Kleinschreibung zugeordnet (`Admin`/`Administrative`, `Tech`/`Technical`,
`Registrant`/`Owner`, `Billing`); jeder andere Typ, den die Registry liefert, bleibt nach diesen vier erhalten.
`saveContacts` liefert `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### DNS-Zoneneinträge

Das Gateway speichert Einträge als Sets (Name + Typ). Beim Schreiben eines Sets werden alle seine Werte ersetzt, und
das Löschen über einen beliebigen Wert entfernt das gesamte Set. Die folgenden Hilfsmethoden auf Wertebene lesen, führen zusammen und schreiben
dies für Sie.

```java
// Auflisten (eine Zeile pro Wert)
dna.getResourceRecords("example.com");

// EINEN Wert hinzufügen / ersetzen / entfernen, die übrigen bleiben erhalten
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Ein Set enthält danach genau diese Werte (eine leere Liste entfernt es)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Low-Level: mit apply=false vormerken, dann einmalig übernehmen
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Namen können `""` / `"@"` (Apex), ein Label (`"www"`) oder ein vollständiger Name (`"www.example.com."`) sein.
Unterstützte Typen: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. Die TTL beträgt
1..86400 Sekunden. Zwischen Zonen-Aufrufen liegt mindestens eine Sekunde, da das Gateway auf schnelle Folgen von Anfragen
mit HTTP 429 antwortet (`setZoneMinInterval(seconds)` ändert den Abstand). Die Ergebnisse melden den gesendeten,
in Großbuchstaben umgewandelten Typ und die begrenzte TTL. Schlägt das automatische Apply fehl, liefert der
Schreibvorgang den Apply-Fehler: Die Änderung ist vorgemerkt, aber noch nicht live.

<details>
<summary>Beispielausgabe</summary>

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
— wenn der Wert bereits vorhanden ist: `{..., "Contents": [...], "Changed": false}`.
</details>

### URL-Weiterleitung

```java
dna.setForwarding("example.com", "https://www.example.org");           // Standard-Weiterleitung
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // maskiert
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Beispielausgabe</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Ohne Weiterleitung (`Redirect information not found` des Gateways):
`{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`. Jeder andere
Fehler, etwa eine unbekannte Domain, wird als Fehler zurückgegeben.
`deleteForwarding` liefert `{"result": "OK", "data": {"DomainName": "example.com", "Removed": true}}`,
oder `"Removed": false`, wenn nichts eingerichtet war.
</details>

### Reseller-Details und Guthaben abrufen

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Beispielausgabe</summary>

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
        {"balance": "1250.50", "currency": "TL", "symbol": "TL"}
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
Das Konto meldet nur USD- und TRY-Guthaben. Für andere Währungen (EUR, GBP, ...) lautet das Ergebnis
`{"ErrorCode": "BALANCE", "OperationResult": "FAILED", "OperationMessage": "No EUR balance is reported for this account. Available: USD, TRY.", "Balance": "0.00", ...}`.
</details>

### Hilfsfunktionen (ohne Netzwerk)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` füllt fehlende Felder mit Platzhaltern, entfernt Ziffern und Sonderzeichen aus Namen,
wandelt Ländernamen in ISO-Codes um und zerlegt Telefonnummern in Rufnummer und Ländervorwahl — dieselbe
Normalisierung, die auch bei der Registrierung angewendet wird.

<details>
<summary>Beispielausgabe</summary>

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

## Fehlerbehandlung

Netzwerkmethoden werfen bei API- oder Netzwerkproblemen niemals Ausnahmen; sie liefern stattdessen einen Fehler-Umschlag:

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

- `Code` lautet bei Gateway-Fehlern `API_<HTTP-Status>` (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  Hat das Gateway einen eigenen Fehlercode gesendet, bleibt er als `GatewayCode` erhalten
  (z. B. `"GatewayCode": "Dna.DomainService:Domain:10007"`), sodass sich „Domain nicht gefunden“ von anderen
  404-Fehlern unterscheiden lässt. Eine Antwort mit 301/302 bedeutet falsche Zugangsdaten (`Invalid API credentials`).
- Wenn das Gateway Validierungsfehler pro Feld auflistet, nennt `Details` die betroffenen Felder, z. B.
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Ein leerer Fehler-Body wird zu `HTTP 429 - Too Many Requests` und Ähnlichem.
- Timeouts sowie DNS- und TLS-Fehler sind ebenfalls Fehler und niemals ein leerer Erfolg. Ihr Code ist
  `CURL_<n>` mit der curl-Fehlernummer, wie in php-dna: Ein Timeout hat den Code `CURL_28` und lautet
  `Could not reach the DomainNameAPI service: the connection timed out. (...)`; andere Transportfehler
  (`CURL_6` DNS, `CURL_7` Verbindung abgelehnt, `CURL_35`/`CURL_60` TLS, `CURL_56` Verbindung zurückgesetzt)
  lauten `Curl error during request: ...`.
- Eine Ablehnung durch das Gateway (`"success": false`) liefert `REGISTER_DOMAIN`, `TRANSFER_DOMAIN` oder
  `RENEW_DOMAIN` mit der Meldung des Gateways.
- Prüfungen auf Bibliotheksseite verwenden eigene Codes: `RESPONSE` (leere Antwort), `DOMAIN_RENEW`, `BALANCE`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Diagnosedaten des letzten Aufrufs:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // dekodierter Body oder {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // dekodierter Body oder {Code, Message, Details} des Fehlers
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Testmodus

Übergeben Sie `true` (oder `"true"`, `"1"`, `"on"`, `"yes"`) als drittes Konstruktorargument, um die
OTE-Testumgebung `https://ote.domainresellerapi.com/api/v1` zu verwenden. `setServiceUrl(url)` richtet den
Client auf eine beliebige andere Basis-URL aus.

## Fehlerberichte (Telemetrie)

Wenn ein API-Aufruf fehlschlägt, kann die Bibliothek einen Fehlerbericht an den eigenen Fehler-Tracker von DomainNameAPI
(Sentry) senden. Ein kleiner, zufällig gewählter Anteil erfolgreicher Aufrufe (4 %, dazu jeder Aufruf, der länger als 1 Sekunde dauert) wird
als Performance-Datensatz gesendet. Berichte werden im Hintergrund gesendet und blockieren oder unterbrechen Ihren
Aufruf niemals. Fehlerberichte sind **standardmäßig aktiviert**; Ereignisse werden mit `application=DomainNameAPI-Java`
und der Bibliotheksversion gekennzeichnet.

> ℹ️ In Version 1.0.0 ist die integrierte Sentry-Adresse noch leer (das Java-Projekt wird gerade
> eingerichtet), daher wird nichts gesendet, solange Sie nicht mit `setTelemetryDsn(...)` eine Adresse konfigurieren.

**Was gesendet wird:**

- Fehlercode und -meldung
- Pfad des aufgerufenen Endpunkts und HTTP-Methode
- Dauer des Aufrufs
- Bibliotheksversion und Java-Version
- Reseller-ID und Domainname
- die öffentliche IP-Adresse des Servers (ermittelt über `api.ipify.org`)
- der Benutzername des Betriebssystems
- die API-Anfrage und -Antwort einschließlich der Kontaktdaten der Domain; nur Zugangsdaten werden maskiert — EPP-/Auth-Codes, API-Keys, Passwörter und Tokens werden durch `***` ersetzt

**Was niemals gesendet wird:** EPP-/Auth-Codes, API-Keys, Passwörter oder Tokens in lesbarer Form.

**Warum und wie lange:** Die Daten werden ausschließlich zur Nutzungsmessung und zur frühzeitigen Erkennung von Problemen verwendet und 30 Tage lang auf dem eigenen Sentry-Server von DomainNameAPI gespeichert; danach werden sie automatisch gelöscht.

**So deaktivieren Sie es:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // nichts senden
dna.setTelemetryMinimal(true);         // oder: nur Code, Meldung, Endpunkt, Dauer, Versionen, Reseller-ID
```

> 🔒 **Sicherheit:** Die Überprüfung von TLS-Zertifikaten ist aktiviert. Auf einem Host mit veraltetem CA-Speicher kann sie
> mit `dna.setSslVerification(false);` abgeschaltet werden — dies wird nicht empfohlen; die Aktualisierung des
> Java-CA-Speichers ist die bessere Lösung.

## Erweitert

- **Eigene HTTP-Schicht:** `dna.setTransport(HttpTransport)` ersetzt den standardmäßigen
  `java.net.http`-Transport (Proxys, eigenes TLS, Aufzeichnung, Tests).
- **Timeouts:** 30 s pro Aufruf, 10 s für den Verbindungsaufbau.
- **User-Agent:** Jede Anfrage sendet `User-Agent: DomainNameAPI-Java/1.0.0`; Cloudflare vor dem
  Gateway weist Standard-Client-Agents ab.

## Tests

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Beschreibung |
|------|-------------|
| `ConformanceTest` | Führt alle 134 Konformitäts-Fixtures von php-dna aus (`src/test/resources/conformance/`): Die gesendeten Anfragen und die zurückgegebenen Werte müssen mit php-dna übereinstimmen |
| `HttpLayerTest` | Header, Query-Strings, JSON-Bodys, Testmodus, Taktung, der echte `java.net.http`-Transport |
| `ErrorMappingTest` | Zuordnung von Gateway-Fehlern, Transportfehler, Validierungsfehler |
| `TelemetryTest` | Maskierung von Zugangsdaten, Klassifizierung, Minimalmodus, Ereignisstruktur |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, PHP-kompatible Formatierung, JSON-Codec |

Kein Test greift auf das Netzwerk außerhalb des lokalen Rechners zu.

<hr style="border: 4px solid #000; border-style: dashed;">

## Rückgabe- und Fehlercodes mit Erklärungen

| Code | Beschreibung                                   | Detail                                                                                                                                                         |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | Befehl erfolgreich ausgeführt                  | Vorgang erfolgreich.                                                                                                                                           |
| 1001 | Befehl erfolgreich ausgeführt; Aktion ausstehend | Vorgang erfolgreich. Der Vorgang befindet sich jedoch derzeit zur Fertigstellung in der Warteschlange.                                                       |
| 2003 | Erforderlicher Parameter fehlt                 | Fehler wegen fehlendem Parameter. Zum Beispiel keine Telefonnummer in den Kontaktinformationen.                                                                |
| 2105 | Objekt ist nicht zur Verlängerung berechtigt   | Der Domainstatus ist für eine Verlängerung nicht geeignet, für Aktualisierungen gesperrt. Der Status darf nicht "clientupdateprohibited" sein. Kann auch an anderen Statusbedingungen liegen. |
| 2200 | Authentifizierungsfehler                       | Autorisierungsfehler, der Sicherheitscode ist falsch oder die Domain ist bei einem anderen Registrar registriert.                                              |
| 2302 | Objekt existiert bereits                       | Domainname oder Nameserver-Informationen sind bereits in der Datenbank vorhanden. Eine Registrierung ist nicht möglich.                                       |
| 2303 | Objekt existiert nicht                         | Domainname oder Nameserver-Informationen sind nicht in der Datenbank vorhanden. Eine Neuregistrierung ist erforderlich.                                       |
| 2304 | Objektstatus verhindert den Vorgang            | Der Domainstatus ist für Aktualisierungen nicht geeignet, für Aktualisierungen gesperrt. Der Status darf nicht "clientupdateprohibited" sein. Kann auch an anderen Statusbedingungen liegen. |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | Systemfehler, Details zu ({0})!                                                                                                      |
| 102  | Mehrere Fehler, Details zu ({0})!                                                                                                    |
| 103  | Unbekannte Fehler, Details zu ({0})!                                                                                                 |
| 200  | API-Befehl nicht gefunden ({0})!                                                                                                     |
| 210  | API-Dienst nicht gefunden ({0})!                                                                                                     |
| 211  | API-Dienstanbieter nicht festgelegt!                                                                                                 |
| 300  | Reseller nicht gefunden!                                                                                                             |
| 301  | Ihre aktuelle IP-Adresse ist nicht für den Zugriff autorisiert. Bitte stellen Sie sicher, dass Sie sich von einer autorisierten IP-Adresse aus verbinden, und versuchen Sie es erneut |
| 310  | TLD wird nicht unterstützt!                                                                                                          |
| 320  | API nicht gefunden!                                                                                                                  |
| 321  | Währung wird nicht unterstützt!                                                                                                      |
| 330  | Erforderliche(r) Parameter nicht festgelegt ({0}).                                                                                   |
| –    | Stellen Sie sicher, dass Sie alle Kontaktobjekte vollständig senden                                                                  |
| 340  | Preisdefinition nicht gefunden ({0}[{1}]-{2}{3}).                                                                                    |
| 350  | Unzureichendes Reseller-Guthaben. (Reseller-ID: {0} - Aktuelles Guthaben: {1} {2}).                                                  |
| 350  | Die Abrechnungswährung stimmt nicht überein oder das Guthaben reicht nicht aus.                                                      |
| 360  | Ungültige API-Anfrage für das Feld ({0}).                                                                                            |
| 360  | API-Kontingent überschritten!                                                                                                        |
| 361  | Drosselungsfehler!                                                                                                                   |
| 362  | Die Premium-Domain kann derzeit nicht registriert werden.                                                                            |
| 363  | Vorgang abgebrochen, da sich die Domain im Zeitraum der automatischen Verlängerung befindet                                          |
| 364  | Diese Domain ist aufgrund eines Problems bei der Registry derzeit nicht für Transaktionen verfügbar                                  |

| CODE | DETAIL                                                                                      |
|------|---------------------------------------------------------------------------------------------|
| 400  | Ungültiger Kontakt ({0}).                                                                   |
| 401  | Kontaktinformationen können nicht synchronisiert werden.                                    |
| 402  | Kein Zugriff auf Kontaktinformationen der Registry.                                         |
| 403  | IM SYSTEM FEHLEN INFORMATIONEN. GEBEN SIE IHRE STANDARDINFORMATIONEN EIN ODER WENDEN SIE SICH AN DAS SUPPORT-TEAM. |
| 404  | DOMAIN-KONTAKT KANN NICHT AKTUALISIERT WERDEN. BERECHTIGUNG DURCH DIE REGISTRY ERFORDERLICH. |
| 410  | Kontakt nicht gefunden.                                                                     |
| 410  | Kontakt nicht gefunden ({0}).                                                               |
| 420  | Ungültiger API-Befehl für Kontakt {0}.                                                      |
| 430  | Kontakt-API nicht gefunden.                                                                 |
| 440  | Kontakt ist nicht synchronisiert.                                                           |
| 450  | Zu viele Domain-Kontakte!                                                                   |
| 451  | Die Kontaktaktualisierung konnte nicht durchgeführt werden.                                 |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | Ungültige Domain-ID.                                                                                                                 |
| 500  | Ungültige Domain-ID ({0}).                                                                                                           |
| 501  | Domain konnte nicht synchronisiert werden ({0})                                                                                      |
| 502  | Interner Transfer fehlgeschlagen                                                                                                     |
| 503  | Domain-Registrierung ist nicht verfügbar.                                                                                            |
| 504  | Domain-Informationen stimmen nicht überein. Vorher: {0}                                                                              |
| 505  | Geben Sie die IP-Adresse ein.                                                                                                        |
| 506  | Der Domain-Transfer wurde gestartet, aber die Kontaktinformationen wurden nicht gelesen.                                             |
| 507  | Autorisierungsfehler.                                                                                                                |
| 510  | Domain nicht gefunden.                                                                                                               |
| 511  | Abgelaufene Domains können nicht gefunden werden.                                                                                    |
| 512  | Domain ist nicht verlängerbar.                                                                                                       |
| 513  | Die Domain befindet sich nicht in einem aktualisierbaren Status. Sie muss aktiv sein, um aktualisiert zu werden                      |
| 514  | Redemption-Periode erwartet.                                                                                                         |
| 520  | Ungültiger API-Befehl für die Domain "{0}".                                                                                          |
| 530  | Ungültige Domain-Laufzeit. Die Laufzeit muss {0} bis {1} Jahre betragen. Die angeforderte Laufzeit beträgt {2} Jahre.                |
| 540  | Die Domain kann nicht über {0} Jahre ab dem aktuellen Datum hinaus verlängert werden.                                                |
| 550  | Ungültiger Domainname. Die Domain muss {0} bis {1} Zeichen lang sein. Der angeforderte Domainname hat {2} Zeichen.                   |
| 560  | Ungültige Anzahl an Nameservern. Die Domain muss {0} bis {1} Nameserver haben. Die angeforderte Anzahl an Nameservern beträgt {2}.   |
| 561  | In der eingehenden Anfrage wurden keine Nameserver-Informationen gefunden                                                            |
| 570  | IDN wird für die TLD "{0}" nicht unterstützt.                                                                                        |
| 571  | Laufzeit ungültig.                                                                                                                   |
| 572  | Befehl ungültig.                                                                                                                     |
| 573  | Domainnamen nicht gefunden.                                                                                                          |
| 574  | TLD-Namen nicht gefunden.                                                                                                            |
| 575  | Die Domain befindet sich nicht in einem aktualisierbaren Status. Sie muss aktiv sein, damit die Nameserver aktualisiert werden können |
| 576  | Die Domain kann erst im letzten Monat vor dem Ablaufdatum verlängert werden                                                          |
| 580  | Transfer wird für die TLD "{0}" nicht unterstützt.                                                                                   |
| 581  | Child-Nameserver nicht gefunden                                                                                                      |
| 582  | Transfer wurde von einem anderen Reseller gestartet.                                                                                 |
| 583  | Transfer nicht initialisiert. Bitte wenden Sie sich an das Support-Team.                                                             |
| 584  | Objektstatus verhindert den Vorgang                                                                                                  |
| 590  | Für diesen Transfer ist ein Auth-Code erforderlich.                                                                                  |
| 591  | Der Auth-Code ist ungültig.                                                                                                          |
| 592  | Auf der Domain besteht eine Transfersperre.                                                                                          |
| 593  | Die Domain kann nicht transferiert werden, Statusinformationen und Transfersperre müssen die erforderlichen Kriterien erfüllen. (Statusinformation: #ok) |
| 594  | Die Nameserver-Adresse der Domain konnte nicht aufgelöst werden ({0})                                                                |
| 595  | Kontaktinformationen können nicht gelesen werden (whois.registrar.tld). Bitte stellen Sie sicher, dass der Datenschutzstatus offen ist |
| 596  | Kontaktinformationen konnten nicht verifiziert werden                                                                                |
| 597  | TLD nicht gefunden                                                                                                                   |
| 598  | Ein unbekannter Fehler ist aufgetreten                                                                                               |
| 599  | Domain-Weiterleitung nicht gefunden                                                                                                  |

## Lizenz

Apache License 2.0 — siehe [LICENSE](LICENSE).
