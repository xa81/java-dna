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

# java-dna — Bibliothèque Java DomainNameAPI

Client Java pour l'API REST de revente de noms de domaine DomainNameAPI. Il s'agit d'un portage fidèle de
[php-dna](https://github.com/domainreseller/php-dna) : les mêmes méthodes, les mêmes requêtes envoyées
sur le réseau et les mêmes valeurs de retour. Kotlin et les autres langages JVM peuvent l'utiliser directement.

## 📦 Téléchargement — utilisez toujours les Releases !

⬇️ **Obtenez la dernière version testée ici : https://github.com/domainreseller/java-dna/releases/latest**

> ⚠️ N'utilisez **pas** le bouton vert **Code → Download ZIP** — il télécharge la branche de développement brute. Les paquets de release sont versionnés, testés et prêts pour la production.

## Guide d'Installation et d'Intégration

### Configuration Minimale Requise

- Java 11 ou supérieur (17 ou 21 recommandé)
- Aucune dépendance à l'exécution : la bibliothèque utilise `java.net.http.HttpClient` et un petit codec JSON intégré.

### A) Jar de release

Téléchargez `domainnameapi-1.0.0.jar` depuis la page [Releases](https://github.com/domainreseller/java-dna/releases/latest) et ajoutez-le à votre classpath.

```bash
javac -cp domainnameapi-1.0.0.jar MyApp.java
java  -cp domainnameapi-1.0.0.jar:. MyApp
```

### B) Maven / Gradle

Coordonnées : `io.github.domainreseller:domainnameapi:1.0.0`. Tant que l'artefact n'est pas disponible dans un
dépôt public, installez-le dans votre dépôt local à partir des sources de la release :

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

## 🔑 Identifiants API — Reseller ID et API Key

La bibliothèque communique avec l'API REST de DomainNameAPI et nécessite exactement deux identifiants, transmis au constructeur :

| Paramètre | Valeur |
|---|---|
| Premier | Reseller ID — UUID au format `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| Second | API Key |

> 💡 Vous trouverez votre **Reseller ID** et votre **API Key** dans votre panneau DomainNameAPI, sous **API Settings**.
> ⚠️ Il s'agit d'**identifiants API** — vos identifiants de connexion au panneau ne fonctionneront **pas** ici.

```java
import io.github.domainreseller.dna.DomainNameApi;
import io.github.domainreseller.dna.Json;

String resellerId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String apiKey     = "your-api-key";

// Environnement de production
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);

// Environnement de test (OTE)
DomainNameApi ote = new DomainNameApi(resellerId, apiKey, true);
```

Les espaces superflus des deux identifiants sont supprimés. L'indicateur de mode test peut aussi être une chaîne, car les panneaux
enregistrent les cases à cocher ainsi : `"1"`, `"true"`, `"on"` et `"yes"` sélectionnent l'environnement de test, toute autre valeur l'environnement de production.

## Démarrage rapide

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

// Afficher n'importe quel résultat au format JSON
System.out.println(Json.encodePretty(details));
```

Kotlin :

```kotlin
val dna = DomainNameApi(resellerId, apiKey)
val details = dna.getDetails("example.com")
if (details["result"] == "OK") {
    val data = details["data"] as Map<*, *>
    println(data["Status"])
}
```

### Valeurs de retour

Les méthodes renvoient la même structure dynamique que php-dna, construite à partir de `LinkedHashMap` et
`ArrayList` :

- Les noms de clés sont identiques à ceux de php-dna (`Status`, `LockStatus`, `NameServers`, ...).
- Les indicateurs tels que `LockStatus` sont les **chaînes** `"true"` / `"false"`.
- Les prix et les soldes sont des **chaînes formatées** (`"11.9900"`, `"601.64"`).
- Les nombres sont des `Integer`, `Long` ou `Double` ; les objets JSON sont des `Map<String, Object>`, les tableaux des `List<Object>`.
- La plupart des méthodes renvoient `{"result": "OK", "data": {...}}` ou `{"result": "ERROR", "error": {...}}`.
  Exceptions : `getCurrentBalance` utilise une enveloppe `OperationResult` et `checkAvailability`
  renvoie une simple `List` en cas de succès.

Les erreurs sont renvoyées, pas levées sous forme d'exceptions. Une instance du client conserve les diagnostics de chaque appel ; utilisez donc une
instance par thread.

## Utilisation

Tous les exemples ci-dessous sont également disponibles sous forme de classes exécutables dans le dossier [examples](examples) :

```bash
mvn -Pexamples -DskipTests compile
DNA_RESELLER_ID=... DNA_API_KEY=... java -cp target/classes GetDetails
```

### Enregistrement de Domaine

Remarque : des paramètres supplémentaires sont requis pour les domaines .tr. Le paramètre `additionalAttributes` est
utilisé pour les noms de domaine qui nécessitent des informations complémentaires, comme les .tr.

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
    1,                                                  // durée (années)
    contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), // liste vide = ns1/ns2.domainnameapi.com
    true,                                               // verrou du registrar
    false                                               // protection de la vie privée
);

