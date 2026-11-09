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

# java-dna — DomainNameAPI Java Kitabxanası

DomainNameAPI domen reseller REST API-si üçün Java müştəri kitabxanası. Bu,
[php-dna](https://github.com/domainreseller/php-dna) kitabxanasının dəqiq portudur: eyni metodlar, şəbəkəyə göndərilən
eyni sorğular və eyni qaytarılan dəyərlər. Kotlin və digər JVM dilləri ondan birbaşa istifadə edə bilər.

## 📦 Yükləmə — həmişə Releases istifadə edin!

⬇️ **Ən son sınaqdan keçirilmiş versiyanı buradan əldə edin: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ Yaşıl **Code → Download ZIP** düyməsindən **istifadə etməyin** — o, xam inkişaf budağını yükləyir. Release paketləri versiyalanmış, sınaqdan keçirilmiş və istehsal mühitinə hazırdır.

## Quraşdırma və İnteqrasiya Təlimatı

### Minimum Tələblər

- Java 11 və ya daha yuxarı (17 və ya 21 tövsiyə olunur)
- İş vaxtı asılılıqları yoxdur: kitabxana `java.net.http.HttpClient` və kiçik daxili JSON kodlayıcısından istifadə edir.

### A) Release jar faylı

[Releases](https://github.com/domainreseller/java-dna/releases/latest) səhifəsindən `domainnameapi-1.0.0.jar` faylını yükləyin və onu classpath-ə əlavə edin.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Koordinatlar: `io.github.domainreseller:domainnameapi:1.0.0`. Artefakt ictimai repozitoriyada əlçatan olana qədər
onu release mənbə kodundan lokal repozitoriyanıza quraşdırın:

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

## 🔑 API Məlumatları — Reseller ID və API Key

Kitabxana DomainNameAPI REST API-si ilə əlaqə qurur və konstruktora ötürülən dəqiq iki giriş məlumatı tələb edir:

| Parametr | Dəyər |
|---|---|
| Birinci | Reseller ID — `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` formatında UUID |
| İkinci | API Key |

> 💡 **Reseller ID** və **API Key** məlumatlarınızı DomainNameAPI panelinizdə **API Parametrləri** bölməsində tapa bilərsiniz.
> ⚠️ Bunlar **API giriş məlumatlarıdır** — panelə giriş məlumatlarınız burada **işləməyəcək**.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Canlı mühit
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Test (OTE) mühiti
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Hər iki giriş məlumatının əvvəlindəki və sonundakı boşluqlar təmizlənir. Panellər checkbox parametrlərini mətn kimi saxladığı üçün
test rejimi bayrağı sətir (string) də ola bilər: `"1"`, `"true"`, `"on"` və `"yes"` test mühitini, digər bütün dəyərlər isə canlı mühiti seçir.

## Sürətli başlanğıc

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

// İstənilən nəticəni JSON kimi çap et
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

### Qaytarılan dəyərlər

Metodlar php-dna-nın qaytardığı eyni dinamik strukturu `LinkedHashMap` və `ArrayList` əsasında qurub
qaytarır:

- Açar adları php-dna ilə eynidir (`Status`, `LockStatus`, `NameServers`, ...).
- `LockStatus` kimi bayraqlar `"true"` / `"false"` **sətirləridir**.
- Qiymətlər və balanslar **formatlanmış sətirlərdir** (`"11.9900"`, `"601.64"`).
- Rəqəmlər `Integer`, `Long` və ya `Double`; JSON obyektləri `Map<String, Object>`, massivlər isə `List<Object>` tipindədir.
- Əksər metodlar `{"result": "OK", "data": {...}}` və ya `{"result": "ERROR", "error": {...}}` qaytarır.
  İstisnalar: `getCurrentBalance` `OperationResult` zərfindən istifadə edir, `checkAvailability` isə uğurlu
  olduqda sadə `List` qaytarır.

Xətalar istisna (exception) kimi atılmır, qaytarılan dəyər kimi verilir. Bir müştəri nümunəsi hər çağırışın diaqnostika
məlumatlarını saxladığı üçün hər axın (thread) üçün ayrıca nümunə istifadə edin.

## İstifadə

Aşağıdakı bütün nümunələr [examples](examples) qovluğunda işə salına bilən siniflər kimi də mövcuddur:

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Domen Qeydiyyatı

Qeyd: .tr domenləri üçün əlavə parametrlər tələb olunur. `additionalAttributes` parametri .tr kimi
əlavə məlumat tələb edən domen adları üçün istifadə olunur.

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
    1,                                                  // müddət (il)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // boş siyahı = ns1/ns2.domainnameapi.com
    true,                                               // qeydiyyatçı kilidi (registrar lock)
    false                                               // məxfilik qoruması
);

// .tr domenləri: 7-ci parametr kimi TRABIS atributları
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = fərdi (Owner), 0 = şirkət (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // fərdi: şəxsiyyət nömrəsi
// şirkət: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

Əlaqə məlumatları düz (`AddressLine1`, `Phone`, `PhoneCountryCode`) və ya iç-içə
(`Address.Line1`, `Phone.Phone.Number`) strukturda ola bilər. Çatışmayan və ya qarışıq sahələr göndərilməzdən əvvəl
tamamlanır və normallaşdırılır (bax: `validateContact`).

<details>
<summary>Nümunə Çıxış</summary>

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
Qeydiyyat son nöqtəsi qısa bir qəbzlə cavab verir; tam qeyd üçün sonra `getDetails` çağırın.
</details>

### Domen Yenilənməsi

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Nümunə Çıxış</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### Domen Transferi

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Əvvəlcə domenin bu auth kodu ilə transfer edilə bilib-bilmədiyini yoxlayın
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Gözləyən transferlər
dna.cancelTransfer("example.com");   // daxil olan
dna.approveTransfer("example.com");  // çıxan
dna.rejectTransfer("example.com");   // çıxan
```

<details>
<summary>Nümunə Çıxış</summary>

`transfer` qeydiyyatla eyni strukturu qaytarır. `checkTransfer`:

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
Domen transfer edilə bilmədikdə `result` dəyəri `"ERROR"` olur və
`error` belədir: `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` bunu qaytarır:
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Domen Siyahısı

```java
Map<String, Object> all = dna.getList();   // ilk 200 domen

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 0-dan başlayır
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Nümunə Çıxış</summary>

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

### TLD Siyahısı

```java
Map<String, Object> result = dna.getTldList(100);   // standart olaraq 20
```

<details>
<summary>Nümunə Çıxış</summary>

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

### Domen Mövcudluğunun Yoxlanılması

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // TLD-siz etiketlər
    Arrays.asList("com", "net"),           // TLD-lər, nöqtəli və ya nöqtəsiz
    1,                                     // müddət
    "create"                               // əmr (standart olaraq "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Bir çağırışda ən çox 20 domen × TLD kombinasiyası yoxlanıla bilər (gateway limiti).

<details>
<summary>Nümunə Çıxış</summary>

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

### Domen Məlumatları

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // eyni struktur
```

<details>
<summary>Nümunə Çıxış</summary>

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

### Nameserver Dəyişdirmə

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Girişlər göndərilməzdən əvvəl boşluqlardan təmizlənir, kiçik hərflərə çevrilir və təkrarlananlar çıxarılır. `Map` də
qəbul edilir (onun dəyərlərindən istifadə olunur).

<details>
<summary>Nümunə Çıxış</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Domen Kilidini Aktivləşdirmə / Deaktivləşdirmə

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Nümunə Çıxış</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child Nameserver-lər

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Nümunə Çıxış</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` bunu qaytarır: `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Domen Məxfiliyinin Dəyişdirilməsi

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>Nümunə Çıxış</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Domen Əlaqə Məlumatları

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
<summary>Nümunə Çıxış</summary>

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
`saveContacts` bunu qaytarır: `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### DNS Zona Qeydləri

Gateway qeydləri dəstlər (set) şəklində saxlayır (ad + tip). Bir dəstə yazmaq onun bütün dəyərlərini əvəz edir,
istənilən dəyərə görə silmək isə bütün dəsti silir. Aşağıdakı dəyər səviyyəli köməkçi metodlar oxuma, birləşdirmə
və yazma işlərini sizin əvəzinizə görür.

```java
// Siyahı (hər dəyər üçün bir sətir)
dna.getResourceRecords("example.com");

// Digərlərini saxlayaraq BİR dəyəri əlavə et / əvəz et / sil
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Dəstin məhz bu dəyərləri saxlamasını təmin et (boş siyahı dəsti silir)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Aşağı səviyyə: apply=false ilə hazırla, sonra bir dəfəyə tətbiq et
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Adlar `""` / `"@"` (apex), etiket (`"www"`) və ya tam ad (`"www.example.com."`) ola bilər.
Dəstəklənən tiplər: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. TTL
1..86400 saniyə aralığındadır. Gateway ardıcıl sorğu partlayışlarına HTTP 429 ilə cavab verdiyi üçün zona çağırışları
arasında ən azı bir saniyə fasilə saxlanılır (`setZoneMinInterval(seconds)` bu fasiləni dəyişir).

<details>
<summary>Nümunə Çıxış</summary>

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
— dəyər artıq mövcud olduqda: `{..., "Contents": [...], "Changed": false}`.
</details>

### URL Yönləndirmə

```java
dna.setForwarding("example.com", "https://www.example.org");           // Standart yönləndirmə
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // maskalanmış
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Nümunə Çıxış</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Yönləndirmə olmadıqda: `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### Reseller Məlumatlarının və Balansın Alınması

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Nümunə Çıxış</summary>

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

### Köməkçi alətlər (şəbəkə tələb etmir)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` çatışmayan sahələri yer tutucu dəyərlərlə doldurur, adlardan rəqəm və simvolları təmizləyir,
ölkə adlarını ISO kodlarına çevirir və telefon nömrələrini nömrə və ölkə koduna ayırır — yəni
qeydiyyat zamanı tətbiq olunan normallaşdırmanın eynisini həyata keçirir.

<details>
<summary>Nümunə Çıxış</summary>

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

## Xətaların idarə edilməsi

Şəbəkə metodları API və ya şəbəkə problemlərində heç vaxt istisna atmır; əvəzində xəta zərfi qaytarır:

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

- Gateway xətalarında `Code` dəyəri `API_<HTTP status>` formatındadır (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  301/302 cavabı yanlış giriş məlumatları deməkdir (`Invalid API credentials`).
- Gateway sahələr üzrə validasiya xətalarını sadaladıqda `Details` həmin sahələrin adını göstərir, məsələn
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Boş xəta gövdəsi `HTTP 429 - Too Many Requests` və bənzəri mesaja çevrilir.
- Vaxt aşımları, DNS və TLS nasazlıqları da xəta kimi qaytarılır, heç vaxt boş uğurlu cavab kimi yox. Vaxt aşımı
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` mesajı və `API_28` kodu ilə qaytarılır;
  digər nəqliyyat xətaları `Curl error during request: ...` şəklindədir (kodların php-dna ilə uyğun gəlməsi üçün curl xəta
  nömrələri saxlanılır; `getParsedResponseData()` onları `CURL_<n>` kimi göstərir).
- Kitabxana tərəfindəki yoxlamalar öz kodlarından istifadə edir: `RESPONSE` (boş cavab), `DOMAIN_RENEW`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Son çağırışın diaqnostika məlumatları:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // deşifrə olunmuş gövdə və ya {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // deşifrə olunmuş gövdə və ya xətanın {Code, Message, Details} məlumatı
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Test rejimi

OTE test mühitindən (`https://ote.domainresellerapi.com/api/v1`) istifadə etmək üçün konstruktorun üçüncü arqumenti kimi
`true` (və ya `"true"`, `"1"`, `"on"`, `"yes"`) ötürün. `setServiceUrl(url)` müştərini
istənilən digər əsas URL-ə yönləndirir.

## Xəta hesabatları (telemetriya)

API çağırışı uğursuz olduqda kitabxana DomainNameAPI-nin öz xəta izləmə sisteminə (Sentry) xəta hesabatı göndərə bilər.
Uğurlu çağırışların kiçik təsadüfi hissəsi (4%, üstəgəl 1 saniyədən yavaş olan bütün çağırışlar) performans qeydi kimi
göndərilir. Hesabatlar arxa planda göndərilir və çağırışınızı heç vaxt bloklamır və ya pozmur. Xəta hesabatları
**standart olaraq aktivdir**; hadisələr kitabxana versiyası ilə birlikdə `application=DomainNameAPI-Java` teqi ilə işarələnir.

> ℹ️ 1.0.0 versiyasında daxili Sentry ünvanı hələ boşdur (Java layihəsi hələ qurulma
> mərhələsindədir), buna görə `setTelemetryDsn(...)` ilə ünvan konfiqurasiya etməsəniz, heç nə göndərilmir.

**Nə göndərilir:**

- xəta kodu və mesajı
- çağırılan son nöqtənin yolu və HTTP metodu
- çağırışın müddəti
- kitabxana versiyası və Java versiyası
- reseller ID və domen adı
- serverin ictimai IP ünvanı (`api.ipify.org` vasitəsilə müəyyən edilir)
- əməliyyat sisteminin istifadəçi adı
- domenin əlaqə məlumatları daxil olmaqla API sorğusu və cavabı; yalnız giriş məlumatları maskalanır — EPP/auth kodları, API açarları, şifrələr və tokenlər `***` ilə əvəz olunur

**Heç vaxt göndərilməyənlər:** oxuna bilən formada EPP/auth kodları, API açarları, şifrələr və ya tokenlər.

**Niyə və nə qədər müddətə:** məlumatlar yalnız istifadənin ölçülməsi və problemlərin erkən diaqnostikası üçün istifadə olunur, DomainNameAPI-nin öz Sentry serverində 30 gün saxlanılır və sonra avtomatik silinir.

**Necə söndürmək olar:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // heç nə göndərmə
dna.setTelemetryMinimal(true);         // və ya: yalnız kod, mesaj, son nöqtə, müddət, versiyalar, reseller ID
```

> 🔒 **Təhlükəsizlik:** TLS sertifikatının yoxlanılması aktivdir. CA anbarı köhnəlmiş serverdə onu
> `dna.setSslVerification(false);` ilə söndürmək olar — bu tövsiyə olunmur; daha düzgün həll
> Java CA anbarını yeniləməkdir.

## Qabaqcıl imkanlar

- **Xüsusi HTTP qatı:** `dna.setTransport(HttpTransport)` standart `java.net.http`
  nəqliyyat qatını əvəz edir (proksilər, xüsusi TLS, qeydə alma, testlər).
- **Vaxt aşımları:** hər çağırış üçün 30 san, qoşulma üçün 10 san.
- **User-Agent:** hər sorğu `User-Agent: DomainNameAPI-Java/1.0.0` başlığını göndərir; gateway-in qarşısındakı Cloudflare
  standart müştəri agentlərini rədd edir.

## Testlər

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Təsvir |
|------|-------------|
| `ConformanceTest` | php-dna-nın bütün 134 uyğunluq fiksturunu işə salır (`src/test/resources/conformance/`): göndərilən sorğular və qaytarılan dəyərlər php-dna ilə eyni olmalıdır |
| `HttpLayerTest` | Başlıqlar, sorğu sətirləri, JSON gövdələri, test rejimi, çağırış tempi, real `java.net.http` nəqliyyat qatı |
| `ErrorMappingTest` | Gateway xətalarının uyğunlaşdırılması, nəqliyyat xətaları, validasiya xətaları |
| `TelemetryTest` | Giriş məlumatlarının maskalanması, təsnifat, minimal rejim, hadisə strukturu |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, PHP ilə uyğun formatlama, JSON kodlayıcı |

Heç bir test lokal maşından kənar şəbəkəyə müraciət etmir.

<hr style="border: 4px solid #000; border-style: dashed;">

## Qaytarılan və Xəta Kodları və Onların Təsviri

| Kod  | Təsvir                                         | Ətraflı                                                                                                                                                        |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | Əmr uğurla tamamlandı                          | Əməliyyat uğurludur.                                                                                                                                           |
| 1001 | Əmr uğurla tamamlandı; əməliyyat gözləmədədir  | Əməliyyat uğurludur. Lakin əməliyyat hazırda tamamlanmaq üçün növbəyə alınıb.                                                                                  |
| 2003 | Tələb olunan parametr çatışmır                 | Parametr çatışmazlığı xətası. Məsələn, əlaqə məlumatlarında telefon daxil edilməyib.                                                                           |
| 2105 | Obyekt yenilənmə üçün uyğun deyil              | Domen statusu yenilənmə üçün uyğun deyil, yeniləmə əməliyyatları üçün kilidlənib. Status "clientupdateprohibited" olmamalıdır. Digər status şərtlərindən qaynaqlana bilər. |
| 2200 | Autentifikasiya xətası                         | Avtorizasiya xətası; təhlükəsizlik kodu yanlışdır və ya domen başqa qeydiyyatçıda qeydiyyatdadır.                                                              |
| 2302 | Obyekt mövcuddur                               | Domen adı və ya nameserver məlumatı verilənlər bazasında artıq mövcuddur. Qeydiyyat mümkün deyil.                                                             |
| 2303 | Obyekt mövcud deyil                            | Domen adı və ya nameserver məlumatı verilənlər bazasında mövcud deyil. Yeni qeydiyyat tələb olunur.                                                           |
| 2304 | Obyektin statusu əməliyyata icazə vermir       | Domen statusu yeniləmə üçün uyğun deyil, yeniləmə əməliyyatları üçün kilidlənib. Status "clientupdateprohibited" olmamalıdır. Digər status şərtlərindən qaynaqlana bilər. |

| KOD  | ƏTRAFLI                                                                                                                              |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | ({0}) üçün ətraflı sistem xətası!                                                                                                    |
| 102  | ({0}) üçün ətraflı çoxsaylı xətalar!                                                                                                 |
| 103  | ({0}) üçün ətraflı naməlum xətalar!                                                                                                  |
| 200  | API əmri tapılmadı ({0})!                                                                                                            |
| 210  | API xidməti tapılmadı ({0})!                                                                                                         |
| 211  | API xidmət təminatçısı təyin edilməyib!                                                                                              |
| 300  | Reseller tapılmadı!                                                                                                                  |
| 301  | Hazırkı IP ünvanınızın girişə icazəsi yoxdur. Zəhmət olmasa, icazəli IP ünvanından qoşulduğunuzdan əmin olun və yenidən cəhd edin    |
| 310  | TLD dəstəklənmir!                                                                                                                    |
| 320  | API tapılmadı!                                                                                                                       |
| 321  | Valyuta dəstəklənmir!                                                                                                                |
| 330  | Tələb olunan parametr(lər) təyin edilməyib ({0}).                                                                                    |
| –    | Bütün əlaqə obyektlərini tam göndərdiyinizdən əmin olun                                                                              |
| 340  | Qiymət tərifi tapılmadı ({0}[{1}]-{2}{3}).                                                                                           |
| 350  | Reseller balansı kifayət deyil. (Reseller Id : {0} - Hazırkı Balans : {1} {2}).                                                      |
| 350  | Mühasibat valyutası uyğun gəlmir və ya balans kifayət deyil.                                                                         |
| 360  | ({0}) sahəsi üçün yanlış API sorğusu.                                                                                                |
| 360  | API kvotası aşıldı!                                                                                                                  |
| 361  | Sorğu məhdudlaşdırma (throttle) xətası!                                                                                              |
| 362  | Premium domen hazırda qeydiyyat üçün əlçatan deyil.                                                                                  |
| 363  | Domen avtomatik yenilənmə dövründə olduğu üçün əməliyyat ləğv edildi                                                                 |
| 364  | Bu domen reyestrdəki problem səbəbindən hazırda əməliyyat üçün əlçatan deyil                                                         |

| KOD  | ƏTRAFLI                                                                                     |
|------|---------------------------------------------------------------------------------------------|
| 400  | Yanlış əlaqə məlumatı ({0}).                                                                |
| 401  | Əlaqə məlumatı sinxronlaşdırıla bilmir.                                                     |
| 402  | Reyestrdən əlaqə məlumatlarına giriş yoxdur.                                                |
| 403  | SİSTEMDƏ ÇATIŞMAYAN MƏLUMAT VAR. STANDART MƏLUMATLARINIZI DAXİL EDİN VƏ YA DƏSTƏK KOMANDASI İLƏ ƏLAQƏ SAXLAYIN. |
| 404  | DOMENİN ƏLAQƏ MƏLUMATI YENİLƏNƏ BİLMİR. REYESTRDƏN İCAZƏ TƏLƏB OLUNUR.                      |
| 410  | Əlaqə məlumatı tapılmadı.                                                                   |
| 410  | Əlaqə məlumatı tapılmadı ({0}).                                                             |
| 420  | {0} əlaqə məlumatı üçün yanlış API əmri.                                                    |
| 430  | Əlaqə API-si tapılmadı.                                                                     |
| 440  | Əlaqə məlumatı sinxronlaşdırılmayıb.                                                        |
| 450  | Domen əlaqə məlumatları həddindən artıq çoxdur!                                             |
| 451  | Əlaqə məlumatının yenilənməsini davam etdirmək mümkün olmadı.                               |

| KOD  | ƏTRAFLI                                                                                                                              |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | Yanlış domen id.                                                                                                                     |
| 500  | Yanlış domen id ({0}).                                                                                                               |
| 501  | Domen sinxronlaşdırıla bilmədi ({0})                                                                                                 |
| 502  | Daxili transfer uğursuz oldu                                                                                                         |
| 503  | Domen qeydiyyatı mümkün deyil.                                                                                                       |
| 504  | Domen məlumatları uyğun gəlmir. Əvvəlki: {0}                                                                                         |
| 505  | IP ünvanını daxil edin.                                                                                                              |
| 506  | Domen transferi başladılıb, lakin əlaqə məlumatları oxunmayıb.                                                                       |
| 507  | Avtorizasiya xətası.                                                                                                                 |
| 510  | Domen tapılmadı.                                                                                                                     |
| 511  | Müddəti bitmiş domenlər tapıla bilmir.                                                                                               |
| 512  | Domen yenilənə bilməz.                                                                                                               |
| 513  | Domen yenilənə bilən statusda deyil. Yenilənmək üçün aktiv olmalıdır                                                                 |
| 514  | Bərpa (redemption) dövrü gözlənilir.                                                                                                 |
| 520  | "{0}" domeni üçün yanlış API əmri.                                                                                                   |
| 530  | Yanlış domen müddəti. Müddət {0} ilə {1} il arasında olmalıdır. Tələb olunan müddət {2} ildir.                                       |
| 540  | Domen cari tarixdən etibarən {0} ildən artıq uzadıla bilməz.                                                                         |
| 550  | Yanlış domen adı. Domen {0} ilə {1} simvol arasında olmalıdır. Tələb olunan domen adı {2} simvoldur.                                 |
| 560  | Yanlış nameserver sayı. Domenin {0} ilə {1} arasında nameserver-i olmalıdır. Tələb olunan nameserver sayı {2}-dir.                   |
| 561  | Daxil olan sorğuda nameserver məlumatı tapılmadı                                                                                     |
| 570  | "{0}" TLD-si üçün IDN dəstəklənmir.                                                                                                  |
| 571  | Müddət yanlışdır.                                                                                                                    |
| 572  | Əmr yanlışdır.                                                                                                                       |
| 573  | Domen adları tapılmadı.                                                                                                              |
| 574  | TLD adları tapılmadı.                                                                                                                |
| 575  | Domen yenilənə bilən statusda deyil. Nameserver-lərin yenilənməsi üçün aktiv olmalıdır                                               |
| 576  | Domen yalnız bitmə tarixindən əvvəlki son 1 ay ərzində yenilənə bilər                                                                |
| 580  | "{0}" TLD-si üçün transfer dəstəklənmir.                                                                                             |
| 581  | Child nameserver tapılmadı                                                                                                           |
| 582  | Transfer başqa reseller tərəfindən başladılıb.                                                                                       |
| 583  | Transfer başladılmayıb. Zəhmət olmasa, dəstək komandası ilə əlaqə saxlayın.                                                          |
| 584  | Obyektin statusu əməliyyata icazə vermir                                                                                             |
| 590  | Bu transfer üçün auth kodu tələb olunur.                                                                                             |
| 591  | Auth kodu etibarlı deyil.                                                                                                            |
| 592  | Domendə transfer kilidi mövcuddur.                                                                                                   |
| 593  | Domen transfer edilə bilməz; Status Məlumatı və Transfer Kilidi tələb olunan kriteriyalara uyğun olmalıdır. (Status Məlumatı: #ok)   |
| 594  | Domenin nameserver ünvanı həll edilə bilmədi ({0})                                                                                   |
| 595  | Əlaqə məlumatları oxuna bilmir (whois.registrar.tld). Zəhmət olmasa, məxfilik qorumasının açıq olduğundan əmin olun                  |
| 596  | Əlaqə məlumatları təsdiqlənə bilmədi                                                                                                 |
| 597  | TLD tapılmadı                                                                                                                        |
| 598  | Naməlum xəta baş verdi                                                                                                               |
| 599  | Domen yönləndirməsi tapılmadı                                                                                                        |

## Lisenziya

Apache License 2.0 — bax: [LICENSE](LICENSE).
