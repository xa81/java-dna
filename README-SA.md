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

# java-dna — مكتبة DomainNameAPI للغة Java

عميل Java لواجهة REST API الخاصة بموزعي النطاقات لدى DomainNameAPI. وهي نقل أمين لمكتبة
[php-dna](https://github.com/domainreseller/php-dna): الدوال نفسها، والطلبات نفسها المُرسَلة عبر الشبكة،
والقيم المُعادة نفسها. ويمكن استخدامها مباشرةً من Kotlin ومن لغات JVM الأخرى.

## 📦 التنزيل — استخدم دائمًا صفحة الإصدارات (Releases)!

⬇️ **احصل على أحدث نسخة مختبَرة من هنا: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ **لا** تستخدم الزر الأخضر **Code → Download ZIP** — فهو ينزّل فرع التطوير الخام. حزم الإصدارات مرقّمة الإصدار ومختبَرة وجاهزة لبيئة الإنتاج.

## دليل التثبيت والتكامل

### الحد الأدنى من المتطلبات

- Java 11 أو أعلى (يوصى بـ 17 أو 21)
- لا توجد أي اعتماديات وقت التشغيل: تستخدم المكتبة `java.net.http.HttpClient` ومُرمِّز JSON صغيرًا مدمجًا.

### أ) ملف jar من صفحة الإصدارات

نزّل الملف `domainnameapi-1.0.0.jar` من صفحة [الإصدارات (Releases)](https://github.com/domainreseller/java-dna/releases/latest) وأضفه إلى مسار الأصناف (classpath) لديك.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### ب) Maven / Gradle

الإحداثيات: `io.github.domainreseller:domainnameapi:1.0.0`. وإلى أن تتوفر الحزمة في مستودع
عام، ثبّتها في مستودعك المحلي انطلاقًا من الشيفرة المصدرية للإصدار:

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

## 🔑 بيانات اعتماد API — Reseller ID و API Key

تتواصل المكتبة مع DomainNameAPI REST API وتحتاج إلى بيانات اعتماد اثنتين فقط تُمرَّران إلى الدالة الإنشائية (constructor):

| المُعامِل | القيمة |
|---|---|
| الأول | Reseller ID — بصيغة UUID مثل `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| الثاني | API Key |

> 💡 ستجد **Reseller ID** و **API Key** في لوحة DomainNameAPI الخاصة بك ضمن **إعدادات API**.
> ⚠️ هذه **بيانات اعتماد API** — بيانات تسجيل الدخول إلى اللوحة **لن تعمل** هنا.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// البيئة الحية
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// بيئة الاختبار (OTE)
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

تُزال المسافات الزائدة من طرفي كلتا القيمتين. ويمكن أيضًا تمرير علامة وضع الاختبار كسلسلة نصية، كما تخزّن
لوحات التحكم إعدادات مربعات الاختيار: القيم `"1"` و`"true"` و`"on"` و`"yes"` تختار بيئة الاختبار، وأي قيمة أخرى تختار البيئة الحية.

## البدء السريع

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

// طباعة أي نتيجة بصيغة JSON
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

### القيم المُعادة

تُعيد الدوال البنية الديناميكية نفسها التي تُعيدها php-dna، مبنيةً من `LinkedHashMap` و
`ArrayList`:

- أسماء المفاتيح مطابقة تمامًا لـ php-dna ‏(`Status`، `LockStatus`، `NameServers`، ...).
- العلامات مثل `LockStatus` هي **سلاسل نصية** `"true"` / `"false"`.
- الأسعار والأرصدة **سلاسل نصية مُنسَّقة** (`"11.9900"`، `"601.64"`).
- الأرقام من النوع `Integer` أو `Long` أو `Double`؛ وكائنات JSON هي `Map<String, Object>`، والمصفوفات `List<Object>`.
- تُعيد معظم الدوال `{"result": "OK", "data": {...}}` أو `{"result": "ERROR", "error": {...}}`.
  الاستثناءات: تستخدم `getCurrentBalance` غلافًا من نوع `OperationResult`، وتُعيد `checkAvailability`
  قائمة `List` مجردة عند النجاح.

تُعاد الأخطاء كقيم ولا تُرمى كاستثناءات. يحتفظ كل كائن عميل ببيانات تشخيصية لكل استدعاء، لذا استخدم كائنًا
واحدًا لكل خيط تنفيذ (thread).

## الاستخدام

جميع الأمثلة أدناه متاحة أيضًا كأصناف قابلة للتشغيل في مجلد [examples](examples):

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### تسجيل النطاق

ملاحظة: نطاقات .tr تتطلب مُعاملات إضافية. يُستخدم المُعامِل `additionalAttributes`
لأسماء النطاقات التي تحتاج إلى معلومات إضافية، مثل .tr.

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
    1,                                                  // المدة (بالسنوات)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // قائمة فارغة = ns1/ns2.domainnameapi.com
    true,                                               // قفل المسجِّل
    false                                               // حماية الخصوصية
);

// نطاقات .tr: سمات TRABIS كمُعامِل سابع
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = فرد (Owner)، 0 = شركة (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // فرد: رقم الهوية الوطنية
// شركة: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

يمكن أن تكون جهات الاتصال مسطّحة (`AddressLine1`، `Phone`، `PhoneCountryCode`) أو متداخلة
(`Address.Line1`، `Phone.Phone.Number`). تُستكمَل الحقول الناقصة أو غير المنتظمة وتُوحَّد صيغتها
قبل الإرسال (راجع `validateContact`).

<details>
<summary>مثال على المخرجات</summary>

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
تُجيب نقطة نهاية التسجيل بإيصال مختصر؛ استدعِ `getDetails` بعد ذلك للحصول على السجل الكامل.
</details>

### تجديد النطاق

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>مثال على المخرجات</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### نقل النطاق

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// تحقّق أولًا مما إذا كان يمكن نقل النطاق باستخدام رمز التفويض هذا
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// عمليات النقل المعلّقة
dna.cancelTransfer("example.com");   // واردة
dna.approveTransfer("example.com");  // صادرة
dna.rejectTransfer("example.com");   // صادرة
```

<details>
<summary>مثال على المخرجات</summary>

تُعيد `transfer` البنية نفسها التي يُعيدها التسجيل. أما `checkTransfer`:

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
عندما يكون النطاق غير قابل للنقل تكون قيمة `result` هي `"ERROR"` وقيمة
`error` هي `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
وتُعيد `cancelTransfer` / `approveTransfer` / `rejectTransfer` القيمة
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(أو `Approved` / `Rejected`).
</details>

### قائمة النطاقات

```java
Map<String, Object> all = dna.getList();   // أول 200 نطاق

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // يبدأ الترقيم من 0
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>مثال على المخرجات</summary>

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

### قائمة امتدادات النطاقات (TLD)

```java
Map<String, Object> result = dna.getTldList(100);   // القيمة الافتراضية 20
```

<details>
<summary>مثال على المخرجات</summary>

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

### التحقق من توفر النطاق

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // الأسماء بدون الامتداد
    Arrays.asList("com", "net"),           // الامتدادات، مع النقطة أو بدونها
    1,                                     // المدة
    "create"                               // الأمر (الافتراضي "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

بحد أقصى 20 تركيبة من (النطاق × الامتداد) في كل استدعاء (حدّ البوابة).

<details>
<summary>مثال على المخرجات</summary>

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

### تفاصيل النطاق

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // البنية نفسها
```

<details>
<summary>مثال على المخرجات</summary>

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

### تعديل خوادم الأسماء

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

تُزال المسافات الزائدة من الإدخالات وتُحوَّل إلى أحرف صغيرة وتُحذف المكررات قبل الإرسال. ويُقبل أيضًا
كائن `Map` (تُستخدم قيمه).

<details>
<summary>مثال على المخرجات</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### تفعيل / تعطيل قفل النطاق

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>مثال على المخرجات</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### خوادم الأسماء الفرعية (Child Nameservers)

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>مثال على المخرجات</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
تُعيد `deleteChildNameServer` القيمة `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### تعديل خصوصية النطاق

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>مثال على المخرجات</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### جهات اتصال النطاق

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
<summary>مثال على المخرجات</summary>

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
تُعيد `saveContacts` القيمة `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### سجلات منطقة DNS

تخزّن البوابة السجلات على شكل مجموعات (الاسم + النوع). كتابة مجموعة تستبدل جميع قيمها، و
الحذف بأي قيمة يزيل المجموعة بأكملها. الدوال المساعدة على مستوى القيمة أدناه تتولى القراءة والدمج والكتابة
نيابةً عنك.

```java
// عرض القائمة (صف واحد لكل قيمة)
dna.getResourceRecords("example.com");

// إضافة / استبدال / إزالة قيمة واحدة مع الإبقاء على القيم الأخرى
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// جعل المجموعة تحتوي على هذه القيم بالضبط (القائمة الفارغة تزيلها)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// المستوى المنخفض: التجهيز مع apply=false ثم التثبيت مرة واحدة
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

يمكن أن تكون الأسماء `""` / `"@"` (جذر النطاق)، أو تسمية (`"www"`)، أو اسمًا كاملًا (`"www.example.com."`).
الأنواع المدعومة: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. قيمة TTL بين
1 و86400 ثانية. يُحافَظ على فاصل ثانية واحدة على الأقل بين استدعاءات المنطقة لأن البوابة تُجيب
بـ HTTP 429 على الطلبات المتلاحقة (يمكن تغيير الفاصل عبر `setZoneMinInterval(seconds)`).

<details>
<summary>مثال على المخرجات</summary>

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
— وعندما تكون القيمة موجودة مسبقًا: `{..., "Contents": [...], "Changed": false}`.
</details>

### إعادة توجيه URL

```java
dna.setForwarding("example.com", "https://www.example.org");           // إعادة توجيه قياسية
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // مقنّعة
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>مثال على المخرجات</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
بدون إعادة توجيه: `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### الحصول على تفاصيل الموزع والرصيد

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY، EUR، GBP
```

<details>
<summary>مثال على المخرجات</summary>

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

### أدوات مساعدة (بدون اتصال بالشبكة)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

تملأ `validateContact` الحقول الناقصة بقيم بديلة، وتزيل الأرقام والرموز من الأسماء،
وتحوّل أسماء الدول إلى رموز ISO، وتقسم أرقام الهاتف إلى الرقم ورمز الدولة — وهي
عملية التوحيد نفسها التي يطبّقها التسجيل.

<details>
<summary>مثال على المخرجات</summary>

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

## معالجة الأخطاء

لا ترمي الدوال الشبكية استثناءات أبدًا عند حدوث مشكلات في API أو الشبكة؛ بل تُعيد غلاف خطأ:

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

- قيمة `Code` هي `API_<HTTP status>` لأخطاء البوابة (`API_400`، `API_404`، `API_429`، `API_500`، ...).
  الإجابة بـ 301/302 تعني بيانات اعتماد خاطئة (`Invalid API credentials`).
- عندما تسرد البوابة أخطاء تحقق خاصة بكل حقل، يذكر `Details` أسماء الحقول، مثل
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- يتحول جسم الخطأ الفارغ إلى رسالة مثل `HTTP 429 - Too Many Requests` وما شابهها.
- تُعدّ مهلات الانتظار وإخفاقات DNS وTLS أخطاءً أيضًا، ولا تُعاد أبدًا كنجاح فارغ. تكون رسالة انتهاء المهلة
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` مع الرمز `API_28`؛
  أما إخفاقات النقل الأخرى فتكون بالصيغة `Curl error during request: ...` (يُحتفظ بأرقام أخطاء curl كي
  تتطابق الرموز مع php-dna؛ وتعرضها `getParsedResponseData()` بالصيغة `CURL_<n>`).
- تستخدم عمليات التحقق داخل المكتبة رموزها الخاصة: `RESPONSE` (إجابة فارغة)، `DOMAIN_RENEW`،
  `TRANSFER_NOT_AVAILABLE`، `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`، `SET_FORWARD`، `AVAILABILITY`.

البيانات التشخيصية لآخر استدعاء:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // الجسم بعد فك ترميزه، أو {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // الجسم بعد فك ترميزه، أو {Code, Message, Details} الخاصة بالخطأ
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## وضع الاختبار

مرّر `true` (أو `"true"`، `"1"`، `"on"`، `"yes"`) كمُعامِل ثالث للدالة الإنشائية لاستخدام
بيئة الاختبار OTE ‏`https://ote.domainresellerapi.com/api/v1`. وتوجّه `setServiceUrl(url)`
العميل إلى أي عنوان أساسي آخر.

## تقارير الأخطاء (القياس عن بُعد)

عندما يفشل استدعاء API، يمكن للمكتبة إرسال تقرير خطأ إلى نظام تتبع الأخطاء الخاص بـ DomainNameAPI
‏(Sentry). كما يُرسَل جزء صغير عشوائي من الاستدعاءات الناجحة (4%، بالإضافة إلى كل استدعاء تتجاوز مدته ثانية واحدة)
كسجل أداء. تُرسَل التقارير في الخلفية ولا تعيق استدعاءك أو تُفسده أبدًا. تقارير الأخطاء
**مفعّلة افتراضيًا**؛ وتُوسَم الأحداث بالوسم `application=DomainNameAPI-Java`
مع إصدار المكتبة.

> ℹ️ في الإصدار 1.0.0 لا يزال عنوان Sentry المدمج فارغًا (مشروع Java قيد الإعداد)،
> لذا لا يُرسَل أي شيء ما لم تضبط عنوانًا باستخدام `setTelemetryDsn(...)`.

**ما يتم إرساله:**

- رمز الخطأ ورسالته
- مسار نقطة النهاية المستدعاة وطريقة HTTP
- مدة الاستدعاء
- إصدار المكتبة وإصدار Java
- معرّف الموزع (Reseller ID) واسم النطاق
- عنوان IP العام للخادم (يتم الحصول عليه عبر `api.ipify.org`)
- اسم مستخدم نظام التشغيل
- طلب API والاستجابة، بما في ذلك بيانات جهات اتصال النطاق؛ تُخفى بيانات الاعتماد فقط — إذ تُستبدل رموز EPP/التفويض ومفاتيح API وكلمات المرور والرموز المميزة (tokens) بـ `***`

**ما لا يُرسَل أبدًا:** رموز EPP/التفويض ومفاتيح API وكلمات المرور والرموز المميزة (tokens) بصيغة مقروءة.

**لماذا وإلى متى:** تُستخدم البيانات فقط لقياس الاستخدام والتشخيص المبكر للمشكلات، وتُحفَظ لمدة 30 يومًا على خادم Sentry الخاص بـ DomainNameAPI، ثم تُحذف تلقائيًا.

**كيفية إيقافها:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // عدم إرسال أي شيء
dna.setTelemetryMinimal(true);         // أو: الرمز والرسالة ونقطة النهاية والتوقيت والإصدارات ومعرّف الموزع فقط
```

> 🔒 **الأمان:** التحقق من شهادة TLS مفعّل. على خادم يحتوي على مخزن شهادات CA قديم يمكن
> إيقافه باستخدام `dna.setSslVerification(false);` — وهذا غير مستحسن؛ فتحديث
> مخزن شهادات CA في Java هو الحل الأفضل.

## خيارات متقدمة

- **طبقة HTTP مخصصة:** تستبدل `dna.setTransport(HttpTransport)` طبقة النقل الافتراضية
  المبنية على `java.net.http` (للوكلاء (proxies)، أو TLS مخصص، أو التسجيل، أو الاختبارات).
- **مهلات الانتظار:** 30 ثانية لكل استدعاء، و10 ثوانٍ للاتصال.
- **User-Agent:** يُرسِل كل طلب الترويسة `User-Agent: DomainNameAPI-Java/1.0.0`؛ إذ إن Cloudflare الموضوعة أمام
  البوابة ترفض وكلاء المستخدم الافتراضيين للعملاء.

## الاختبارات

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| الاختبار | الوصف |
|------|-------------|
| `ConformanceTest` | يشغّل جميع حالات المطابقة الـ 126 الخاصة بـ php-dna ‏(`src/test/resources/conformance/`): يجب أن تتطابق الطلبات المُرسَلة والقيم المُعادة مع php-dna |
| `HttpLayerTest` | الترويسات، وسلاسل الاستعلام، وأجسام JSON، ووضع الاختبار، وتنظيم الإيقاع، وطبقة النقل الفعلية `java.net.http` |
| `ErrorMappingTest` | ربط أخطاء البوابة، وإخفاقات النقل، وأخطاء التحقق |
| `TelemetryTest` | إخفاء بيانات الاعتماد، والتصنيف، والوضع المختصر، وبنية الحدث |
| `UtilitiesTest` | `isTrTLD`، `validateContact`، التنسيق المتوافق مع PHP، مُرمِّز JSON |

لا يتصل أي اختبار بالشبكة خارج الجهاز المحلي.

<hr style="border: 4px solid #000; border-style: dashed;">

## رموز الإرجاع والأخطاء مع أوصافها

| الرمز | الوصف | التفاصيل |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | تم تنفيذ الأمر بنجاح | العملية ناجحة. |
| 1001 | تم تنفيذ الأمر بنجاح؛ الإجراء معلّق | العملية ناجحة. إلا أنها حاليًا في قائمة الانتظار لإتمامها. |
| 2003 | مُعامِل مطلوب مفقود | خطأ في مُعامِل مفقود. على سبيل المثال، عدم إدخال رقم هاتف في معلومات الاتصال. |
| 2105 | الكائن غير مؤهل للتجديد | حالة النطاق غير مناسبة للتجديد، فهو مقفل أمام عمليات التحديث. يجب ألا تكون الحالة "clientupdateprohibited". وقد يعود ذلك إلى حالات أخرى. |
| 2200 | خطأ في المصادقة | خطأ في التفويض، رمز الأمان غير صحيح أو أن النطاق مسجّل لدى مسجِّل آخر. |
| 2302 | الكائن موجود | اسم النطاق أو معلومات خادم الأسماء موجودة مسبقًا في قاعدة البيانات. لا يمكن التسجيل. |
| 2303 | الكائن غير موجود | اسم النطاق أو معلومات خادم الأسماء غير موجودة في قاعدة البيانات. يلزم تسجيل جديد. |
| 2304 | حالة الكائن تمنع العملية | حالة النطاق غير مناسبة للتحديث، فهو مقفل أمام عمليات التحديث. يجب ألا تكون الحالة "clientupdateprohibited". وقد يعود ذلك إلى حالات أخرى. |

| الرمز | التفاصيل |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | خطأ في النظام، التفاصيل لـ ({0})! |
| 102  | أخطاء متعددة، التفاصيل لـ ({0})! |
| 103  | أخطاء غير معروفة، التفاصيل لـ ({0})! |
| 200  | أمر API غير موجود ({0})! |
| 210  | خدمة API غير موجودة ({0})! |
| 211  | لم يُحدَّد مزوّد خدمة API! |
| 300  | الموزع غير موجود! |
| 301  | عنوان IP الحالي الخاص بك غير مصرّح له بالوصول. يُرجى التأكد من أنك تتصل من عنوان IP مصرّح به ثم المحاولة مرة أخرى |
| 310  | امتداد النطاق (TLD) غير مدعوم! |
| 320  | API غير موجود! |
| 321  | العملة غير مدعومة! |
| 330  | لم تُحدَّد المُعاملات المطلوبة ({0}). |
| –    | تأكد من إرسال جميع كائنات جهات الاتصال كاملة |
| 340  | لم يُعثر على تعريف السعر ({0}[{1}]-{2}{3}). |
| 350  | رصيد الموزع غير كافٍ. (معرّف الموزع : {0} - الرصيد الحالي : {1} {2}). |
| 350  | عملة المحاسبة غير مطابقة أو أن الرصيد غير كافٍ. |
| 360  | طلب API غير صالح للحقل ({0}). |
| 360  | تم تجاوز حصة API! |
| 361  | خطأ تقييد المعدّل (Throttling)! |
| 362  | النطاق المميز (Premium) غير متاح للتسجيل حاليًا. |
| 363  | أُلغيت العملية لأن النطاق في فترة التجديد التلقائي |
| 364  | هذا النطاق غير متاح حاليًا لإجراء المعاملات بسبب مشكلة لدى السجل (Registry) |

| الرمز | التفاصيل |
|------|---------------------------------------------------------------------------------------------|
| 400  | جهة اتصال غير صالحة ({0}). |
| 401  | لا يمكن مزامنة معلومات جهة الاتصال. |
| 402  | لا يوجد وصول إلى معلومات جهة الاتصال من السجل. |
| 403  | توجد معلومات ناقصة في النظام. أدخل معلوماتك الافتراضية أو تواصل مع فريق الدعم. |
| 404  | لا يمكن تحديث جهة اتصال النطاق. يلزم إذن من السجل. |
| 410  | جهة الاتصال غير موجودة. |
| 410  | جهة الاتصال غير موجودة ({0}). |
| 420  | أمر API غير صالح لجهة الاتصال {0}. |
| 430  | API جهة الاتصال غير موجود. |
| 440  | جهة الاتصال غير مُزامَنة. |
| 450  | عدد كبير جدًا من جهات اتصال النطاق! |
| 451  | تعذّر متابعة تحديث جهة الاتصال. |

| الرمز | التفاصيل |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | معرّف نطاق غير صالح. |
| 500  | معرّف نطاق غير صالح ({0}). |
| 501  | تعذّرت مزامنة النطاق ({0}) |
| 502  | فشل النقل الداخلي |
| 503  | تسجيل النطاق غير متاح. |
| 504  | معلومات النطاق غير متطابقة. السابق: {0} |
| 505  | أدخل عنوان IP. |
| 506  | بدأ نقل النطاق ولكن لم تُقرأ معلومات جهات الاتصال. |
| 507  | خطأ في التفويض. |
| 510  | النطاق غير موجود. |
| 511  | لا يمكن العثور على النطاقات منتهية الصلاحية. |
| 512  | النطاق غير قابل للتجديد. |
| 513  | النطاق ليس في حالة قابلة للتحديث. يجب أن يكون نشطًا ليتم تحديثه |
| 514  | يُتوقَّع أن يكون النطاق في فترة الاسترداد (Redemption). |
| 520  | أمر API غير صالح للنطاق "{0}". |
| 530  | مدة النطاق غير صالحة. يجب أن تكون المدة من {0} إلى {1} سنة. المدة المطلوبة {2} سنة. |
| 540  | لا يمكن تمديد النطاق لأكثر من {0} سنة من التاريخ الحالي. |
| 550  | اسم النطاق غير صالح. يجب أن يتراوح طول النطاق بين {0} و{1} حرفًا. طول اسم النطاق المطلوب {2} حرفًا. |
| 560  | عدد خوادم الأسماء غير صالح. يجب أن يحتوي النطاق على {0} إلى {1} من خوادم الأسماء. عدد خوادم الأسماء المطلوب {2}. |
| 561  | لم يُعثر على معلومات خادم الأسماء في الطلب الوارد |
| 570  | IDN غير مدعوم للامتداد "{0}". |
| 571  | المدة غير صالحة. |
| 572  | الأمر غير صالح. |
| 573  | لم يُعثر على أسماء النطاقات. |
| 574  | لم يُعثر على أسماء الامتدادات (TLD). |
| 575  | النطاق ليس في حالة قابلة للتحديث. يجب أن يكون نشطًا لكي يتم تحديث خوادم الأسماء |
| 576  | يمكن تجديد النطاق خلال الشهر الأخير قبل تاريخ انتهاء الصلاحية |
| 580  | النقل غير مدعوم للامتداد "{0}". |
| 581  | خادم الأسماء الفرعي غير موجود |
| 582  | بدأ موزع آخر عملية النقل. |
| 583  | لم تبدأ عملية النقل. يُرجى التواصل مع فريق الدعم. |
| 584  | حالة الكائن تمنع العملية |
| 590  | رمز التفويض مطلوب لعملية النقل هذه. |
| 591  | رمز التفويض غير صالح. |
| 592  | يوجد قفل نقل على النطاق. |
| 593  | لا يمكن نقل النطاق، إذ يجب أن تستوفي معلومات الحالة وقفل النقل المعايير المطلوبة. (معلومات الحالة: #ok) |
| 594  | تعذّر حل عنوان خادم أسماء النطاق ({0}) |
| 595  | لا يمكن قراءة معلومات جهة الاتصال (whois.registrar.tld)، يُرجى التأكد من أن حالة حماية الخصوصية مفتوحة |
| 596  | تعذّر التحقق من معلومات جهة الاتصال |
| 597  | امتداد النطاق (TLD) غير موجود |
| 598  | حدث خطأ غير معروف |
| 599  | إعادة توجيه النطاق غير موجودة |

## الترخيص

Apache License 2.0 — راجع [LICENSE](LICENSE).