// Domaines .tr : attributs TRABIS comme 7e paramètre
Map<String, Object> trabis = new LinkedHashMap<>();
trabis.put("TRABISDOMAINCATEGORY", 1);           // 1 = particulier (Owner), 0 = entreprise (Corporate)
trabis.put("TRABISCITIZIENID", "11111111111");   // particulier : numéro d'identité national
// entreprise : trabis.put("TRABISTAXOFFICE", "Istanbul VD"); trabis.put("TRABISTAXNUMBER", "1234567890");
dna.registerWithContactInfo("example.com.tr", 1, contacts,
    Arrays.asList("ns1.example.com", "ns2.example.com"), true, false, trabis);
```

Les contacts peuvent être plats (`AddressLine1`, `Phone`, `PhoneCountryCode`) ou imbriqués
(`Address.Line1`, `Phone.Phone.Number`). Les champs manquants ou mal formés sont complétés et normalisés
avant l'envoi (voir `validateContact`).

<details>
<summary>Exemple de Sortie</summary>

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
Le point d'accès d'enregistrement répond par un bref accusé de réception ; appelez ensuite `getDetails` pour obtenir l'enregistrement complet.
</details>

### Renouvellement de Domaine

```java
Map<String, Object> result = dna.renew("example.com", 1);
```

<details>
<summary>Exemple de Sortie</summary>

```json
{"result": "OK", "data": {"ExpirationDate": "2027-11-09T12:00:00"}}
```
</details>

### Transfert de Domaine

```java
Map<String, Object> result = dna.transfer("example.com", "Xjk9Vtr2", 1);

// Vérifier d'abord si le domaine peut être transféré avec ce code d'autorisation
Map<String, Object> check = dna.checkTransfer("example.com", "Xjk9Vtr2");

// Transferts en attente
dna.cancelTransfer("example.com");   // entrant
dna.approveTransfer("example.com");  // sortant
dna.rejectTransfer("example.com");   // sortant
```

<details>
<summary>Exemple de Sortie</summary>

`transfer` renvoie la même structure que l'enregistrement. `checkTransfer` :

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
Lorsque le domaine n'est pas transférable, `result` vaut `"ERROR"` et
`error` vaut `{"Code": "TRANSFER_NOT_AVAILABLE", "Message": "Domain is locked", "Details": "TransferLocked"}`.
`cancelTransfer` / `approveTransfer` / `rejectTransfer` renvoient
`{"result": "OK", "data": {"DomainName": "example.com", "Status": "Cancelled"}}`
(`Approved` / `Rejected`).
</details>

### Liste des Domaines

```java
Map<String, Object> all = dna.getList();   // 200 premiers domaines

