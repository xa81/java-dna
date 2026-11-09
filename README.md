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

# java-dna — DomainNameAPI Java Library

Java client for the DomainNameAPI domain reseller REST API. It is a faithful port of
[php-dna](https://github.com/domainreseller/php-dna): the same methods, the same requests on
the wire and the same return values. Kotlin and other JVM languages can use it directly.

## 📦 Download — always use Releases!

⬇️ **Get the latest tested version here: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ Do **not** use the green **Code → Download ZIP** button — that downloads the raw development branch. Release packages are versioned, tested and production-ready.

## Installation and Integration Guide

### Minimum Requirements

- Java 11 or higher (Recommended 17 or 21)
- No runtime dependencies: the library uses `java.net.http.HttpClient` and a small built-in JSON codec.

### A) Release jar

Download `domainnameapi-1.0.0.jar` from the [Releases](https://github.com/domainreseller/java-dna/releases/latest) page and put it on your classpath.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Coordinates: `io.github.domainreseller:domainnameapi:1.0.0`. Until the artifact is available from a
public repository, install it into your local repository from the release source:

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

## 🔑 API Credentials — Reseller ID & API Key

The library talks to the DomainNameAPI REST API and needs exactly two credentials, passed to the constructor:

| Parameter | Value |
|---|---|
| First | Reseller ID — UUID like `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Second | API Key |

> 💡 Find your **Reseller ID** and **API Key** in your DomainNameAPI panel under **API Settings**.
> ⚠️ These are **API credentials** — your panel login details will **not** work here.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Live environment
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Test (OTE) environment
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Both credentials are trimmed. The test-mode flag can also be a string, as panels store checkbox
settings: `"1"`, `"true"`, `"on"` and `"yes"` select the test environment, anything else the live one.

## Quick start

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

// Print any result as JSON
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

### Return values

Methods return the same dynamic structure php-dna returns, built from `LinkedHashMap` and
`ArrayList`:

- Key names are identical to php-dna (`Status`, `LockStatus`, `NameServers`, ...).
- Flags such as `LockStatus` are the **strings** `"true"` / `"false"`.
- Prices and balances are **formatted strings** (`"11.9900"`, `"601.64"`).
- Numbers are `Integer`, `Long` or `Double`; JSON objects are `Map<String, Object>`, arrays `List<Object>`.
- Most methods return `{"result": "OK", "data": {...}}` or `{"result": "ERROR", "error": {...}}`.
  Exceptions: `getCurrentBalance` uses an `OperationResult` envelope and `checkAvailability`
  returns a bare `List` on success.

Errors are returned, not thrown. One client instance keeps per-call diagnostics, so use one
instance per thread.

## Usage

All examples below are also available as runnable classes in the [examples](examples) folder:

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Domain Registration

Note: Additional parameters are required for .tr domains. The `additionalAttributes` parameter is
used for domain names that need extra information, such as .tr.

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
    1,                                                  // period (years)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // empty list = ns1/ns2.domainnameapi.com
    true,                                               // registrar lock
    false                                               // privacy protection
);

// .tr domains: TRABIS attributes as the 7th parameter
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = individual (Owner), 0 = company (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // individual: national ID
// company: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

Contacts can be flat (`AddressLine1`, `Phone`, `PhoneCountryCode`) or nested
(`Address.Line1`, `Phone.Phone.Number`). Missing or messy fields are completed and normalised
before sending (see `validateContact`).

<details>
<summary>Sample Output</summary>

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
The register endpoint answers with a short receipt; call `getDetails` afterwards for the full record.
</details>

### Domain Renewal

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Sample Output</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### Domain Transfer

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Check first whether the domain can be transferred with this auth code
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Pending transfers
dna.cancelTransfer("example.com");   // incoming
dna.approveTransfer("example.com");  // outgoing
dna.rejectTransfer("example.com");   // outgoing
```

<details>
<summary>Sample Output</summary>

`transfer` returns the same shape as registration. `checkTransfer`:

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
When the domain is not transferable `result` is `"ERROR"` and
`error` is `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` return
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Domain List

```java
Map<String, Object> all = dna.getList();   // first 200 domains

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 0-based
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Sample Output</summary>

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

### TLD List

```java
Map<String, Object> result = dna.getTldList(100);   // default 20
```

<details>
<summary>Sample Output</summary>

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

### Domain Availability Check

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // labels without TLD
    Arrays.asList("com", "net"),           // TLDs, with or without the dot
    1,                                     // period
    "create"                               // command (default "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

At most 20 domain × TLD combinations per call (gateway limit).

<details>
<summary>Sample Output</summary>

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

### Domain Details

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // same shape
```

<details>
<summary>Sample Output</summary>

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

### Nameserver Modification

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Entries are trimmed, lower-cased and de-duplicated before sending. A `Map` is accepted too
(its values are used).

<details>
<summary>Sample Output</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Enable / Disable Domain Lock

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Sample Output</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child Nameservers

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Sample Output</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` returns `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Modify Domain Privacy

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>Sample Output</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Domain Contacts

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
<summary>Sample Output</summary>

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
`saveContacts` returns `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### DNS Zone Records

The gateway stores records as sets (name + type). Writing a set replaces all of its values, and
deleting by any value removes the whole set. The value-level helpers below read, merge and write
for you.

```java
// List (one row per value)
dna.getResourceRecords("example.com");

// Add / replace / remove ONE value, keeping the others
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Make a set hold exactly these values (an empty list removes it)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Low level: stage with apply=false, then commit once
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Names may be `""` / `"@"` (apex), a label (`"www"`) or a full name (`"www.example.com."`).
Supported types: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. TTL is
1..86400 seconds. Zone calls are kept at least one second apart because the gateway answers
HTTP 429 to bursts (`setZoneMinInterval(seconds)` changes the gap).

<details>
<summary>Sample Output</summary>

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
— when the value is already there: `{..., "Contents": [...], "Changed": false}`.
</details>

### URL Forwarding

```java
dna.setForwarding("example.com", "https://www.example.org");           // Standard redirect
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // masked
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Sample Output</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Without forwarding: `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### Get Reseller Details and Balance

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Sample Output</summary>

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

### Utilities (no network)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` fills missing fields with placeholders, strips digits and symbols from names,
turns country names into ISO codes and splits phone numbers into number and country code — the
same normalisation registration applies.

<details>
<summary>Sample Output</summary>

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

## Error handling

Network methods never throw for API or network problems; they return an error envelope:

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

- `Code` is `API_<HTTP status>` for gateway errors (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  A 301/302 answer means wrong credentials (`Invalid API credentials`).
- When the gateway lists per-field validation errors, `Details` names the fields, e.g.
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- An empty error body becomes `HTTP 429 - Too Many Requests` and similar.
- Timeouts, DNS and TLS failures are errors too, never an empty success. A timeout reads
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` with code `API_28`;
  other transport failures read `Curl error during request: ...` (curl error numbers are kept so
  codes match php-dna; `getParsedResponseData()` shows them as `CURL_<n>`).
- Library-side checks use their own codes: `RESPONSE` (empty answer), `DOMAIN_RENEW`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Diagnostics of the last call:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // decoded body, or {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // decoded body, or {Code, Message, Details} of the error
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Test mode

Pass `true` (or `"true"`, `"1"`, `"on"`, `"yes"`) as the third constructor argument to use the
OTE test environment `https://ote.domainresellerapi.com/api/v1`. `setServiceUrl(url)` points the
client at any other base URL.

## Error reporting (telemetry)

When an API call fails, the library can send an error report to DomainNameAPI's own error tracker
(Sentry). A small random share of successful calls (4%, plus every call slower than 1 second) is
sent as a performance record. Reports are sent in the background and never block or break your
call. Error reporting is **on by default**; events are tagged `application=DomainNameAPI-Java`
with the library version.

> ℹ️ In version 1.0.0 the built-in Sentry address is still empty (the Java project is being set
> up), so nothing is sent unless you configure an address with `setTelemetryDsn(...)`.

**What is sent:**

- error code and message
- called endpoint path and HTTP method
- call duration
- library version and Java version
- reseller ID and domain name
- the server's public IP address (looked up via `api.ipify.org`)
- the operating-system user name
- the API request and response, including the domain's contact details; only credentials are masked — EPP/auth codes, API keys, passwords and tokens are replaced with `***`

**What is never sent:** EPP/auth codes, API keys, passwords or tokens in readable form.

**Why and how long:** the data is used only for usage measurement and early diagnosis of problems, and is kept for 30 days on DomainNameAPI's own Sentry server, after which it is deleted automatically.

**How to turn it off:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // send nothing
dna.setTelemetryMinimal(true);         // or: only code, message, endpoint, timing, versions, reseller ID
```

> 🔒 **Security:** TLS certificate verification is on. On a host with an outdated CA store it can
> be switched off with `dna.setSslVerification(false);` — this is not recommended; updating the
> Java CA store is the better fix.

## Advanced

- **Custom HTTP layer:** `dna.setTransport(HttpTransport)` replaces the default
  `java.net.http` transport (proxies, custom TLS, recording, tests).
- **Timeouts:** 30 s per call, 10 s to connect.
- **User-Agent:** every request sends `User-Agent: DomainNameAPI-Java/1.0.0`; Cloudflare in front of
  the gateway rejects default client agents.

## Testing

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Description |
|------|-------------|
| `ConformanceTest` | Runs all 126 php-dna conformance fixtures (`src/test/resources/conformance/`): the requests sent and the values returned must match php-dna |
| `HttpLayerTest` | Headers, query strings, JSON bodies, test mode, pacing, the real `java.net.http` transport |
| `ErrorMappingTest` | Gateway error mapping, transport failures, validation errors |
| `TelemetryTest` | Credential masking, classification, minimal mode, event shape |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, PHP-compatible formatting, JSON codec |

No test touches the network outside the local machine.

<hr style="border: 4px solid #000; border-style: dashed;">

## Return and Error Codes with Descriptions

| Code | Description                                    | Detail                                                                                                                                                         |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | Command completed successfully                 | Operation successful.                                                                                                                                          |
| 1001 | Command completed successfully; action pending | Operation successful. However, the operation is currently queued for completion.                                                                               |
| 2003 | Required parameter missing                     | Parameter missing error. For example, no phone entry in contact information.                                                                                   |
| 2105 | Object is not eligible for renewal             | Domain status is not suitable for renewal, locked for update operations. Status should not be "clientupdateprohibited". May be due to other status conditions. |
| 2200 | Authentication error                           | Authorization error, security code is incorrect or domain is registered with another registrar.                                                                |
| 2302 | Object exists                                  | Domain name or nameserver information already exists in database. Cannot be registered.                                                                        |
| 2303 | Object does not exist                          | Domain name or nameserver information does not exist in database. New registration required.                                                                   |
| 2304 | Object status prohibits operation              | Domain status is not suitable for updates, locked for update operations. Status should not be "clientupdateprohibited". May be due to other status conditions. |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | System Error Detailed For ({0})!                                                                                                     |
| 102  | Multiple Errors Detailed For ({0})!                                                                                                  |
| 103  | Unknown Errors Detailed For ({0})!                                                                                                   |
| 200  | API command not found ({0})!                                                                                                         |
| 210  | API service not found ({0})!                                                                                                         |
| 211  | API service provider not set!                                                                                                        |
| 300  | Reseller not found!                                                                                                                  |
| 301  | Your current IP address is not authorized to access. Please make sure you are connecting from an authorized IP address and try again |
| 310  | TLD is not supported!                                                                                                                |
| 320  | API not found!                                                                                                                       |
| 321  | Currency is not supported!                                                                                                           |
| 330  | Required parameter(s) not set ({0}).                                                                                                 |
| –    | Make sure you send all contact objects full                                                                                          |
| 340  | Price definition not found ({0}[{1}]-{2}{3}).                                                                                        |
| 350  | Insufficient reseller balance. (Reseller Id : {0} - Current Balance : {1} {2}).                                                      |
| 350  | Accounting currency does not match or the balance is not sufficient.                                                                 |
| 360  | Invalid API request for field ({0}).                                                                                                 |
| 360  | API quota exceeded!                                                                                                                  |
| 361  | Throttled error!                                                                                                                     |
| 362  | Premium domain is not available to register right now.                                                                               |
| 363  | Operation cancelled because domain in auto-renewal period                                                                            |
| 364  | This domain is currently unavailable for transaction due to a problem with the registry                                              |

| CODE | DETAIL                                                                                      |
|------|---------------------------------------------------------------------------------------------|
| 400  | Invalid contact ({0}).                                                                      |
| 401  | Contact info can not sync.                                                                  |
| 402  | No access to contact information from registry.                                             |
| 403  | THE SYSTEM HAS MISSING INFORMATION. ENTER YOUR DEFAULT INFORMATION OR CONTACT SUPPORT TEAM. |
| 404  | DOMAIN CONTACT CANNOT UPDATE. PERMISSION FROM THE REGISTRY.                                 |
| 410  | Contact not found.                                                                          |
| 410  | Contact not found ({0}).                                                                    |
| 420  | Invalid Api command for contact {0}.                                                        |
| 430  | Contact api not found.                                                                      |
| 440  | Contact is not synced.                                                                      |
| 450  | Too many domain contacts!                                                                   |
| 451  | Failed to proceed with contact update.                                                      |

| CODE | DETAIL                                                                                                                               |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | Invalid domain id.                                                                                                                   |
| 500  | Invalid domain id ({0}).                                                                                                             |
| 501  | Domain could not synchronized({0})                                                                                                   |
| 502  | Internal transfer failed                                                                                                             |
| 503  | Domain registration is not available.                                                                                                |
| 504  | Domain information does not match. Before :{0}                                                                                       |
| 505  | Enter the Ip Address.                                                                                                                |
| 506  | Domain Transfer Has Been Started But Contact Info Not Read.                                                                          |
| 507  | Authorization error.                                                                                                                 |
| 510  | Domain not found.                                                                                                                    |
| 511  | Expired domains cannot be found.                                                                                                     |
| 512  | Domain is not renewable.                                                                                                             |
| 513  | Domain is not in updateable status. It must be active for to be updated                                                              |
| 514  | Redemption Period Expected.                                                                                                          |
| 520  | Invalid Api command for domain "{0}".                                                                                                |
| 530  | Invalid domain period. Period must be {0} to {1} years. Requested period is {2} years.                                               |
| 540  | Domain cannot be extended beyond {0} years from current date.                                                                        |
| 550  | Invalid domain name. Domain must be {0} to {1} characters length. Requested domain name is {2} characters.                           |
| 560  | Invalid name server count. Domain must have {0} to {1} name servers. Requested name server count is {2}.                             |
| 561  | No name server information found in the incoming request                                                                             |
| 570  | Idn not supported for tld "{0}".                                                                                                     |
| 571  | Period invalid.                                                                                                                      |
| 572  | Command invalid.                                                                                                                     |
| 573  | Domain names not found.                                                                                                              |
| 574  | TLD names not found.                                                                                                                 |
| 575  | Domain is not in updateable status. It must be active for name servers to be updated                                                 |
| 576  | Domain can be renewed in the last 1 month before the expire date                                                                     |
| 580  | Transfer not supported for tld "{0}".                                                                                                |
| 581  | Child name server not found                                                                                                          |
| 582  | Transfer started by other reseller.                                                                                                  |
| 583  | Transfer not initialized. Please contact the support team.                                                                           |
| 584  | Object status prohibits operation                                                                                                    |
| 590  | Auth code is required for this transfer.                                                                                             |
| 591  | Auth code is not valid.                                                                                                              |
| 592  | Transfer lock exists on domain.                                                                                                      |
| 593  | Domain cannot be transferred, Status Information and Transfer Lock must comply with the required criteria. (Status Information: #ok) |
| 594  | Domain name server address could not resolved ({0})                                                                                  |
| 595  | Contact information can not be read (whois.registrar.tld) Please make sure that privacy protection status open                       |
| 596  | Contact information could not verified                                                                                               |
| 597  | Tld Not Found                                                                                                                        |
| 598  | Unknown error occurred                                                                                                               |
| 599  | Domain Forward Not Found                                                                                                             |

## License

Apache License 2.0 — see [LICENSE](LICENSE).
