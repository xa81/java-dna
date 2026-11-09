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

# java-dna — Libreria Java DomainNameAPI

Client Java per l'API REST di rivendita domini di DomainNameAPI. È un porting fedele di
[php-dna](https://github.com/domainreseller/php-dna): gli stessi metodi, le stesse richieste inviate
in rete e gli stessi valori restituiti. Kotlin e gli altri linguaggi JVM possono utilizzarla direttamente.

## 📦 Download — usa sempre le Releases!

⬇️ **Scarica qui l'ultima versione testata: https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ **Non** usare il pulsante verde **Code → Download ZIP** — scarica il ramo di sviluppo grezzo. I pacchetti di release sono versionati, testati e pronti per la produzione.

## Guida all'Installazione e Integrazione

### Requisiti Minimi

- Java 11 o superiore (consigliati 17 o 21)
- Nessuna dipendenza a runtime: la libreria usa `java.net.http.HttpClient` e un piccolo codec JSON integrato.

### A) Jar di release

Scarica `domainnameapi-1.0.0.jar` dalla pagina [Releases](https://github.com/domainreseller/java-dna/releases/latest) e aggiungilo al tuo classpath.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Coordinate: `io.github.domainreseller:domainnameapi:1.0.0`. Finché l'artefatto non sarà disponibile in un
repository pubblico, installalo nel tuo repository locale a partire dal sorgente della release:

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

## 🔑 Credenziali API — Reseller ID e API Key

La libreria comunica con l'API REST di DomainNameAPI e richiede esattamente due credenziali, passate al costruttore:

| Parametro | Valore |
|---|---|
| Primo | Reseller ID — UUID nel formato `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Secondo | API Key |

> 💡 Trovi il tuo **Reseller ID** e la tua **API Key** nel pannello DomainNameAPI, alla voce **API Settings**.
> ⚠️ Queste sono **credenziali API** — i dati di accesso al pannello qui **non** funzionano.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Ambiente di produzione
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Ambiente di test (OTE)
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Da entrambe le credenziali vengono rimossi gli spazi iniziali e finali. Il flag della modalità test può essere anche una stringa, dato che i pannelli
salvano così le caselle di controllo: `"1"`, `"true"`, `"on"` e `"yes"` selezionano l'ambiente di test, qualsiasi altro valore quello di produzione.

## Avvio rapido

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

// Stampa qualsiasi risultato in formato JSON
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

### Valori restituiti

I metodi restituiscono la stessa struttura dinamica restituita da php-dna, costruita con `LinkedHashMap` e
`ArrayList`:

- I nomi delle chiavi sono identici a quelli di php-dna (`Status`, `LockStatus`, `NameServers`, ...).
- I flag come `LockStatus` sono le **stringhe** `"true"` / `"false"`.
- Prezzi e saldi sono **stringhe formattate** (`"11.9900"`, `"601.64"`).
- I numeri sono `Integer`, `Long` o `Double`; gli oggetti JSON sono `Map<String, Object>`, gli array `List<Object>`.
- La maggior parte dei metodi restituisce `{"result": "OK", "data": {...}}` oppure `{"result": "ERROR", "error": {...}}`.
  Eccezioni: `getCurrentBalance` usa un involucro `OperationResult` e `checkAvailability`
  restituisce una semplice `List` in caso di successo.

Gli errori vengono restituiti, non sollevati come eccezioni. Un'istanza del client conserva la diagnostica di ogni chiamata, quindi usa un'istanza
per ciascun thread.

## Utilizzo

Tutti gli esempi seguenti sono disponibili anche come classi eseguibili nella cartella [examples](examples):

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Registrazione del Dominio

Nota: per i domini .tr sono richiesti parametri aggiuntivi. Il parametro `additionalAttributes` viene
utilizzato per i nomi di dominio che richiedono informazioni extra, come i .tr.

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
    1,                                                  // durata (anni)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // lista vuota = ns1/ns2.domainnameapi.com
    true,                                               // blocco del registrar
    false                                               // protezione della privacy
);

// Domini .tr: attributi TRABIS come 7° parametro
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = persona fisica (Owner), 0 = azienda (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // persona fisica: codice identificativo nazionale
// azienda: trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

I contatti possono essere piatti (`AddressLine1`, `Phone`, `PhoneCountryCode`) o annidati
(`Address.Line1`, `Phone.Phone.Number`). I campi mancanti o non corretti vengono completati e normalizzati
prima dell'invio (vedi `validateContact`).

<details>
<summary>Output di Esempio</summary>

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
L'endpoint di registrazione risponde con una breve ricevuta; chiama poi `getDetails` per ottenere il record completo.
Quando il gateway rifiuta l'ordine (`"success": false`), il risultato è un errore con codice
`REGISTER_DOMAIN` e il messaggio del gateway.
</details>

### Rinnovo del Dominio

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Output di Esempio</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
Un rinnovo rifiutato restituisce un errore con codice `RENEW_DOMAIN`.
</details>

### Trasferimento del Dominio

```java
// I contatti non vengono mai inviati con un trasferimento: il registrar li acquisisce insieme al dominio
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Verifica prima se il dominio può essere trasferito con questo codice di autorizzazione
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Trasferimenti in sospeso
dna.cancelTransfer("example.com");   // in entrata
dna.approveTransfer("example.com");  // in uscita
dna.rejectTransfer("example.com");   // in uscita
```

<details>
<summary>Output di Esempio</summary>

`transfer` restituisce la stessa struttura della registrazione (un errore con codice `TRANSFER_DOMAIN` quando il
gateway rifiuta il trasferimento). `checkTransfer`:

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
Quando il dominio non è trasferibile, `result` è `"ERROR"` ed
`error` è `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` restituiscono
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Elenco dei Domini

```java
Map<String, Object> all = dna.getList();   // primi 200 domini

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // a partire da 0
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Output di Esempio</summary>

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

### Elenco dei TLD

```java
Map<String, Object> result = dna.getTldList(100);   // predefinito 20
```

<details>
<summary>Output di Esempio</summary>

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

### Verifica della Disponibilità del Dominio

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // etichette senza TLD
    Arrays.asList("com", "net"),           // TLD, con o senza il punto
    1,                                     // durata
    "create"                               // comando (predefinito "create")
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Al massimo 20 combinazioni dominio × TLD per chiamata (limite del gateway).

<details>
<summary>Output di Esempio</summary>

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

### Dettagli del Dominio

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // stessa struttura
```

<details>
<summary>Output di Esempio</summary>

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

### Modifica dei Name Server

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Prima dell'invio, le voci vengono ripulite dagli spazi, convertite in minuscolo e private dei duplicati. È accettata anche una `Map`
(ne vengono usati i valori).

<details>
<summary>Output di Esempio</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Attivare / Disattivare il Blocco del Dominio

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Output di Esempio</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Child Name Server

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Output di Esempio</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` restituisce `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Modificare la Protezione Privacy del Dominio

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true);

// Lo stato può essere anche testo, come lo memorizzano i pannelli: "1", "true", "on", "yes" lo attivano
dna.modifyPrivacyProtectionStatus("example.com", "off");
```

Il terzo argomento facoltativo (`reason`) è accettato per compatibilità con php-dna, ma non viene inviato: il
gateway non prevede questo campo.

<details>
<summary>Output di Esempio</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Contatti del Dominio

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
<summary>Output di Esempio</summary>

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
I tipi di contatto vengono confrontati senza distinzione tra maiuscole e minuscole (`Admin`/`Administrative`, `Tech`/`Technical`,
`Registrant`/`Owner`, `Billing`); qualsiasi altro tipo restituito dal registro viene mantenuto dopo questi quattro.
`saveContacts` restituisce `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### Record della Zona DNS

Il gateway memorizza i record come insiemi (nome + tipo). La scrittura di un insieme ne sostituisce tutti i valori e
l'eliminazione tramite uno qualsiasi dei valori rimuove l'intero insieme. Le funzioni di supporto a livello di valore riportate di seguito leggono, uniscono e scrivono
al posto tuo.

```java
// Elenco (una riga per valore)
dna.getResourceRecords("example.com");

// Aggiungi / sostituisci / rimuovi UN valore, mantenendo gli altri
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Fai in modo che un insieme contenga esattamente questi valori (una lista vuota lo rimuove)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Basso livello: prepara con apply=false, poi conferma una sola volta
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

I nomi possono essere `""` / `"@"` (apex), un'etichetta (`"www"`) o un nome completo (`"www.example.com."`).
Tipi supportati: `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. Il TTL va da
1 a 86400 secondi. Le chiamate alla zona vengono distanziate di almeno un secondo, perché il gateway risponde
HTTP 429 alle raffiche di richieste (`setZoneMinInterval(seconds)` modifica l'intervallo). I risultati riportano il
tipo in maiuscolo e il TTL ricondotto ai limiti, così come sono stati inviati. Se l'applicazione automatica non riesce, la scrittura
restituisce l'errore di applicazione: la modifica è predisposta ma non ancora attiva.

<details>
<summary>Output di Esempio</summary>

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
— quando il valore è già presente: `{..., "Contents": [...], "Changed": false}`.
</details>

### Inoltro URL

```java
dna.setForwarding("example.com", "https://www.example.org");           // Reindirizzamento standard
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // mascherato
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Output di Esempio</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Senza inoltro (il messaggio `Redirect information not found` del gateway):
`{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`. Qualsiasi altro
errore, ad esempio un dominio sconosciuto, viene restituito come errore.
`deleteForwarding` restituisce `{"result": "OK", "data": {"DomainName": "example.com", "Removed": true}}`,
oppure `"Removed": false` se non era impostato nulla.
</details>

### Ottenere Dettagli e Saldo del Rivenditore

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Output di Esempio</summary>

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
L'account riporta solo i saldi in USD e TRY. Per le altre valute (EUR, GBP, ...) il risultato è
`{"ErrorCode": "BALANCE", "OperationResult": "FAILED", "OperationMessage": "No EUR balance is reported for this account. Available: USD, TRY.", "Balance": "0.00", ...}`.
</details>

### Utilità (senza rete)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` compila i campi mancanti con valori segnaposto, rimuove cifre e simboli dai nomi,
converte i nomi dei paesi in codici ISO e suddivide i numeri di telefono in numero e prefisso internazionale — la
stessa normalizzazione applicata durante la registrazione.

<details>
<summary>Output di Esempio</summary>

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

## Gestione degli errori

I metodi di rete non sollevano mai eccezioni per problemi dell'API o della rete; restituiscono un involucro di errore:

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

- `Code` è `API_<stato HTTP>` per gli errori del gateway (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  Quando il gateway ha inviato un proprio codice di errore, questo viene mantenuto in `GatewayCode`
  (ad es. `"GatewayCode": "Dna.DomainService:Domain:10007"`), così "dominio non trovato" si distingue
  dagli altri 404. Una risposta 301/302 indica credenziali errate (`Invalid API credentials`).
- Quando il gateway elenca errori di validazione per singolo campo, `Details` indica i campi interessati, ad esempio
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Un corpo di errore vuoto diventa `HTTP 429 - Too Many Requests` o un messaggio simile.
- Anche timeout, errori DNS e TLS sono errori, mai un successo vuoto. Il loro codice è
  `CURL_<n>` con i numeri di errore di curl, come in php-dna: un timeout è `CURL_28` e restituisce
  `Could not reach the DomainNameAPI service: the connection timed out. (...)`; gli altri errori di trasporto
  (`CURL_6` DNS, `CURL_7` connessione rifiutata, `CURL_35`/`CURL_60` TLS, `CURL_56` connessione reimpostata)
  restituiscono `Curl error during request: ...`.
- Un rifiuto del gateway (`"success": false`) restituisce `REGISTER_DOMAIN`, `TRANSFER_DOMAIN` o
  `RENEW_DOMAIN` con il messaggio del gateway.
- I controlli eseguiti dalla libreria usano codici propri: `RESPONSE` (risposta vuota), `DOMAIN_RENEW`, `BALANCE`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Diagnostica dell'ultima chiamata:

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // corpo decodificato, oppure {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // corpo decodificato, oppure {Code, Message, Details} dell'errore
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Modalità test

Passa `true` (oppure `"true"`, `"1"`, `"on"`, `"yes"`) come terzo argomento del costruttore per usare
l'ambiente di test OTE `https://ote.domainresellerapi.com/api/v1`. `setServiceUrl(url)` indirizza il
client verso qualsiasi altro URL di base.

## Segnalazione degli errori (telemetria)

Quando una chiamata API non va a buon fine, la libreria può inviare una segnalazione di errore al sistema di tracciamento degli errori di DomainNameAPI
(Sentry). Una piccola quota casuale delle chiamate riuscite (il 4%, più ogni chiamata che supera 1 secondo) viene
inviata come record di prestazioni. Le segnalazioni vengono inviate in background e non bloccano né interrompono mai la tua
chiamata. La segnalazione degli errori è **attiva per impostazione predefinita**; gli eventi sono contrassegnati con `application=DomainNameAPI-Java`
e con la versione della libreria.

> ℹ️ Nella versione 1.0.0 l'indirizzo Sentry integrato è ancora vuoto (il progetto Java è in fase di
> configurazione), quindi non viene inviato nulla a meno che tu non configuri un indirizzo con `setTelemetryDsn(...)`.

**Cosa viene inviato:**

- codice e messaggio di errore
- percorso dell'endpoint chiamato e metodo HTTP
- durata della chiamata
- versione della libreria e versione di Java
- Reseller ID e nome di dominio
- indirizzo IP pubblico del server (ricavato tramite `api.ipify.org`)
- nome utente del sistema operativo
- richiesta e risposta dell'API, inclusi i dati di contatto del dominio; vengono mascherate solo le credenziali — codici EPP/di autorizzazione, chiavi API, password e token vengono sostituiti con `***`

**Cosa non viene mai inviato:** codici EPP/di autorizzazione, chiavi API, password o token in forma leggibile.

**Perché e per quanto tempo:** i dati vengono utilizzati esclusivamente per misurare l'utilizzo e diagnosticare tempestivamente i problemi, e vengono conservati per 30 giorni sul server Sentry di DomainNameAPI, dopodiché vengono eliminati automaticamente.

**Come disattivarla:**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // non inviare nulla
dna.setTelemetryMinimal(true);         // oppure: solo codice, messaggio, endpoint, tempi, versioni, Reseller ID
```

> 🔒 **Sicurezza:** la verifica dei certificati TLS è attiva. Su un host con un archivio CA obsoleto può
> essere disattivata con `dna.setSslVerification(false);` — non è consigliato; aggiornare
> l'archivio CA di Java è la soluzione migliore.

## Funzionalità avanzate

- **Livello HTTP personalizzato:** `dna.setTransport(HttpTransport)` sostituisce il trasporto
  `java.net.http` predefinito (proxy, TLS personalizzato, registrazione del traffico, test).
- **Timeout:** 30 s per chiamata, 10 s per la connessione.
- **User-Agent:** ogni richiesta invia `User-Agent: DomainNameAPI-Java/1.0.0`; Cloudflare, posto davanti
  al gateway, rifiuta gli user agent predefiniti dei client.

## Test

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Descrizione |
|------|-------------|
| `ConformanceTest` | Esegue tutte le 134 fixture di conformità di php-dna (`src/test/resources/conformance/`): le richieste inviate e i valori restituiti devono corrispondere a quelli di php-dna |
| `HttpLayerTest` | Header, query string, corpi JSON, modalità test, cadenzamento delle chiamate, il trasporto reale `java.net.http` |
| `ErrorMappingTest` | Mappatura degli errori del gateway, errori di trasporto, errori di validazione |
| `TelemetryTest` | Mascheramento delle credenziali, classificazione, modalità minima, struttura degli eventi |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, formattazione compatibile con PHP, codec JSON |

Nessun test accede alla rete al di fuori della macchina locale.

<hr style="border: 4px solid #000; border-style: dashed;">

## Codici di Ritorno ed Errore con Descrizioni

| Codice | Descrizione | Dettaglio |
|------|-------------|-----------|
| 1000 | Comando completato con successo | Operazione riuscita. |
| 1001 | Comando completato con successo; azione in sospeso | Operazione riuscita. Tuttavia, l'operazione è attualmente in coda per il completamento. |
| 2003 | Parametro obbligatorio mancante | Errore di parametro mancante. Ad esempio: nessun numero di telefono nelle informazioni di contatto. |
| 2105 | L'oggetto non è idoneo al rinnovo | Lo stato del dominio non consente il rinnovo; è bloccato per le operazioni di aggiornamento. Lo stato non deve essere "clientupdateprohibited". Può dipendere da altre condizioni di stato. |
| 2200 | Errore di autenticazione | Errore di autorizzazione: il codice di sicurezza non è corretto oppure il dominio è registrato presso un altro registrar. |
| 2302 | L'oggetto esiste | Il nome di dominio o le informazioni del name server esistono già nel database. Impossibile registrarlo. |
| 2303 | L'oggetto non esiste | Il nome di dominio o le informazioni del name server non esistono nel database. È necessaria una nuova registrazione. |
| 2304 | Lo stato dell'oggetto impedisce l'operazione | Lo stato del dominio non consente aggiornamenti; è bloccato per le operazioni di aggiornamento. Lo stato non deve essere "clientupdateprohibited". Può dipendere da altre condizioni di stato. |

| CODICE | DETTAGLIO |
|------|-----------|
| 101  | Errore di sistema dettagliato per ({0})! |
| 102  | Errori multipli dettagliati per ({0})! |
| 103  | Errori sconosciuti dettagliati per ({0})! |
| 200  | Comando API non trovato ({0})! |
| 210  | Servizio API non trovato ({0})! |
| 211  | Provider del servizio API non impostato! |
| 300  | Rivenditore non trovato! |
| 301  | Il tuo indirizzo IP attuale non è autorizzato ad accedere. Assicurati di connetterti da un indirizzo IP autorizzato e riprova |
| 310  | TLD non supportato! |
| 320  | API non trovata! |
| 321  | Valuta non supportata! |
| 330  | Parametro/i obbligatorio/i non impostato/i ({0}). |
| –    | Assicurati di inviare tutti gli oggetti di contatto completi |
| 340  | Definizione del prezzo non trovata ({0}[{1}]-{2}{3}). |
| 350  | Saldo del rivenditore insufficiente. (Id Rivenditore : {0} - Saldo Attuale : {1} {2}). |
| 350  | La valuta contabile non corrisponde oppure il saldo è insufficiente. |
| 360  | Richiesta API non valida per il campo ({0}). |
| 360  | Quota API superata! |
| 361  | Errore di limitazione delle richieste! |
| 362  | Il dominio premium non è al momento disponibile per la registrazione. |
| 363  | Operazione annullata perché il dominio è nel periodo di rinnovo automatico |
| 364  | Questo dominio non è al momento disponibile per operazioni a causa di un problema presso il registro |

| CODICE | DETTAGLIO |
|------|-----------|
| 400  | Contatto non valido ({0}). |
| 401  | Impossibile sincronizzare le informazioni di contatto. |
| 402  | Nessun accesso alle informazioni di contatto dal registro. |
| 403  | NEL SISTEMA MANCANO DELLE INFORMAZIONI. INSERISCI LE TUE INFORMAZIONI PREDEFINITE O CONTATTA IL TEAM DI SUPPORTO. |
| 404  | IMPOSSIBILE AGGIORNARE IL CONTATTO DEL DOMINIO. È NECESSARIA L'AUTORIZZAZIONE DEL REGISTRO. |
| 410  | Contatto non trovato. |
| 410  | Contatto non trovato ({0}). |
| 420  | Comando API non valido per il contatto {0}. |
| 430  | API dei contatti non trovata. |
| 440  | Il contatto non è sincronizzato. |
| 450  | Troppi contatti per il dominio! |
| 451  | Impossibile procedere con l'aggiornamento del contatto. |

| CODICE | DETTAGLIO |
|------|-----------|
| 500  | ID dominio non valido. |
| 500  | ID dominio non valido ({0}). |
| 501  | Impossibile sincronizzare il dominio ({0}) |
| 502  | Trasferimento interno non riuscito |
| 503  | La registrazione del dominio non è disponibile. |
| 504  | Le informazioni del dominio non corrispondono. Prima: {0} |
| 505  | Inserisci l'indirizzo IP. |
| 506  | Il trasferimento del dominio è stato avviato, ma le informazioni di contatto non sono state lette. |
| 507  | Errore di autorizzazione. |
| 510  | Dominio non trovato. |
| 511  | I domini scaduti non possono essere trovati. |
| 512  | Il dominio non è rinnovabile. |
| 513  | Il dominio non è in uno stato aggiornabile. Deve essere attivo per poter essere aggiornato |
| 514  | È previsto il periodo di riscatto (Redemption Period). |
| 520  | Comando API non valido per il dominio "{0}". |
| 530  | Durata del dominio non valida. La durata deve essere compresa tra {0} e {1} anni. La durata richiesta è di {2} anni. |
| 540  | Il dominio non può essere esteso oltre {0} anni dalla data attuale. |
| 550  | Nome di dominio non valido. Il dominio deve avere una lunghezza compresa tra {0} e {1} caratteri. Il nome di dominio richiesto ha {2} caratteri. |
| 560  | Numero di name server non valido. Il dominio deve avere da {0} a {1} name server. Il numero di name server richiesto è {2}. |
| 561  | Nessuna informazione sui name server trovata nella richiesta ricevuta |
| 570  | IDN non supportato per il TLD "{0}". |
| 571  | Durata non valida. |
| 572  | Comando non valido. |
| 573  | Nomi di dominio non trovati. |
| 574  | Nomi dei TLD non trovati. |
| 575  | Il dominio non è in uno stato aggiornabile. Deve essere attivo perché i name server possano essere aggiornati |
| 576  | Il dominio può essere rinnovato solo nell'ultimo mese prima della data di scadenza |
| 580  | Trasferimento non supportato per il TLD "{0}". |
| 581  | Child name server non trovato |
| 582  | Trasferimento avviato da un altro rivenditore. |
| 583  | Trasferimento non inizializzato. Contatta il team di supporto. |
| 584  | Lo stato dell'oggetto impedisce l'operazione |
| 590  | Per questo trasferimento è richiesto il codice di autorizzazione. |
| 591  | Il codice di autorizzazione non è valido. |
| 592  | Sul dominio è presente un blocco di trasferimento. |
| 593  | Il dominio non può essere trasferito: le informazioni di stato e il blocco di trasferimento devono rispettare i criteri richiesti. (Informazioni di stato: #ok) |
| 594  | Impossibile risolvere l'indirizzo del name server del dominio ({0}) |
| 595  | Impossibile leggere le informazioni di contatto (whois.registrar.tld). Assicurati che lo stato della protezione della privacy sia aperto |
| 596  | Impossibile verificare le informazioni di contatto |
| 597  | TLD non trovato |
| 598  | Si è verificato un errore sconosciuto |
| 599  | Inoltro del dominio non trovato |

## Licenza

Apache License 2.0 — vedi [LICENSE](LICENSE).