Map<String, Object> filters = new LinkedHashMap<>();
filters.put("PageNumber", 0);               // commence à 0
filters.put("PageSize", 50);
filters.put("OrderColumn", "ExpirationDate");
filters.put("OrderDirection", "DESC");
filters.put("DomainName", "example");
Map<String, Object> page = dna.getList(filters);
```

<details>
<summary>Exemple de Sortie</summary>

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

### Liste des TLD

```java
Map<String, Object> result = dna.getTldList(100);   // 20 par défaut
```

<details>
<summary>Exemple de Sortie</summary>

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

### Vérification de Disponibilité de Domaine

```java
Object result = dna.checkAvailability(
    Arrays.asList("example", "example2"),  // libellés sans TLD
    Arrays.asList("com", "net"),           // TLD, avec ou sans le point
    1,                                     // durée
    "create"                               // commande ("create" par défaut)
);
if (result instanceof List) {
    for (Object row : (List<?>) result) { /* ... */ }
}
```

Au maximum 20 combinaisons domaine × TLD par appel (limite de la passerelle).

<details>
<summary>Exemple de Sortie</summary>

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

### Détails du Domaine

```java
Map<String, Object> result = dna.getDetails("example.com");
Map<String, Object> fresh  = dna.syncFromRegistry("example.com"); // même structure
```

<details>
<summary>Exemple de Sortie</summary>

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

### Modification des Serveurs de Noms

```java
Map<String, Object> result = dna.modifyNameServer("example.com",
    Arrays.asList("ns1.example.com", "ns2.example.com"));
```

Avant l'envoi, les entrées sont débarrassées des espaces superflus, converties en minuscules et dédoublonnées. Une `Map` est également acceptée
(ses valeurs sont utilisées).

<details>
<summary>Exemple de Sortie</summary>

```json
{"result": "OK", "data": {"NameServers": ["ns1.example.com", "ns2.example.com"]}}
```
</details>

### Activer / Désactiver le Verrouillage du Domaine

```java
dna.enableTheftProtectionLock("example.com");
dna.disableTheftProtectionLock("example.com");
```

<details>
<summary>Exemple de Sortie</summary>

```json
{"result": "OK", "data": {"LockStatus": true}}
```
</details>

### Serveurs de Noms Enfants

```java
dna.addChildNameServer("example.com", "ns1.example.com", "192.0.2.10");
dna.modifyChildNameServer("example.com", "ns1.example.com", "192.0.2.20");
dna.deleteChildNameServer("example.com", "ns1.example.com");
```

<details>
<summary>Exemple de Sortie</summary>

```json
{"result": "OK", "data": {"NameServer": "ns1.example.com", "IPAdresses": ["192.0.2.10"]}}
```
`deleteChildNameServer` renvoie `{"result": "OK", "data": {"NameServer": "ns1.example.com"}}`.
</details>

### Modifier la Protection de la Vie Privée du Domaine

```java
Map<String, Object> result = dna.modifyPrivacyProtectionStatus("example.com", true, "Owner request");
```

<details>
<summary>Exemple de Sortie</summary>

```json
{"data": {"PrivacyProtectionStatus": true}, "result": "OK"}
```
</details>

### Contacts du Domaine

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
<summary>Exemple de Sortie</summary>

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
`saveContacts` renvoie `{"result": "OK", "data": {"contacts": {...}}}`.
</details>

### Enregistrements de Zone DNS

La passerelle stocke les enregistrements sous forme d'ensembles (nom + type). L'écriture d'un ensemble remplace toutes ses valeurs, et
la suppression par n'importe laquelle de ses valeurs supprime l'ensemble entier. Les fonctions utilitaires ci-dessous, qui travaillent au niveau des valeurs, lisent, fusionnent et écrivent
à votre place.

```java
// Lister (une ligne par valeur)
dna.getResourceRecords("example.com");

// Ajouter / remplacer / supprimer UNE valeur, en conservant les autres
dna.addResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");
dna.replaceResourceRecordValue("example.com", "www", "A", "192.0.2.10", "192.0.2.20");
dna.removeResourceRecordValue("example.com", "@", "MX", "30 mx3.example.com");

// Faire en sorte qu'un ensemble contienne exactement ces valeurs (une liste vide le supprime)
dna.setResourceRecordSet("example.com", "@", "TXT", Arrays.asList("v=spf1 -all"), 3600);
dna.deleteResourceRecordSet("example.com", "old", "CNAME");

