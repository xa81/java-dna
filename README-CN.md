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

# java-dna — DomainNameAPI Java 库

DomainNameAPI 域名代理商 REST API 的 Java 客户端。它是
[php-dna](https://github.com/domainreseller/php-dna) 的忠实移植版本：方法相同、发送到网络上的请求相同、
返回值也相同。Kotlin 及其他 JVM 语言可以直接使用。

## 📦 下载 — 请始终使用 Releases 页面！

⬇️ **在此获取最新的经过测试的版本：https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ **请勿**使用绿色的 **Code → Download ZIP** 按钮 — 它下载的是未经处理的开发分支。发布包带有版本号、经过测试，可直接用于生产环境。

## 安装与集成指南

### 最低要求

- Java 11 或更高版本（推荐 17 或 21）
- 无任何运行时依赖：本库使用 `java.net.http.HttpClient` 和一个小巧的内置 JSON 编解码器。

### A) 发布版 jar

从 [Releases](https://github.com/domainreseller/java-dna/releases/latest) 页面下载 `domainnameapi-1.0.0.jar`，并将其加入您的 classpath。

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

坐标：`io.github.domainreseller:domainnameapi:1.0.0`。在该构件可从公共仓库获取之前，
请基于发布版源码将其安装到本地仓库：

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

## 🔑 API 凭据 — Reseller ID 与 API Key

本库与 DomainNameAPI REST API 通信，只需要两项凭据，并通过构造函数传入：

| 参数 | 值 |
|---|---|
| 第一个 | Reseller ID — UUID 格式，例如 `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| 第二个 | API Key |

> 💡 您可以在 DomainNameAPI 面板的 **API 设置** 中找到 **Reseller ID** 和 **API Key**。
> ⚠️ 这些是 **API 凭据** — 您的面板登录信息在此处**无效**。

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// 正式环境
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// 测试（OTE）环境
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

两项凭据都会自动去除首尾空白。测试模式标志也可以是字符串，因为面板通常以这种方式保存复选框设置：
`"1"`、`"true"`、`"on"` 和 `"yes"` 选择测试环境，其他任何值则选择正式环境。

## 快速入门

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

// 以 JSON 格式打印任意结果
System.out.println(Json.encodePretty(details));
```

Kotlin：

```kotlin
val dna = DomainNameApi(resellerId, apiKey)
val details = dna.getDetails("example.com")
if (details["result"] == "OK") {
    val data = details["data"] as Map<*, *>
    println(data["Status"])
}
```

### 返回值

各方法返回与 php-dna 相同的动态结构，由 `LinkedHashMap` 和
`ArrayList` 构成：

- 键名与 php-dna 完全一致（`Status`、`LockStatus`、`NameServers` 等）。
- `LockStatus` 等标志为**字符串** `"true"` / `"false"`。
- 价格和余额为**格式化后的字符串**（`"11.9900"`、`"601.64"`）。
- 数字为 `Integer`、`Long` 或 `Double`；JSON 对象为 `Map<String, Object>`，数组为 `List<Object>`。
- 大多数方法返回 `{"result": "OK", "data": {...}}` 或 `{"result": "ERROR", "error": {...}}`。
  例外：`getCurrentBalance` 使用 `OperationResult` 封装结构，`checkAvailability`
  在成功时直接返回一个 `List`。

错误以返回值的形式给出，而不是抛出异常。每个客户端实例都会保存每次调用的诊断信息，因此请为每个线程
使用一个独立的实例。

## 使用方法

以下所有示例也以可运行类的形式提供在 [examples](examples) 文件夹中：

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### 域名注册

注意：.tr 域名需要额外参数。`additionalAttributes` 参数用于
需要额外信息的域名，例如 .tr。

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
    1,                                                  // 期限（年）
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // 空列表 = ns1/ns2.domainnameapi.com
    true,                                               // 注册商锁
    false                                               // 隐私保护
);

// .tr 域名：TRABIS 属性作为第 7 个参数
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = 个人（Owner），0 = 公司（Corporate）
trabis.put("TRABISCITIZIENID", "11111111111");   // 个人：身份证号
// 公司：trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

联系人可以是扁平结构（`AddressLine1`、`Phone`、`PhoneCountryCode`），也可以是嵌套结构
（`Address.Line1`、`Phone.Phone.Number`）。缺失或格式混乱的字段会在发送前被补全
并规范化（参见 `validateContact`）。

<details>
<summary>示例输出</summary>

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
注册接口只返回一份简短的回执；如需完整记录，请随后调用 `getDetails`。
</details>

### 域名续费

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>示例输出</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### 域名转移

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// 先检查该域名能否使用此授权码转移
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// 待处理的转移
dna.cancelTransfer("example.com");   // 转入
dna.approveTransfer("example.com");  // 转出
dna.rejectTransfer("example.com");   // 转出
```

<details>
<summary>示例输出</summary>

`transfer` 返回与注册相同的结构。`checkTransfer`：

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
当域名不可转移时，`result` 为 `"ERROR"`，
`error` 为 `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`。
`cancelTransfer` / `approveTransfer` / `rejectTransfer` 返回
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
（或 `Approved` / `Rejected`）。
</details>

### 域名列表

```java
Map<String, Object> all = dna.getList();   // 前 200 个域名

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // 从 0 开始
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>示例输出</summary>

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

### TLD 列表

```java
Map<String, Object> result = dna.getTldList(100);   // 默认 20
```

<details>
<summary>示例输出</summary>

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

### 域名可用性查询

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // 不含 TLD 的名称
    Arrays.asList("com", "net"),           // TLD，可带或不带点
    1,                                     // 期限
    "create"                               // 命令（默认 "create"）
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

每次调用最多 20 个“域名 × TLD”组合（网关限制）。

<details>
<summary>示例输出</summary>

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

### 域名详情

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // 结构相同
```

<details>
<summary>示例输出</summary>

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

### 修改域名服务器

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

发送前会对各条目去除首尾空白、转换为小写并去重。也可以传入 `Map`
（使用其中的值）。

<details>
<summary>示例输出</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### 启用 / 禁用域名锁

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>示例输出</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### 子域名服务器（Child Nameservers）

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>示例输出</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` 返回 `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`。
</details>

### 修改域名隐私保护

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>示例输出</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### 域名联系人

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
<summary>示例输出</summary>

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
`saveContacts` 返回 `{"result": "OK", "data": {"contacts": {...}}}`。
</details>

### DNS 区域记录

网关以记录集（名称 + 类型）的形式存储记录。写入一个记录集会替换其全部值，
而按任意一个值删除都会移除整个记录集。下面这些按值操作的辅助方法会替您完成读取、合并和写入。

```java
// 列出记录（每个值一行）
dna.getResourceRecords("example.com");

// 添加 / 替换 / 删除单个值，同时保留其他值
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// 使记录集恰好包含这些值（空列表会删除该记录集）
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// 底层操作：使用 apply=false 暂存，然后一次性提交
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

名称可以是 `""` / `"@"`（根域名）、一个标签（`"www"`）或完整名称（`"www.example.com."`）。
支持的类型：`A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`。TTL 范围为
1..86400 秒。由于网关会对突发请求返回 HTTP 429，区域相关调用之间至少间隔一秒
（可通过 `setZoneMinInterval(seconds)` 修改间隔）。

<details>
<summary>示例输出</summary>

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
`addResourceRecordValue`：
`{"result": "OK", "data": {"Name": "", "Type": "MX", "TTL": 3600, "Contents": ["10 mx1.example.com.", "30 mx3.example.com"]}}`
— 当该值已存在时：`{..., "Contents": [...], "Changed": false}`。
</details>

### URL 转发

```java
dna.setForwarding("example.com", "https://www.example.org");           // 标准重定向
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // 隐藏式（框架）转发
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>示例输出</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
未设置转发时：`{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`。
</details>

### 获取代理商详情和余额

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY、EUR、GBP
```

<details>
<summary>示例输出</summary>

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
`getCurrentBalance`：
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

### 实用工具（无需网络）

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` 会用占位值填充缺失字段，去除姓名中的数字和符号，
将国家名称转换为 ISO 代码，并将电话号码拆分为号码和国家代码 — 这与
注册时所执行的规范化处理完全相同。

<details>
<summary>示例输出</summary>

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

## 错误处理

网络方法在遇到 API 或网络问题时从不抛出异常；而是返回一个错误封装结构：

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

- 对于网关错误，`Code` 为 `API_<HTTP status>`（`API_400`、`API_404`、`API_429`、`API_500` 等）。
  301/302 响应表示凭据错误（`Invalid API credentials`）。
- 当网关列出逐字段的校验错误时，`Details` 会写明相关字段，例如
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- 空的错误响应体会被转换为 `HTTP 429 - Too Many Requests` 之类的信息。
- 超时、DNS 和 TLS 失败同样属于错误，绝不会被当作空的成功结果。超时信息为
  `Could not reach the DomainNameAPI service: the connection timed out. (...)`，错误码为 `API_28`；
  其他传输失败信息为 `Curl error during request: ...`（保留 curl 错误编号，以便
  错误码与 php-dna 保持一致；`getParsedResponseData()` 将其显示为 `CURL_<n>`）。
- 库内部的检查使用其自有错误码：`RESPONSE`（空响应）、`DOMAIN_RENEW`、
  `TRANSFER_NOT_AVAILABLE`、`ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`、`SET_FORWARD`、`AVAILABILITY`。

最近一次调用的诊断信息：

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // 解码后的响应体，或 {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // 解码后的响应体，或错误的 {Code, Message, Details}
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## 测试模式

将 `true`（或 `"true"`、`"1"`、`"on"`、`"yes"`）作为构造函数的第三个参数传入，即可使用
OTE 测试环境 `https://ote.domainresellerapi.com/api/v1`。`setServiceUrl(url)` 可将
客户端指向任意其他基础 URL。

## 错误报告（遥测）

当 API 调用失败时，本库可以将错误报告发送到 DomainNameAPI 自有的错误跟踪系统
（Sentry）。此外，一小部分随机抽取的成功调用（4%，外加所有耗时超过 1 秒的调用）
会作为性能记录发送。报告在后台发送，绝不会阻塞或中断您的调用。错误报告
**默认开启**；事件会附带标签 `application=DomainNameAPI-Java`
以及库版本号。

> ℹ️ 在 1.0.0 版本中，内置的 Sentry 地址仍为空（Java 项目正在配置中），
> 因此除非您使用 `setTelemetryDsn(...)` 配置地址，否则不会发送任何内容。

**发送的内容：**

- 错误码和错误信息
- 所调用的接口路径和 HTTP 方法
- 调用耗时
- 库版本和 Java 版本
- 代理商 ID 和域名
- 服务器的公网 IP 地址（通过 `api.ipify.org` 查询）
- 操作系统用户名
- API 请求和响应，包括域名的联系人信息；仅凭据会被屏蔽 — EPP/授权码、API 密钥、密码和令牌会被替换为 `***`

**绝不发送的内容：** 可读形式的 EPP/授权码、API 密钥、密码或令牌。

**用途与保留期限：** 这些数据仅用于使用情况统计和问题的早期诊断，保存在 DomainNameAPI 自有的 Sentry 服务器上 30 天，之后自动删除。

**如何关闭：**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // 不发送任何内容
dna.setTelemetryMinimal(true);         // 或者：仅发送错误码、信息、接口、耗时、版本和代理商 ID
```

> 🔒 **安全：** TLS 证书验证默认开启。在 CA 证书库过旧的主机上，可以
> 使用 `dna.setSslVerification(false);` 将其关闭 — 不建议这样做；更新
> Java CA 证书库才是更好的解决办法。

## 高级用法

- **自定义 HTTP 层：** `dna.setTransport(HttpTransport)` 可替换默认的
  `java.net.http` 传输层（用于代理、自定义 TLS、录制、测试等）。
- **超时：** 每次调用 30 秒，连接 10 秒。
- **User-Agent：** 每个请求都会发送 `User-Agent: DomainNameAPI-Java/1.0.0`；网关前端的 Cloudflare
  会拒绝默认的客户端 User-Agent。

## 测试

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| 测试 | 说明 |
|------|-------------|
| `ConformanceTest` | 运行全部 126 个 php-dna 一致性测试用例（`src/test/resources/conformance/`）：发送的请求和返回的值必须与 php-dna 一致 |
| `HttpLayerTest` | 请求头、查询字符串、JSON 请求体、测试模式、请求节流，以及真实的 `java.net.http` 传输层 |
| `ErrorMappingTest` | 网关错误映射、传输失败、校验错误 |
| `TelemetryTest` | 凭据屏蔽、分类、精简模式、事件结构 |
| `UtilitiesTest` | `isTrTLD`、`validateContact`、与 PHP 兼容的格式化、JSON 编解码器 |

所有测试都不会访问本机以外的网络。

<hr style="border: 4px solid #000; border-style: dashed;">

## 返回码与错误码说明

| 代码 | 描述 | 详情 |
|------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1000 | 命令执行成功 | 操作成功。 |
| 1001 | 命令执行成功；操作待处理 | 操作成功。但该操作目前正在队列中等待完成。 |
| 2003 | 缺少必需参数 | 参数缺失错误。例如，联系人信息中未填写电话。 |
| 2105 | 对象不符合续费条件 | 域名状态不适合续费，已被锁定，无法进行更新操作。状态不应为 "clientupdateprohibited"。也可能由其他状态条件导致。 |
| 2200 | 身份验证错误 | 授权错误，安全码不正确或域名已在其他注册商处注册。 |
| 2302 | 对象已存在 | 域名或域名服务器信息已存在于数据库中，无法注册。 |
| 2303 | 对象不存在 | 域名或域名服务器信息不存在于数据库中，需要重新注册。 |
| 2304 | 对象状态禁止此操作 | 域名状态不适合更新，已被锁定，无法进行更新操作。状态不应为 "clientupdateprohibited"。也可能由其他状态条件导致。 |

| 代码 | 详情 |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 101  | 系统错误，详情见 ({0})！ |
| 102  | 多个错误，详情见 ({0})！ |
| 103  | 未知错误，详情见 ({0})！ |
| 200  | 未找到 API 命令 ({0})！ |
| 210  | 未找到 API 服务 ({0})！ |
| 211  | 未设置 API 服务提供商！ |
| 300  | 未找到代理商！ |
| 301  | 您当前的 IP 地址无权访问。请确保从已授权的 IP 地址连接后重试 |
| 310  | 不支持该 TLD！ |
| 320  | 未找到 API！ |
| 321  | 不支持该币种！ |
| 330  | 未设置必需参数 ({0})。 |
| –    | 请确保发送的所有联系人对象信息完整 |
| 340  | 未找到价格定义 ({0}[{1}]-{2}{3})。 |
| 350  | 代理商余额不足。（代理商 ID：{0} - 当前余额：{1} {2}）。 |
| 350  | 结算币种不匹配或余额不足。 |
| 360  | 字段 ({0}) 的 API 请求无效。 |
| 360  | 已超出 API 配额！ |
| 361  | 请求被限流！ |
| 362  | 该溢价域名目前无法注册。 |
| 363  | 由于域名处于自动续费期，操作已取消 |
| 364  | 由于注册局方面的问题，该域名目前无法进行交易 |

| 代码 | 详情 |
|------|---------------------------------------------------------------------------------------------|
| 400  | 联系人无效 ({0})。 |
| 401  | 联系人信息无法同步。 |
| 402  | 无法从注册局获取联系人信息。 |
| 403  | 系统中缺少信息。请填写您的默认信息或联系支持团队。 |
| 404  | 无法更新域名联系人。需要注册局的许可。 |
| 410  | 未找到联系人。 |
| 410  | 未找到联系人 ({0})。 |
| 420  | 联系人 {0} 的 API 命令无效。 |
| 430  | 未找到联系人 API。 |
| 440  | 联系人未同步。 |
| 450  | 域名联系人过多！ |
| 451  | 无法继续进行联系人更新。 |

| 代码 | 详情 |
|------|--------------------------------------------------------------------------------------------------------------------------------------|
| 500  | 域名 ID 无效。 |
| 500  | 域名 ID 无效 ({0})。 |
| 501  | 域名无法同步 ({0}) |
| 502  | 内部转移失败 |
| 503  | 无法注册该域名。 |
| 504  | 域名信息不匹配。之前：{0} |
| 505  | 请输入 IP 地址。 |
| 506  | 域名转移已开始，但未能读取联系人信息。 |
| 507  | 授权错误。 |
| 510  | 未找到域名。 |
| 511  | 无法找到已过期的域名。 |
| 512  | 域名不可续费。 |
| 513  | 域名不处于可更新状态。必须为激活状态才能更新 |
| 514  | 应处于赎回期（Redemption Period）。 |
| 520  | 域名 "{0}" 的 API 命令无效。 |
| 530  | 域名期限无效。期限必须为 {0} 至 {1} 年。所请求的期限为 {2} 年。 |
| 540  | 域名不能从当前日期起延长超过 {0} 年。 |
| 550  | 域名无效。域名长度必须为 {0} 至 {1} 个字符。所请求的域名为 {2} 个字符。 |
| 560  | 域名服务器数量无效。域名必须有 {0} 至 {1} 个域名服务器。所请求的域名服务器数量为 {2}。 |
| 561  | 传入的请求中未找到域名服务器信息 |
| 570  | TLD "{0}" 不支持 IDN。 |
| 571  | 期限无效。 |
| 572  | 命令无效。 |
| 573  | 未找到域名。 |
| 574  | 未找到 TLD 名称。 |
| 575  | 域名不处于可更新状态。必须为激活状态才能更新域名服务器 |
| 576  | 域名只能在到期日前的最后 1 个月内续费 |
| 580  | TLD "{0}" 不支持转移。 |
| 581  | 未找到子域名服务器 |
| 582  | 转移已由其他代理商发起。 |
| 583  | 转移未初始化。请联系支持团队。 |
| 584  | 对象状态禁止此操作 |
| 590  | 此次转移需要授权码。 |
| 591  | 授权码无效。 |
| 592  | 域名存在转移锁。 |
| 593  | 域名无法转移，状态信息和转移锁必须符合规定条件。（状态信息：#ok） |
| 594  | 无法解析域名服务器地址 ({0}) |
| 595  | 无法读取联系人信息（whois.registrar.tld），请确保隐私保护状态已开启 |
| 596  | 无法验证联系人信息 |
| 597  | 未找到 TLD |
| 598  | 发生未知错误 |
| 599  | 未找到域名转发 |

## 许可证

Apache License 2.0 — 参见 [LICENSE](LICENSE)。
