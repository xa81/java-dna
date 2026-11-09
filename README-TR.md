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

# java-dna — DomainNameAPI Java Kütüphanesi

DomainNameAPI alan adı bayilik (reseller) REST API'si için Java istemcisi.
[php-dna](https://github.com/domainreseller/php-dna) kütüphanesinin birebir uyarlamasıdır: aynı metotlar, ağa giden
aynı istekler ve aynı dönüş değerleri. Kotlin ve diğer JVM dilleri tarafından doğrudan kullanılabilir.

## 📦 İndirme — her zaman Releases kullanın!

⬇️ **Test edilmiş en güncel sürümü buradan edinin: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ Yeşil **Code → Download ZIP** düğmesini **kullanmayın** — bu düğme ham geliştirme dalını indirir. Release paketleri sürümlenmiş, test edilmiş ve canlı ortama hazırdır.

## Kurulum ve Entegrasyon Rehberi

### Minimum Gereksinimler

- Java 11 veya üzeri (17 veya 21 önerilir)
- Çalışma zamanı bağımlılığı yoktur: kütüphane `java.net.http.HttpClient` ve küçük, yerleşik bir JSON kodlayıcı/çözücü kullanır.

### A) Release jar dosyası

[Releases](https://github.com/domainreseller/java-dna/releases/latest) sayfasından `domainnameapi-1.0.0.jar` dosyasını indirin ve classpath'inize ekleyin.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Koordinatlar: `io.github.domainreseller:domainnameapi:1.0.0`. Artifact herkese açık bir depoda yayınlanana
kadar, release kaynak kodundan yerel deponuza kurun:

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

## 🔑 API Bilgileri — Reseller ID ve API Key

Kütüphane DomainNameAPI REST API'si ile haberleşir ve yapıcı metoda (constructor) verilen tam olarak iki kimlik bilgisine ihtiyaç duyar:

| Parametre | Değer |
|---|---|
| Birinci | Reseller ID — `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` biçiminde bir UUID |
| İkinci | API Key |

> 💡 **Reseller ID** ve **API Key** bilgilerinizi DomainNameAPI panelinizde **API Ayarları** bölümünde bulabilirsiniz.
> ⚠️ Bunlar **API kimlik bilgileridir** — panel giriş bilgileriniz burada **çalışmaz**.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Canlı ortam
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Test (OTE) ortamı
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Her iki kimlik bilgisinin de baştaki ve sondaki boşlukları temizlenir. Paneller onay kutusu ayarlarını metin olarak sakladığından,
test modu bayrağı string de olabilir: `"1"`, `"true"`, `"on"` ve `"yes"` test ortamını, diğer tüm değerler canlı ortamı seçer.

## Hızlı başlangıç

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

// Herhangi bir sonucu JSON olarak yazdır
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

### Dönüş değerleri

Metotlar, php-dna'nın döndürdüğü dinamik yapının aynısını `LinkedHashMap` ve `ArrayList` ile
oluşturarak döndürür:

- Anahtar adları php-dna ile birebir aynıdır (`Status`, `LockStatus`, `NameServers`, ...).
- `LockStatus` gibi bayraklar `"true"` / `"false"` **string** değerleridir.
- Fiyatlar ve bakiyeler **biçimlendirilmiş string** değerlerdir (`"11.9900"`, `"601.64"`).
- Sayılar `Integer`, `Long` veya `Double`; JSON nesneleri `Map<String, Object>`, diziler ise `List<Object>` olarak gelir.
- Çoğu metot `{"result": "OK", "data": {...}}` veya `{"result": "ERROR", "error": {...}}` döndürür.
  İstisnalar: `getCurrentBalance` bir `OperationResult` zarfı kullanır, `checkAvailability` ise başarılı
  olduğunda yalın bir `List` döndürür.

Hatalar istisna (exception) olarak fırlatılmaz, dönüş değeri olarak verilir. Bir istemci örneği her çağrıya ait tanılama
bilgilerini tuttuğu için her iş parçacığında (thread) ayrı bir örnek kullanın.

## Kullanım

Aşağıdaki tüm örnekler [examples](examples) klasöründe çalıştırılabilir sınıflar olarak da mevcuttur:

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Domain Kaydı

Not: .tr alan adları için ek parametreler gereklidir. `additionalAttributes` parametresi, .tr gibi
ek bilgi gerektiren alan adları için kullanılır.

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
    1,                                                  // süre (yıl)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // boş liste = ns1/ns2.domainnameapi.com
    true,                                               // kayıt kilidi (registrar lock)
    false                                               // gizlilik koruması
);

// .tr alan adları: 7. parametre olarak TRABIS öznitelikleri
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = bireysel (Owner), 0 = kurumsal (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // bireysel: T.C. kimlik numarası
// kurumsal: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

İletişim bilgileri düz (`AddressLine1`, `Phone`, `PhoneCountryCode`) ya da iç içe
(`Address.Line1`, `Phone.Phone.Number`) yapıda olabilir. Eksik veya düzensiz alanlar gönderilmeden önce
tamamlanır ve normalleştirilir (bkz. `validateContact`).

<details>
<summary>Örnek Çıktı</summary>

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
Kayıt uç noktası kısa bir makbuzla yanıt verir; kaydın tamamı için ardından `getDetails` çağırın.
</details>

### Domain Yenileme

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Örnek Çıktı</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### Domain Transferi

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Önce alan adının bu auth koduyla transfer edilip edilemeyeceğini kontrol edin
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Bekleyen transferler
dna.cancelTransfer("example.com");   // gelen
dna.approveTransfer("example.com");  // giden
dna.rejectTransfer("example.com");   // giden
```

<details>
<summary>Örnek Çıktı</summary>

`transfer`, kayıt işlemiyle aynı yapıyı döndürür. `checkTransfer`:

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
Alan adı transfer edilemiyorsa `result` değeri `"ERROR"` olur ve
`error` şu şekildedir: `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` şunu döndürür:
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Alan Adı Listesi

```java
Map<String, Object> all = dna.getList();   // ilk 200 alan adı

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 0'dan başlar
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Örnek Çıktı</summary>

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

### TLD Listesi

```java
Map<String, Object> result = dna.getTldList(100);   // varsayılan 20
```

<details>
<summary>Örnek Çıktı</summary>

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

### Alan Adı Uygunluk Kontrolü

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // TLD'siz etiketler
    Arrays.asList("com", "net"),           // TLD'ler, noktalı veya noktasız
    1,                                     // süre
    "create"                               // komut (varsayılan "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Tek çağrıda en fazla 20 alan adı × TLD kombinasyonu sorgulanabilir (gateway sınırı).

<details>
<summary>Örnek Çıktı</summary>

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

### Alan Adı Detayları

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // aynı yapı
```

<details>
<summary>Örnek Çıktı</summary>

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

### Nameserver Düzenleme

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Girdiler gönderilmeden önce boşluklardan arındırılır, küçük harfe çevrilir ve tekrar edenler ayıklanır. Bir `Map` de
kabul edilir (değerleri kullanılır).

<details>
<summary>Örnek Çıktı</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Domain Kilidini Aktifleştirme / Kaldırma

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Örnek Çıktı</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child Nameserver'lar

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Örnek Çıktı</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` şunu döndürür: `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Domain Gizliliğini Değiştirme

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>Örnek Çıktı</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Domain İletişim Bilgileri

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
<summary>Örnek Çıktı</summary>

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
`saveContacts` şunu döndürür: `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### DNS Zone Kayıtları

Gateway kayıtları küme (set) olarak saklar (ad + tür). Bir kümeye yazmak, kümenin tüm değerlerini değiştirir;
herhangi bir değere göre silmek ise kümenin tamamını kaldırır. Aşağıdaki değer düzeyindeki yardımcı metotlar okuma,
birleştirme ve yazma işlemlerini sizin yerinize yapar.

```java
// Listele (her değer için bir satır)
dna.getResourceRecords("example.com");

// Diğerlerini koruyarak TEK bir değer ekle / değiştir / kaldır
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Bir kümenin tam olarak bu değerleri içermesini sağla (boş liste kümeyi kaldırır)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Düşük seviye: apply=false ile hazırla, ardından tek seferde uygula
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Adlar `""` / `"@"` (apex), bir etiket (`"www"`) ya da tam ad (`"www.example.com."`) olabilir.
Desteklenen türler: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. TTL
1..86400 saniye aralığındadır. Gateway art arda gelen isteklere HTTP 429 ile yanıt verdiğinden, zone çağrıları arasında
en az bir saniye bırakılır (`setZoneMinInterval(seconds)` bu aralığı değiştirir).

<details>
<summary>Örnek Çıktı</summary>

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
— değer zaten mevcutsa: `{..., "Contents": [...], "Changed": false}`.
</details>

### URL Yönlendirme

```java
dna.setForwarding("example.com", "https://www.example.org");           // Standart yönlendirme
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // maskeli
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Örnek Çıktı</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Yönlendirme yoksa: `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### Bayi Bilgileri ve Bakiye Sorgulama

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Örnek Çıktı</summary>

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

### Yardımcı araçlar (ağ bağlantısı gerektirmez)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` eksik alanları yer tutucu değerlerle doldurur, adlardaki rakam ve sembolleri temizler,
ülke adlarını ISO kodlarına dönüştürür ve telefon numaralarını numara ve ülke kodu olarak ayırır — yani
kayıt işleminin uyguladığı normalleştirmenin aynısını yapar.

<details>
<summary>Örnek Çıktı</summary>

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

## Hata yönetimi

Ağ metotları API veya ağ sorunlarında hiçbir zaman istisna fırlatmaz; bunun yerine bir hata zarfı döndürür:

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

- Gateway hatalarında `Code` değeri `API_<HTTP status>` (HTTP durum kodu) biçimindedir (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  301/302 yanıtı hatalı kimlik bilgileri anlamına gelir (`Invalid API credentials`).
- Gateway alan bazında doğrulama hatalarını listelediğinde, `Details` ilgili alanları belirtir, örneğin
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Boş bir hata gövdesi `HTTP 429 - Too Many Requests` ve benzeri bir mesaja dönüştürülür.
- Zaman aşımları, DNS ve TLS hataları da hata olarak döner; hiçbir zaman boş bir başarı yanıtı olarak değil. Zaman aşımı
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` mesajı ve `API_28` koduyla döner;
  diğer aktarım hataları `Curl error during request: ...` şeklindedir (kodların php-dna ile eşleşmesi için curl hata
  numaraları korunur; `getParsedResponseData()` bunları `CURL_<n>` olarak gösterir).
- Kütüphane tarafındaki kontroller kendi kodlarını kullanır: `RESPONSE` (boş yanıt), `DOMAIN_RENEW`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Son çağrıya ait tanılama bilgileri:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // çözümlenmiş gövde veya {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // çözümlenmiş gövde veya hatanın {Code, Message, Details} bilgisi
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Test modu

OTE test ortamını (`https://ote.domainresellerapi.com/api/v1`) kullanmak için yapıcı metodun üçüncü argümanı olarak
`true` (veya `"true"`, `"1"`, `"on"`, `"yes"`) verin. `setServiceUrl(url)` ile istemciyi
başka herhangi bir temel URL'ye yönlendirebilirsiniz.

## Hata raporlama (telemetri)

Bir API çağrısı başarısız olduğunda kütüphane, DomainNameAPI'nin kendi hata izleme sistemine (Sentry) bir hata raporu
gönderebilir. Başarılı çağrıların küçük ve rastgele bir kısmı (%4 ile birlikte 1 saniyeden yavaş süren tüm çağrılar)
performans kaydı olarak gönderilir. Raporlar arka planda gönderilir; çağrınızı asla bekletmez veya bozmaz.
Hata raporlama **varsayılan olarak açıktır**; olaylar kütüphane sürümüyle birlikte `application=DomainNameAPI-Java`
etiketiyle işaretlenir.

> ℹ️ 1.0.0 sürümünde yerleşik Sentry adresi henüz boştur (Java projesi hâlâ kurulma
> aşamasındadır); bu nedenle `setTelemetryDsn(...)` ile bir adres yapılandırmadığınız sürece hiçbir şey gönderilmez.

**Gönderilen bilgiler:**

- hata kodu ve mesajı
- çağrılan uç noktanın yolu ve HTTP metodu
- çağrı süresi
- kütüphane sürümü ve Java sürümü
- reseller ID ve alan adı
- sunucunun genel (public) IP adresi (`api.ipify.org` üzerinden sorgulanır)
- işletim sistemi kullanıcı adı
- alan adının iletişim bilgileri dahil API isteği ve yanıtı; yalnızca kimlik bilgileri maskelenir — EPP/auth kodları, API anahtarları, parolalar ve token'lar `***` ile değiştirilir

**Asla gönderilmeyenler:** okunabilir biçimde EPP/auth kodları, API anahtarları, parolalar veya token'lar.

**Neden ve ne kadar süreyle:** veriler yalnızca kullanım ölçümü ve sorunların erken teşhisi için kullanılır; DomainNameAPI'nin kendi Sentry sunucusunda 30 gün saklanır ve ardından otomatik olarak silinir.

**Nasıl kapatılır:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // hiçbir şey gönderme
dna.setTelemetryMinimal(true);         // veya: yalnızca kod, mesaj, uç nokta, süre, sürümler, reseller ID
```

> 🔒 **Güvenlik:** TLS sertifika doğrulaması açıktır. CA deposu güncel olmayan bir sunucuda
> `dna.setSslVerification(false);` ile kapatılabilir — bu önerilmez; daha doğru çözüm
> Java CA deposunu güncellemektir.

## İleri düzey

- **Özel HTTP katmanı:** `dna.setTransport(HttpTransport)` varsayılan `java.net.http`
  aktarım katmanının yerini alır (proxy'ler, özel TLS, kayıt alma, testler).
- **Zaman aşımları:** çağrı başına 30 sn, bağlantı için 10 sn.
- **User-Agent:** her istek `User-Agent: DomainNameAPI-Java/1.0.0` başlığını gönderir; gateway'in önündeki Cloudflare
  varsayılan istemci tanımlayıcılarını reddeder.

## Testler

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Açıklama |
|------|-------------|
| `ConformanceTest` | php-dna'nın 126 uyumluluk fikstürünün tamamını çalıştırır (`src/test/resources/conformance/`): gönderilen istekler ve döndürülen değerler php-dna ile birebir eşleşmelidir |
| `HttpLayerTest` | Başlıklar, sorgu dizeleri, JSON gövdeleri, test modu, çağrı aralığı denetimi, gerçek `java.net.http` aktarım katmanı |
| `ErrorMappingTest` | Gateway hata eşlemesi, aktarım hataları, doğrulama hataları |
| `TelemetryTest` | Kimlik bilgisi maskeleme, sınıflandırma, minimal mod, olay yapısı |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, PHP uyumlu biçimlendirme, JSON kodlayıcı/çözücü |

Hiçbir test yerel makine dışındaki ağa erişmez.

<hr style="border: 4px solid #000; border-style: dashed;">

## Dönüş ve Hata Kodları ile Açıklamaları

| Kod  | Açıklama                                       | Detay                                                                                                                                                          |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | Komut başarıyla tamamlandı                     | İşlem başarılı.                                                                                                                                                |
| 1001 | Komut başarıyla tamamlandı; işlem beklemede    | İşlem başarılı. Ancak işlem şu anda tamamlanmak üzere kuyruğa alınmış durumda.                                                                                 |
| 2003 | Gerekli parametre eksik                        | Parametre eksik hatası. Örneğin, iletişim bilgilerinde telefon girişi yapılmamış.                                                                              |
| 2105 | Nesne yenileme için uygun değil                | Domain durumu yenilemeye uygun değil, güncelleme işlemlerine karşı kilitli. Durum "clientupdateprohibited" olmamalıdır. Başka durum koşullarından kaynaklanıyor olabilir. |
| 2200 | Kimlik doğrulama hatası                        | Yetkilendirme hatası; güvenlik kodu hatalı veya domain başka bir kayıt firmasında kayıtlı.                                                                     |
| 2302 | Nesne mevcut                                   | Domain adı veya nameserver bilgisi veritabanında zaten mevcut. Kayıt yapılamaz.                                                                                |
| 2303 | Nesne mevcut değil                             | Domain adı veya nameserver bilgisi veritabanında mevcut değil. Yeni kayıt gereklidir.                                                                          |
| 2304 | Nesne durumu işleme izin vermiyor              | Domain durumu güncellemeye uygun değil, güncelleme işlemlerine karşı kilitli. Durum "clientupdateprohibited" olmamalıdır. Başka durum koşullarından kaynaklanıyor olabilir. |

| KOD  | DETAY                                                                                                                                |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | ({0}) için ayrıntılı sistem hatası!                                                                                                  |
| 102  | ({0}) için ayrıntılı çoklu hata!                                                                                                     |
| 103  | ({0}) için ayrıntılı bilinmeyen hata!                                                                                                |
| 200  | API komutu bulunamadı ({0})!                                                                                                         |
| 210  | API servisi bulunamadı ({0})!                                                                                                        |
| 211  | API servis sağlayıcısı ayarlanmamış!                                                                                                 |
| 300  | Bayi bulunamadı!                                                                                                                     |
| 301  | Mevcut IP adresinizin erişim yetkisi yok. Lütfen yetkili bir IP adresinden bağlandığınızdan emin olun ve tekrar deneyin              |
| 310  | TLD desteklenmiyor!                                                                                                                  |
| 320  | API bulunamadı!                                                                                                                      |
| 321  | Para birimi desteklenmiyor!                                                                                                          |
| 330  | Gerekli parametre(ler) ayarlanmamış ({0}).                                                                                           |
| –    | Tüm iletişim nesnelerini eksiksiz gönderdiğinizden emin olun                                                                         |
| 340  | Fiyat tanımı bulunamadı ({0}[{1}]-{2}{3}).                                                                                           |
| 350  | Yetersiz bayi bakiyesi. (Bayi Id : {0} - Mevcut Bakiye : {1} {2}).                                                                   |
| 350  | Muhasebe para birimi eşleşmiyor veya bakiye yetersiz.                                                                                |
| 360  | ({0}) alanı için geçersiz API isteği.                                                                                                |
| 360  | API kotası aşıldı!                                                                                                                   |
| 361  | İstek sınırlama (throttle) hatası!                                                                                                   |
| 362  | Premium domain şu anda kayıt için uygun değil.                                                                                       |
| 363  | Domain otomatik yenileme döneminde olduğu için işlem iptal edildi                                                                    |
| 364  | Bu domain, kayıt otoritesindeki (registry) bir sorun nedeniyle şu anda işlem için kullanılamıyor                                    |

| KOD  | DETAY                                                                                       |
|------|---------------------------------------------------------------------------------------------|
| 400  | Geçersiz iletişim bilgisi ({0}).                                                            |
| 401  | İletişim bilgisi senkronize edilemiyor.                                                     |
| 402  | Kayıt otoritesinden (registry) iletişim bilgilerine erişim yok.                             |
| 403  | SİSTEMDE EKSİK BİLGİ VAR. VARSAYILAN BİLGİLERİNİZİ GİRİN VEYA DESTEK EKİBİYLE İLETİŞİME GEÇİN. |
| 404  | DOMAIN İLETİŞİM BİLGİSİ GÜNCELLENEMİYOR. KAYIT OTORİTESİNDEN (REGISTRY) İZİN GEREKİYOR.     |
| 410  | İletişim bilgisi bulunamadı.                                                                |
| 410  | İletişim bilgisi bulunamadı ({0}).                                                          |
| 420  | {0} iletişim bilgisi için geçersiz API komutu.                                              |
| 430  | İletişim API'si bulunamadı.                                                                 |
| 440  | İletişim bilgisi senkronize edilmemiş.                                                      |
| 450  | Çok fazla domain iletişim bilgisi!                                                          |
| 451  | İletişim bilgisi güncellemesine devam edilemedi.                                            |

| KOD  | DETAY                                                                                                                                |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | Geçersiz domain id.                                                                                                                  |
| 500  | Geçersiz domain id ({0}).                                                                                                            |
| 501  | Domain senkronize edilemedi ({0})                                                                                                    |
| 502  | Dahili transfer başarısız oldu                                                                                                       |
| 503  | Domain kaydı yapılamıyor.                                                                                                            |
| 504  | Domain bilgileri eşleşmiyor. Önceki: {0}                                                                                             |
| 505  | IP adresini girin.                                                                                                                   |
| 506  | Domain transferi başlatıldı ancak iletişim bilgileri okunamadı.                                                                      |
| 507  | Yetkilendirme hatası.                                                                                                                |
| 510  | Domain bulunamadı.                                                                                                                   |
| 511  | Süresi dolmuş domainler bulunamıyor.                                                                                                 |
| 512  | Domain yenilenemez.                                                                                                                  |
| 513  | Domain güncellenebilir durumda değil. Güncellenebilmesi için aktif olmalıdır                                                         |
| 514  | Geri alım (redemption) dönemi bekleniyor.                                                                                            |
| 520  | "{0}" domaini için geçersiz API komutu.                                                                                              |
| 530  | Geçersiz domain süresi. Süre {0} ile {1} yıl arasında olmalıdır. İstenen süre {2} yıl.                                               |
| 540  | Domain, mevcut tarihten itibaren {0} yıldan daha uzun süreye uzatılamaz.                                                             |
| 550  | Geçersiz domain adı. Domain {0} ile {1} karakter uzunluğunda olmalıdır. İstenen domain adı {2} karakter.                             |
| 560  | Geçersiz nameserver sayısı. Domain {0} ile {1} arasında nameserver'a sahip olmalıdır. İstenen nameserver sayısı {2}.                 |
| 561  | Gelen istekte nameserver bilgisi bulunamadı                                                                                          |
| 570  | "{0}" TLD'si için IDN desteklenmiyor.                                                                                                |
| 571  | Süre geçersiz.                                                                                                                       |
| 572  | Komut geçersiz.                                                                                                                      |
| 573  | Domain adları bulunamadı.                                                                                                            |
| 574  | TLD adları bulunamadı.                                                                                                               |
| 575  | Domain güncellenebilir durumda değil. Nameserver'ların güncellenebilmesi için aktif olmalıdır                                        |
| 576  | Domain, bitiş tarihinden önceki son 1 ay içinde yenilenebilir                                                                        |
| 580  | "{0}" TLD'si için transfer desteklenmiyor.                                                                                           |
| 581  | Child nameserver bulunamadı                                                                                                          |
| 582  | Transfer başka bir bayi tarafından başlatıldı.                                                                                       |
| 583  | Transfer başlatılamadı. Lütfen destek ekibiyle iletişime geçin.                                                                      |
| 584  | Nesne durumu işleme izin vermiyor                                                                                                    |
| 590  | Bu transfer için auth kodu gereklidir.                                                                                               |
| 591  | Auth kodu geçerli değil.                                                                                                             |
| 592  | Domain üzerinde transfer kilidi mevcut.                                                                                              |
| 593  | Domain transfer edilemiyor; Durum Bilgisi ve Transfer Kilidi gerekli kriterlere uygun olmalıdır. (Durum Bilgisi: #ok)                |
| 594  | Domain nameserver adresi çözümlenemedi ({0})                                                                                         |
| 595  | İletişim bilgileri okunamıyor (whois.registrar.tld). Lütfen gizlilik korumasının açık olduğundan emin olun                           |
| 596  | İletişim bilgileri doğrulanamadı                                                                                                     |
| 597  | TLD bulunamadı                                                                                                                       |
| 598  | Bilinmeyen bir hata oluştu                                                                                                           |
| 599  | Domain yönlendirmesi bulunamadı                                                                                                      |

## Lisans

Apache License 2.0 — bkz. [LICENSE](LICENSE).