// Bas niveau : préparer avec apply=false, puis valider en une seule fois
dna.addResourceRecord("example.com", "api", "A", "192.0.2.7", 300, false);
dna.editResourceRecord("example.com", "www", "www", "A", "192.0.2.6", 600, false);
dna.deleteResourceRecord("example.com", "test.example.com.", "192.0.2.9", "A", false);
dna.applyResourceRecords("example.com");
```

Les noms peuvent être `""` / `"@"` (apex), un libellé (`"www"`) ou un nom complet (`"www.example.com."`).
Types pris en charge : `A, AAAA, ALIAS, CAA, CNAME, DS, MX, NAPTR, PTR, SRV, TLSA, TXT`. Le TTL est compris entre
1 et 86400 secondes. Les appels de zone sont espacés d'au moins une seconde, car la passerelle répond
HTTP 429 aux rafales de requêtes (`setZoneMinInterval(seconds)` modifie cet intervalle).

<details>
<summary>Exemple de Sortie</summary>

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
`addResourceRecordValue` :
`{"result": "OK", "data": {"Name": "", "Type": "MX", "TTL": 3600, "Contents": ["10 mx1.example.com.", "30 mx3.example.com"]}}`
— lorsque la valeur existe déjà : `{..., "Contents": [...], "Changed": false}`.
</details>

### Redirection d'URL

```java
dna.setForwarding("example.com", "https://www.example.org");           // Redirection standard
dna.setForwarding("example.com", "https://www.example.org", "Frame");  // masquée
dna.getForwarding("example.com");
dna.deleteForwarding("example.com");
```

<details>
<summary>Exemple de Sortie</summary>

```json
{"result": "OK", "data": {"Enabled": true, "RedirectAddress": "https://www.example.org", "ForwardType": "Frame"}}
```
Sans redirection : `{"result": "OK", "data": {"Enabled": false, "RedirectAddress": "", "ForwardType": ""}}`.
</details>

### Obtenir les Détails et le Solde du Revendeur

```java
Map<String, Object> reseller = dna.getResellerDetails();
Map<String, Object> usd = dna.getCurrentBalance();        // USD
Map<String, Object> tl  = dna.getCurrentBalance("TRY");   // TRY, EUR, GBP
```

<details>
<summary>Exemple de Sortie</summary>

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
`getCurrentBalance` :
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

### Utilitaires (sans réseau)

```java
dna.isTrTLD("example.com.tr");   // true

Map<String, Object> raw = new LinkedHashMap<>();
raw.put("FirstName", " Jo ");
raw.put("Phone", "+90 (532) 374 33 88");
raw.put("Country", "Turkey");
Map<String, Object> clean = dna.validateContact(raw);
```

`validateContact` remplit les champs manquants avec des valeurs par défaut, supprime les chiffres et les symboles des noms,
convertit les noms de pays en codes ISO et sépare les numéros de téléphone en numéro et indicatif pays — la
même normalisation que celle appliquée lors de l'enregistrement.

<details>
<summary>Exemple de Sortie</summary>

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

## Gestion des erreurs

Les méthodes réseau ne lèvent jamais d'exception en cas de problème d'API ou de réseau ; elles renvoient une enveloppe d'erreur :

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

- `Code` vaut `API_<statut HTTP>` pour les erreurs de la passerelle (`API_400`, `API_404`, `API_429`, `API_500`, ...).
  Une réponse 301/302 signifie que les identifiants sont incorrects (`Invalid API credentials`).
- Lorsque la passerelle renvoie des erreurs de validation par champ, `Details` nomme les champs concernés, par exemple
  `Contacts Phone - The field Phone must be a string with a maximum length of 16.`
- Un corps d'erreur vide devient `HTTP 429 - Too Many Requests` ou un message similaire.
- Les dépassements de délai et les échecs DNS et TLS sont eux aussi des erreurs, jamais un succès vide. Un dépassement de délai produit
  `Could not reach the DomainNameAPI service: the connection timed out. (...)` avec le code `API_28` ;
  les autres échecs de transport produisent `Curl error during request: ...` (les numéros d'erreur curl sont conservés afin que
  les codes correspondent à php-dna ; `getParsedResponseData()` les affiche sous la forme `CURL_<n>`).
- Les vérifications effectuées par la bibliothèque utilisent leurs propres codes : `RESPONSE` (réponse vide), `DOMAIN_RENEW`,
  `TRANSFER_NOT_AVAILABLE`, `ADD_ZONE` / `EDIT_ZONE` / `SET_ZONE` / `DELETE_ZONE`, `SET_FORWARD`, `AVAILABILITY`.

Diagnostics du dernier appel :

```java
dna.getRequestData();        // {url, method, payload}
dna.getResponseData();       // corps décodé, ou {raw_response: body}
dna.getResponseHeaders();    // {url, http_code, content_type, total_time, headers}
dna.getParsedResponseData(); // corps décodé, ou {Code, Message, Details} de l'erreur
dna.getLastFunction();       // "request:GET domains/info"
dna.getServiceUrl();
```

## Mode test

Passez `true` (ou `"true"`, `"1"`, `"on"`, `"yes"`) comme troisième argument du constructeur pour utiliser
l'environnement de test OTE `https://ote.domainresellerapi.com/api/v1`. `setServiceUrl(url)` permet de diriger
le client vers n'importe quelle autre URL de base.

## Signalement des erreurs (télémétrie)

Lorsqu'un appel API échoue, la bibliothèque peut envoyer un rapport d'erreur au propre outil de suivi des erreurs de DomainNameAPI
(Sentry). Une petite part aléatoire des appels réussis (4 %, plus chaque appel dépassant 1 seconde) est
envoyée sous forme d'enregistrement de performance. Les rapports sont envoyés en arrière-plan et ne bloquent ni n'interrompent jamais votre
appel. Le signalement des erreurs est **activé par défaut** ; les événements sont étiquetés `application=DomainNameAPI-Java`
avec la version de la bibliothèque.

> ℹ️ Dans la version 1.0.0, l'adresse Sentry intégrée est encore vide (le projet Java est en cours de
> mise en place) ; rien n'est donc envoyé tant que vous ne configurez pas une adresse avec `setTelemetryDsn(...)`.

**Ce qui est envoyé :**

- le code et le message d'erreur
- le chemin du point d'accès appelé et la méthode HTTP
- la durée de l'appel
- la version de la bibliothèque et la version de Java
- le Reseller ID et le nom de domaine
- l'adresse IP publique du serveur (obtenue via `api.ipify.org`)
- le nom d'utilisateur du système d'exploitation
- la requête et la réponse de l'API, y compris les coordonnées de contact du domaine ; seuls les identifiants sont masqués — les codes EPP/d'autorisation, les clés API, les mots de passe et les jetons sont remplacés par `***`

**Ce qui n'est jamais envoyé :** les codes EPP/d'autorisation, les clés API, les mots de passe ou les jetons sous forme lisible.

**Pourquoi et pour combien de temps :** les données servent uniquement à mesurer l'utilisation et à diagnostiquer les problèmes au plus tôt ; elles sont conservées 30 jours sur le propre serveur Sentry de DomainNameAPI, puis supprimées automatiquement.

**Comment la désactiver :**

```java
DomainNameApi dna = new DomainNameApi(resellerId, apiKey);
dna.setErrorReportingEnabled(false);   // ne rien envoyer
dna.setTelemetryMinimal(true);         // ou : uniquement code, message, point d'accès, durée, versions, Reseller ID
```

> 🔒 **Sécurité :** la vérification des certificats TLS est activée. Sur un hôte dont le magasin de certificats CA est obsolète, elle peut
> être désactivée avec `dna.setSslVerification(false);` — ce n'est pas recommandé ; mettre à jour le
> magasin de certificats CA de Java est la meilleure solution.

## Fonctionnalités avancées

- **Couche HTTP personnalisée :** `dna.setTransport(HttpTransport)` remplace le transport
  `java.net.http` par défaut (proxys, TLS personnalisé, enregistrement des échanges, tests).
- **Délais d'expiration :** 30 s par appel, 10 s pour la connexion.
- **User-Agent :** chaque requête envoie `User-Agent: DomainNameAPI-Java/1.0.0` ; Cloudflare, placé devant
  la passerelle, rejette les agents clients par défaut.

## Tests

```bash
mvn verify
# or without a local JDK/Maven:
docker run --rm -v "$PWD":/src -v dna-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-17 mvn -q verify
```

| Test | Description |
|------|-------------|
| `ConformanceTest` | Exécute les 126 fixtures de conformité de php-dna (`src/test/resources/conformance/`) : les requêtes envoyées et les valeurs renvoyées doivent correspondre à celles de php-dna |
| `HttpLayerTest` | En-têtes, chaînes de requête, corps JSON, mode test, espacement des appels, transport réel `java.net.http` |
| `ErrorMappingTest` | Correspondance des erreurs de la passerelle, échecs de transport, erreurs de validation |
| `TelemetryTest` | Masquage des identifiants, classification, mode minimal, structure des événements |
| `UtilitiesTest` | `isTrTLD`, `validateContact`, formatage compatible PHP, codec JSON |

Aucun test n'accède au réseau en dehors de la machine locale.

<hr style="border: 4px solid #000; border-style: dashed;">

## Codes de Retour et d'Erreur avec Descriptions

| Code | Description | Détail |
|------|-------------|--------|
| 1000 | Commande exécutée avec succès | Opération réussie. |
| 1001 | Commande exécutée avec succès ; action en attente | Opération réussie. Cependant, l'opération est actuellement en file d'attente pour être finalisée. |
| 2003 | Paramètre requis manquant | Erreur de paramètre manquant. Par exemple : aucun numéro de téléphone dans les informations de contact. |
| 2105 | L'objet n'est pas éligible au renouvellement | Le statut du domaine ne permet pas le renouvellement ; il est verrouillé pour les opérations de mise à jour. Le statut ne doit pas être "clientupdateprohibited". Peut être dû à d'autres conditions de statut. |
| 2200 | Erreur d'authentification | Erreur d'autorisation : le code de sécurité est incorrect ou le domaine est enregistré auprès d'un autre registrar. |
| 2302 | L'objet existe | Le nom de domaine ou les informations du serveur de noms existent déjà dans la base de données. Enregistrement impossible. |
| 2303 | L'objet n'existe pas | Le nom de domaine ou les informations du serveur de noms n'existent pas dans la base de données. Un nouvel enregistrement est nécessaire. |
| 2304 | Le statut de l'objet interdit l'opération | Le statut du domaine ne permet pas les mises à jour ; il est verrouillé pour les opérations de mise à jour. Le statut ne doit pas être "clientupdateprohibited". Peut être dû à d'autres conditions de statut. |

| CODE | DÉTAIL |
|------|--------|
| 101  | Erreur système détaillée pour ({0}) ! |
| 102  | Erreurs multiples détaillées pour ({0}) ! |
| 103  | Erreurs inconnues détaillées pour ({0}) ! |
| 200  | Commande API introuvable ({0}) ! |
| 210  | Service API introuvable ({0}) ! |
| 211  | Fournisseur du service API non défini ! |
| 300  | Revendeur introuvable ! |
| 301  | Votre adresse IP actuelle n'est pas autorisée à accéder au service. Veuillez vous assurer de vous connecter depuis une adresse IP autorisée et réessayer |
| 310  | TLD non pris en charge ! |
| 320  | API introuvable ! |
| 321  | Devise non prise en charge ! |
| 330  | Paramètre(s) requis non défini(s) ({0}). |
| –    | Assurez-vous d'envoyer tous les objets de contact de manière complète |
| 340  | Définition de prix introuvable ({0}[{1}]-{2}{3}). |
| 350  | Solde du revendeur insuffisant. (Id Revendeur : {0} - Solde Actuel : {1} {2}). |
| 350  | La devise comptable ne correspond pas ou le solde est insuffisant. |
| 360  | Requête API invalide pour le champ ({0}). |
| 360  | Quota API dépassé ! |
| 361  | Erreur de limitation du débit ! |
| 362  | Le domaine premium n'est pas disponible à l'enregistrement pour le moment. |
| 363  | Opération annulée car le domaine est en période de renouvellement automatique |
| 364  | Ce domaine est actuellement indisponible pour toute transaction en raison d'un problème auprès du registre |

| CODE | DÉTAIL |
|------|--------|
| 400  | Contact invalide ({0}). |
| 401  | Les informations de contact ne peuvent pas être synchronisées. |
| 402  | Aucun accès aux informations de contact depuis le registre. |
| 403  | LE SYSTÈME COMPORTE DES INFORMATIONS MANQUANTES. SAISISSEZ VOS INFORMATIONS PAR DÉFAUT OU CONTACTEZ L'ÉQUIPE DE SUPPORT. |
| 404  | LE CONTACT DU DOMAINE NE PEUT PAS ÊTRE MIS À JOUR. AUTORISATION REQUISE DU REGISTRE. |
| 410  | Contact introuvable. |
| 410  | Contact introuvable ({0}). |
| 420  | Commande API invalide pour le contact {0}. |
| 430  | API de contact introuvable. |
| 440  | Le contact n'est pas synchronisé. |
| 450  | Trop de contacts pour le domaine ! |
| 451  | Impossible de procéder à la mise à jour du contact. |

| CODE | DÉTAIL |
|------|--------|
| 500  | Identifiant de domaine invalide. |
| 500  | Identifiant de domaine invalide ({0}). |
| 501  | Le domaine n'a pas pu être synchronisé ({0}) |
| 502  | Échec du transfert interne |
| 503  | L'enregistrement du domaine n'est pas disponible. |
| 504  | Les informations du domaine ne correspondent pas. Avant : {0} |
| 505  | Saisissez l'adresse IP. |
| 506  | Le transfert du domaine a démarré, mais les informations de contact n'ont pas pu être lues. |
| 507  | Erreur d'autorisation. |
| 510  | Domaine introuvable. |
| 511  | Les domaines expirés sont introuvables. |
| 512  | Le domaine n'est pas renouvelable. |
| 513  | Le domaine n'est pas dans un statut permettant la mise à jour. Il doit être actif pour pouvoir être mis à jour |
| 514  | Période de rédemption attendue. |
| 520  | Commande API invalide pour le domaine "{0}". |
| 530  | Durée de domaine invalide. La durée doit être comprise entre {0} et {1} ans. La durée demandée est de {2} ans. |
| 540  | Le domaine ne peut pas être prolongé au-delà de {0} ans à compter de la date actuelle. |
| 550  | Nom de domaine invalide. Le domaine doit comporter entre {0} et {1} caractères. Le nom de domaine demandé comporte {2} caractères. |
| 560  | Nombre de serveurs de noms invalide. Le domaine doit avoir entre {0} et {1} serveurs de noms. Le nombre de serveurs de noms demandé est de {2}. |
| 561  | Aucune information de serveur de noms trouvée dans la requête reçue |
| 570  | IDN non pris en charge pour le TLD "{0}". |
| 571  | Durée invalide. |
| 572  | Commande invalide. |
| 573  | Noms de domaine introuvables. |
| 574  | Noms de TLD introuvables. |
| 575  | Le domaine n'est pas dans un statut permettant la mise à jour. Il doit être actif pour que les serveurs de noms puissent être mis à jour |
| 576  | Le domaine ne peut être renouvelé qu'au cours du dernier mois précédant la date d'expiration |
| 580  | Transfert non pris en charge pour le TLD "{0}". |
| 581  | Serveur de noms enfant introuvable |
| 582  | Transfert initié par un autre revendeur. |
| 583  | Transfert non initialisé. Veuillez contacter l'équipe de support. |
| 584  | Le statut de l'objet interdit l'opération |
| 590  | Un code d'autorisation est requis pour ce transfert. |
| 591  | Le code d'autorisation n'est pas valide. |
| 592  | Un verrou de transfert est présent sur le domaine. |
| 593  | Le domaine ne peut pas être transféré ; les informations de statut et le verrou de transfert doivent respecter les critères requis. (Informations de statut : #ok) |
| 594  | L'adresse du serveur de noms du domaine n'a pas pu être résolue ({0}) |
| 595  | Les informations de contact ne peuvent pas être lues (whois.registrar.tld). Veuillez vous assurer que le statut de protection de la vie privée est ouvert |
| 596  | Les informations de contact n'ont pas pu être vérifiées |
| 597  | TLD introuvable |
| 598  | Une erreur inconnue s'est produite |
| 599  | Redirection de domaine introuvable |

## Licence

Apache License 2.0 — voir [LICENSE](LICENSE).
