package io.github.domainreseller.dna;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DomainNameAPI client: a faithful Java port of php-dna's
 * {@code DomainNameAPI_PHPLibrary}.
 *
 * <p>Authenticate with your <b>Reseller ID</b> (UUID) and <b>API Key</b> from the
 * reseller panel (API Settings). Panel login credentials do not work.</p>
 *
 * <pre>{@code
 * DomainNameApi dna = new DomainNameApi("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", "your-api-key");
 * Map<String, Object> details = dna.getDetails("example.com");
 * if ("OK".equals(details.get("result"))) { ... }
 * }</pre>
 *
 * <p>Every method returns the same dynamic structure php-dna returns, built from
 * {@link java.util.LinkedHashMap} and {@link java.util.ArrayList}: the same keys,
 * string flags ({@code "true"}/{@code "false"}), formatted money strings
 * ({@code "11.9900"}) and OK/ERROR envelopes. Numbers are {@link Integer},
 * {@link Long} or {@link Double}. Errors are returned, never thrown: a failed call
 * yields {@code {"result":"ERROR","error":{"Code":..,"Message":..,"Details":..}}}.</p>
 *
 * <p>An instance keeps per-call diagnostics ({@link #getRequestData()} and
 * friends), so use one instance per thread.</p>
 */
public class DomainNameApi {

    /** Library version. */
    public static final String VERSION = "1.0.0";

    /** User-Agent sent with every request (Cloudflare rejects default client agents). */
    public static final String USER_AGENT = "DomainNameAPI-Java/" + VERSION;

    /** Application name reported in telemetry. */
    public static final String APPLICATION = Telemetry.APPLICATION;

    /** Live gateway base URL. */
    public static final String URL_PROD = DNARest.URL_PROD;

    /** Test (OTE) gateway base URL. */
    public static final String URL_OTE = DNARest.URL_OTE;

    /** Record types the DNS zone API accepts. */
    public static final List<String> ZONE_RECORD_TYPES = DNARest.ZONE_RECORD_TYPES;

    /** Forwarding types: Standard (redirect) and Frame (masked). */
    public static final List<String> FORWARD_TYPES = DNARest.FORWARD_TYPES;

    /** Nameservers used by {@code registerWithContactInfo} when none are given. */
    public static final List<String> DEFAULT_NAMESERVERS = DNARest.DEFAULT_NAMESERVERS;

    private final DNARest client;

    /**
     * Live environment client.
     *
     * @param resellerId Reseller ID (UUID)
     * @param apiKey     API Key
     */
    public DomainNameApi(String resellerId, String apiKey) {
        this(resellerId, apiKey, false);
    }

    /**
     * @param resellerId Reseller ID (UUID); surrounding whitespace is trimmed
     * @param apiKey     API Key; surrounding whitespace is trimmed
     * @param testMode   {@code true} for the OTE test environment
     */
    public DomainNameApi(String resellerId, String apiKey, boolean testMode) {
        this.client = new DNARest(trim(resellerId), trim(apiKey), testMode);
    }

    /**
     * Test mode given as a string, as panels store checkbox settings:
     * {@code "1"}, {@code "true"}, {@code "on"} and {@code "yes"} (any case) mean
     * test mode, anything else means live (php-dna BUG-10284).
     *
     * @param resellerId Reseller ID (UUID)
     * @param apiKey     API Key
     * @param testMode   test mode flag as text
     */
    public DomainNameApi(String resellerId, String apiKey, String testMode) {
        this(resellerId, apiKey, Php.filterBool(testMode));
    }

    private static String trim(String s) {
        return s == null ? "" : Php.trim(s);
    }

    // ================================================================ account

    /**
     * Reseller account details with USD and TL balances.
     *
     * @return {@code {result, id, active, name, balance, currency, symbol, balances[]}}
     */
    public Map<String, Object> getResellerDetails() {
        return client.getResellerDetails();
    }

    /** Balance in USD. @return OperationResult envelope (see {@link #getCurrentBalance(String)}) */
    public Map<String, Object> getCurrentBalance() {
        return client.getCurrentBalance("USD");
    }

    /**
     * Balance in one currency (USD, TRY, EUR, GBP).
     *
     * @param currency currency code, any case
     * @return {@code {ErrorCode, OperationMessage, OperationResult, Balance, CurrencyId, CurrencyInfo, CurrencyName, CurrencySymbol}}
     */
    public Map<String, Object> getCurrentBalance(String currency) {
        return client.getCurrentBalance(currency);
    }

    // ================================================================ search and lists

    /** Availability check with command {@code "create"}. @see #checkAvailability(List, List, int, String) */
    public Object checkAvailability(List<String> domains, List<String> extensions, int period) {
        return client.checkAvailability(domains, extensions, period, "create");
    }

    /**
     * Check availability of every domain × extension combination (at most 20 per call).
     *
     * @param domains    labels without TLD, e.g. {@code ["example"]}
     * @param extensions TLDs with or without the dot, e.g. {@code ["com", ".net"]}
     * @param period     registration period in years
     * @param command    "create", "renew", "transfer", ... (echoed in the result)
     * @return on success a {@code List} of {@code {TLD, DomainName, Status, Command, Period, IsFee, Price, Currency, Reason}};
     * on failure the {@code Map} error envelope
     */
    public Object checkAvailability(List<String> domains, List<String> extensions, int period, String command) {
        return client.checkAvailability(domains, extensions, period, command);
    }

    /** First 200 domains of the account. @see #getList(Map) */
    public Map<String, Object> getList() {
        return client.getList(Collections.emptyMap());
    }

    /**
     * Domains in the account.
     *
     * @param extraParameters filters and paging: {@code PageNumber} (0-based), {@code PageSize},
     *                        {@code OrderColumn}, {@code OrderDirection}, {@code DomainName}, or the raw
     *                        gateway keys {@code MaxResultCount}, {@code SkipCount}, {@code Sorting}
     * @return {@code {data: {Domains: [...]}, result, TotalCount}}
     */
    public Map<String, Object> getList(Map<String, ?> extraParameters) {
        return client.getList(extraParameters);
    }

    /** First 20 TLDs with pricing. @see #getTldList(int) */
    public Map<String, Object> getTldList() {
        return client.getTldList(20);
    }

    /**
     * TLD list with pricing.
     *
     * @param count how many TLDs to return
     * @return {@code {data: [{id, status, maxchar, maxperiod, minchar, minperiod, tld, pricing, currencies}], result}}
     */
    public Map<String, Object> getTldList(int count) {
        return client.getTldList(count);
    }

    // ================================================================ domain

    /**
     * Domain details.
     *
     * @param domainName e.g. "example.com"
     * @return {@code {data: {ID, Status, DomainName, AuthCode, LockStatus, ...}, result}}
     */
    public Map<String, Object> getDetails(String domainName) {
        return client.getDetails(domainName);
    }

    /**
     * Refresh a domain from the registry (same result as {@link #getDetails(String)}).
     *
     * @param domainName domain
     * @return domain details envelope
     */
    public Map<String, Object> syncFromRegistry(String domainName) {
        return client.syncFromRegistry(domainName);
    }

    /**
     * The four contacts of a domain.
     *
     * @param domainName domain
     * @return {@code {data: {contacts: {Administrative, Billing, Registrant, Technical}}, result}}
     */
    public Map<String, Object> getContacts(String domainName) {
        return client.getContacts(domainName);
    }

    /**
     * Replace the contacts of a domain.
     *
     * @param domainName domain
     * @param contacts   {@code {"Administrative": {...}, "Billing": {...}, "Technical": {...}, "Registrant": {...}}};
     *                   each contact flat ({@code AddressLine1}, {@code Phone}, ...) or nested
     *                   ({@code Address.Line1}, {@code Phone.Phone.Number}, ...)
     * @return {@code {result, data: {contacts}}}
     */
    public Map<String, Object> saveContacts(String domainName, Map<String, ?> contacts) {
        return client.saveContacts(domainName, contacts);
    }

    /**
     * Set the nameservers of a domain. Entries are trimmed, lower-cased and de-duplicated.
     *
     * @param domainName  domain
     * @param nameServers nameserver host names
     * @return {@code {result, data: {NameServers}}}
     */
    public Map<String, Object> modifyNameServer(String domainName, List<String> nameServers) {
        return client.modifyNameServer(domainName, nameServers);
    }

    /**
     * Set the nameservers of a domain from a map (the values are used, as in php-dna).
     *
     * @param domainName  domain
     * @param nameServers e.g. {@code {"ns1": "ns1.example.net", "ns2": "ns2.example.net"}}
     * @return {@code {result, data: {NameServers}}}
     */
    public Map<String, Object> modifyNameServer(String domainName, Map<String, ?> nameServers) {
        return client.modifyNameServer(domainName, nameServers);
    }

    /**
     * Turn the registrar (theft protection) lock on.
     *
     * @param domainName domain
     * @return {@code {result, data: {LockStatus: true}}}
     */
    public Map<String, Object> enableTheftProtectionLock(String domainName) {
        return client.enableTheftProtectionLock(domainName);
    }

    /**
     * Turn the registrar (theft protection) lock off.
     *
     * @param domainName domain
     * @return {@code {result, data: {LockStatus: false}}}
     */
    public Map<String, Object> disableTheftProtectionLock(String domainName) {
        return client.disableTheftProtectionLock(domainName);
    }

    /** Privacy protection on/off with reason "Owner request". */
    public Map<String, Object> modifyPrivacyProtectionStatus(String domainName, boolean status) {
        return client.modifyPrivacyProtectionStatus(domainName, status, DNARest.DEFAULT_REASON);
    }

    /**
     * Turn WHOIS privacy protection on or off.
     *
     * @param domainName domain
     * @param status     true to enable
     * @param reason     kept for php-dna compatibility; not sent (the gateway has no such field)
     * @return {@code {data: {PrivacyProtectionStatus}, result}}
     */
    public Map<String, Object> modifyPrivacyProtectionStatus(String domainName, boolean status, String reason) {
        return client.modifyPrivacyProtectionStatus(domainName, status, reason);
    }

    /**
     * Privacy protection with the status given as text, as panels store it:
     * {@code "1"}, {@code "true"}, {@code "on"}, {@code "yes"} enable, anything else disables.
     *
     * @param domainName domain
     * @param status     status as text
     * @return {@code {data: {PrivacyProtectionStatus}, result}}
     */
    public Map<String, Object> modifyPrivacyProtectionStatus(String domainName, String status) {
        return client.modifyPrivacyProtectionStatus(domainName, status, DNARest.DEFAULT_REASON);
    }

    /** Text status variant with a reason (not sent). */
    public Map<String, Object> modifyPrivacyProtectionStatus(String domainName, String status, String reason) {
        return client.modifyPrivacyProtectionStatus(domainName, status, reason);
    }

    /**
     * Renew a domain.
     *
     * @param domainName domain
     * @param period     years
     * @return {@code {result, data: {ExpirationDate}}}
     */
    public Map<String, Object> renew(String domainName, int period) {
        return client.renew(domainName, period);
    }

    /** Register with default nameservers, lock on, privacy off, no extra attributes. */
    public Map<String, Object> registerWithContactInfo(String domainName, int period, Map<String, ?> contacts) {
        return registerWithContactInfo(domainName, period, contacts, Collections.emptyList(), true, false, Collections.emptyMap());
    }

    /** Register with lock on, privacy off, no extra attributes. */
    public Map<String, Object> registerWithContactInfo(String domainName, int period, Map<String, ?> contacts,
                                                       List<String> nameServers) {
        return registerWithContactInfo(domainName, period, contacts, nameServers, true, false, Collections.emptyMap());
    }

    /** Register with privacy off, no extra attributes. */
    public Map<String, Object> registerWithContactInfo(String domainName, int period, Map<String, ?> contacts,
                                                       List<String> nameServers, boolean eppLock) {
        return registerWithContactInfo(domainName, period, contacts, nameServers, eppLock, false, Collections.emptyMap());
    }

    /** Register without extra attributes. */
    public Map<String, Object> registerWithContactInfo(String domainName, int period, Map<String, ?> contacts,
                                                       List<String> nameServers, boolean eppLock, boolean privacyLock) {
        return registerWithContactInfo(domainName, period, contacts, nameServers, eppLock, privacyLock, Collections.emptyMap());
    }

    /**
     * Register a domain.
     *
     * @param domainName           domain
     * @param period               years
     * @param contacts             Administrative, Billing, Technical and Registrant contacts
     * @param nameServers          nameservers; empty means {@link #DEFAULT_NAMESERVERS}
     * @param eppLock              registrar lock
     * @param privacyLock          WHOIS privacy
     * @param additionalAttributes TLD attributes, e.g. TRABIS keys for .tr
     *                             ({@code TRABISDOMAINCATEGORY}, {@code TRABISCITIZIENID}, {@code TRABISTAXOFFICE}, ...)
     * @return domain details envelope
     */
    public Map<String, Object> registerWithContactInfo(String domainName, int period, Map<String, ?> contacts,
                                                       List<String> nameServers, boolean eppLock, boolean privacyLock,
                                                       Map<String, ?> additionalAttributes) {
        return client.registerWithContactInfo(domainName, period, contacts, nameServers, eppLock, privacyLock,
            additionalAttributes);
    }

    // ================================================================ child nameservers

    /**
     * Create a child nameserver (glue record).
     *
     * @param domainName domain
     * @param nameServer host, e.g. "ns1.example.com"
     * @param ipAddress  IPv4 or IPv6 address
     * @return {@code {result, data: {NameServer, IPAdresses}}}
     */
    public Map<String, Object> addChildNameServer(String domainName, String nameServer, String ipAddress) {
        return client.addChildNameServer(domainName, nameServer, ipAddress);
    }

    /**
     * Delete a child nameserver.
     *
     * @param domainName domain
     * @param nameServer host
     * @return {@code {result, data: {NameServer}}}
     */
    public Map<String, Object> deleteChildNameServer(String domainName, String nameServer) {
        return client.deleteChildNameServer(domainName, nameServer);
    }

    /**
     * Change the IP address of a child nameserver.
     *
     * @param domainName domain
     * @param nameServer host
     * @param ipAddress  new IP address
     * @return {@code {result, data: {NameServer, IPAdresses}}}
     */
    public Map<String, Object> modifyChildNameServer(String domainName, String nameServer, String ipAddress) {
        return client.modifyChildNameServer(domainName, nameServer, ipAddress);
    }

    // ================================================================ transfers

    /**
     * Start an incoming transfer. Transfers never send contacts: the registrar
     * handles them, so the request always carries {@code "contacts": []}.
     *
     * @param domainName domain
     * @param eppCode    auth/EPP code
     * @param period     years added by the transfer
     * @return domain details envelope; ERROR {@code TRANSFER_DOMAIN} when the gateway refuses
     */
    public Map<String, Object> transfer(String domainName, String eppCode, int period) {
        return client.transfer(domainName, eppCode, period);
    }

    /**
     * Cancel a pending incoming transfer.
     *
     * @param domainName domain
     * @return {@code {result, data: {DomainName, Status}}}
     */
    public Map<String, Object> cancelTransfer(String domainName) {
        return client.cancelTransfer(domainName);
    }

    /**
     * Approve a pending outgoing transfer.
     *
     * @param domainName domain
     * @return {@code {result, data: {DomainName, Status}}}
     */
    public Map<String, Object> approveTransfer(String domainName) {
        return client.approveTransfer(domainName);
    }

    /**
     * Reject a pending outgoing transfer.
     *
     * @param domainName domain
     * @return {@code {result, data: {DomainName, Status}}}
     */
    public Map<String, Object> rejectTransfer(String domainName) {
        return client.rejectTransfer(domainName);
    }

    /**
     * Check whether a domain can be transferred with an auth code. {@code result} is
     * OK only when the registry reports the domain transferable.
     *
     * @param domainName domain
     * @param authCode   auth/EPP code
     * @return {@code {result, data: {TransferAvailabilityStatus, AuthCodeIsRequired, ...}, error?}}
     */
    public Map<String, Object> checkTransfer(String domainName, String authCode) {
        return client.checkTransfer(domainName, authCode);
    }

    // ================================================================ DNS zone

    /**
     * All zone records, one row per value.
     *
     * @param domainName domain
     * @return {@code {result, data: {records: [{Name, Type, TTL, Content, Disabled}]}}}
     */
    public Map<String, Object> getResourceRecords(String domainName) {
        return client.getResourceRecords(domainName);
    }

    /** Add a record with TTL 3600 and apply. */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, String content) {
        return client.addResourceRecord(domainName, name, type, content, 3600, true);
    }

    /** Add a record and apply. */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, String content, int ttl) {
        return client.addResourceRecord(domainName, name, type, content, ttl, true);
    }

    /**
     * Add (write) a record set. The gateway replaces a whole name+type on write; to
     * add one value next to existing ones use {@link #addResourceRecordValue}.
     *
     * @param domainName domain
     * @param name       label ("www"), "@" or "" for the apex, or a full name
     * @param type       one of {@link #ZONE_RECORD_TYPES}
     * @param content    value
     * @param ttl        1..86400 seconds (out-of-range values are clamped on the wire)
     * @param apply      commit right away
     * @return {@code {result, data: {Name, Type, TTL, Content}}}
     */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, String content,
                                                 int ttl, boolean apply) {
        return client.addResourceRecord(domainName, name, type, content, ttl, apply);
    }

    /** Add a record set with several values, TTL 3600, and apply. */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, List<String> content) {
        return client.addResourceRecord(domainName, name, type, content, 3600, true);
    }

    /** Add a record set with several values and apply. */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, List<String> content,
                                                 int ttl) {
        return client.addResourceRecord(domainName, name, type, content, ttl, true);
    }

    /** Add a record set with several values. */
    public Map<String, Object> addResourceRecord(String domainName, String name, String type, List<String> content,
                                                 int ttl, boolean apply) {
        return client.addResourceRecord(domainName, name, type, content, ttl, apply);
    }

    /** Edit a record with TTL 3600 and apply. */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  String content) {
        return client.editResourceRecord(domainName, recordName, name, type, content, 3600, true);
    }

    /** Edit a record and apply. */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  String content, int ttl) {
        return client.editResourceRecord(domainName, recordName, name, type, content, ttl, true);
    }

    /**
     * Edit an existing record set identified by {@code recordName}. Replaces every
     * value of the set; to change one value use {@link #replaceResourceRecordValue}.
     *
     * @param domainName domain
     * @param recordName current name as stored
     * @param name       new name
     * @param type       record type
     * @param content    new value
     * @param ttl        seconds
     * @param apply      commit right away
     * @return {@code {result, data: {Name, Type, TTL, Content}}}
     */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  String content, int ttl, boolean apply) {
        return client.editResourceRecord(domainName, recordName, name, type, content, ttl, apply);
    }

    /** Edit a record set with several values, TTL 3600, and apply. */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  List<String> content) {
        return client.editResourceRecord(domainName, recordName, name, type, content, 3600, true);
    }

    /** Edit a record set with several values and apply. */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  List<String> content, int ttl) {
        return client.editResourceRecord(domainName, recordName, name, type, content, ttl, true);
    }

    /** Edit a record set with several values. */
    public Map<String, Object> editResourceRecord(String domainName, String recordName, String name, String type,
                                                  List<String> content, int ttl, boolean apply) {
        return client.editResourceRecord(domainName, recordName, name, type, content, ttl, apply);
    }

    /** Delete a record and apply. */
    public Map<String, Object> deleteResourceRecord(String domainName, String name, String record, String recordType) {
        return client.deleteResourceRecord(domainName, name, record, recordType, true);
    }

    /**
     * Raw zone DELETE. The gateway removes the whole name+type set, whatever value is
     * named; see {@link #removeResourceRecordValue} to remove a single value.
     *
     * @param domainName domain
     * @param name       record name (as listed)
     * @param record     a value of the set
     * @param recordType type
     * @param apply      commit right away
     * @return {@code {result, data: {Name, Type, Content}}}
     */
    public Map<String, Object> deleteResourceRecord(String domainName, String name, String record, String recordType,
                                                    boolean apply) {
        return client.deleteResourceRecord(domainName, name, record, recordType, apply);
    }

    /**
     * Commit staged zone changes.
     *
     * @param domainName domain
     * @return {@code {result, data: {DomainName}}}
     */
    public Map<String, Object> applyResourceRecords(String domainName) {
        return client.applyResourceRecords(domainName);
    }

    /** Replace a whole set with TTL 3600 and apply. */
    public Map<String, Object> setResourceRecordSet(String domainName, String name, String type, List<String> contents) {
        return client.setResourceRecordSet(domainName, name, type, contents, 3600, true);
    }

    /** Replace a whole set and apply. */
    public Map<String, Object> setResourceRecordSet(String domainName, String name, String type, List<String> contents,
                                                    int ttl) {
        return client.setResourceRecordSet(domainName, name, type, contents, ttl, true);
    }

    /**
     * Make a name+type hold exactly {@code contents}. An empty list removes the set.
     *
     * @param domainName domain
     * @param name       label, "" or "@" for the apex
     * @param type       record type
     * @param contents   every value the set should hold
     * @param ttl        seconds
     * @param apply      commit right away
     * @return {@code {result, data: {Name, Type, TTL, Contents}}}
     */
    public Map<String, Object> setResourceRecordSet(String domainName, String name, String type, List<String> contents,
                                                    int ttl, boolean apply) {
        return client.setResourceRecordSet(domainName, name, type, contents, ttl, apply);
    }

    /** Remove a whole set and apply. */
    public Map<String, Object> deleteResourceRecordSet(String domainName, String name, String type) {
        return client.deleteResourceRecordSet(domainName, name, type, true, "");
    }

    /** Remove a whole set. */
    public Map<String, Object> deleteResourceRecordSet(String domainName, String name, String type, boolean apply) {
        return client.deleteResourceRecordSet(domainName, name, type, apply, "");
    }

    /**
     * Remove every value under a name+type. Without {@code knownValue} the zone is read
     * first to find one; a missing set is reported as OK with {@code Removed: false}.
     *
     * @param domainName domain
     * @param name       label or full name
     * @param type       record type
     * @param apply      commit right away
     * @param knownValue any current value of the set, saves the lookup
     * @return result envelope
     */
    public Map<String, Object> deleteResourceRecordSet(String domainName, String name, String type, boolean apply,
                                                       String knownValue) {
        return client.deleteResourceRecordSet(domainName, name, type, apply, knownValue);
    }

    /** Add one value, keep the set's TTL, and apply. */
    public Map<String, Object> addResourceRecordValue(String domainName, String name, String type, String value) {
        return client.addResourceRecordValue(domainName, name, type, value, null, true);
    }

    /** Add one value and apply ({@code ttl == null} keeps the set's TTL). */
    public Map<String, Object> addResourceRecordValue(String domainName, String name, String type, String value,
                                                      Integer ttl) {
        return client.addResourceRecordValue(domainName, name, type, value, ttl, true);
    }

    /**
     * Add one value to a set, keeping the others (read, merge, write). Adding a value
     * that is already there changes nothing ({@code Changed: false}).
     *
     * @param domainName domain
     * @param name       label
     * @param type       record type
     * @param value      value to add
     * @param ttl        seconds, or null to keep the set's TTL
     * @param apply      commit right away
     * @return result envelope
     */
    public Map<String, Object> addResourceRecordValue(String domainName, String name, String type, String value,
                                                      Integer ttl, boolean apply) {
        return client.addResourceRecordValue(domainName, name, type, value, ttl, apply);
    }

    /** Remove one value and apply. */
    public Map<String, Object> removeResourceRecordValue(String domainName, String name, String type, String value) {
        return client.removeResourceRecordValue(domainName, name, type, value, true);
    }

    /**
     * Remove one value from a set, keeping the others. Removing the last value
     * deletes the set; removing an absent value changes nothing.
     *
     * @param domainName domain
     * @param name       label
     * @param type       record type
     * @param value      value to remove
     * @param apply      commit right away
     * @return result envelope
     */
    public Map<String, Object> removeResourceRecordValue(String domainName, String name, String type, String value,
                                                         boolean apply) {
        return client.removeResourceRecordValue(domainName, name, type, value, apply);
    }

    /** Swap one value, keep the set's TTL, and apply. */
    public Map<String, Object> replaceResourceRecordValue(String domainName, String name, String type,
                                                          String oldValue, String newValue) {
        return client.replaceResourceRecordValue(domainName, name, type, oldValue, newValue, null, true);
    }

    /** Swap one value and apply ({@code ttl == null} keeps the set's TTL). */
    public Map<String, Object> replaceResourceRecordValue(String domainName, String name, String type,
                                                          String oldValue, String newValue, Integer ttl) {
        return client.replaceResourceRecordValue(domainName, name, type, oldValue, newValue, ttl, true);
    }

    /**
     * Swap one value of a set for another, keeping the rest.
     *
     * @param domainName domain
     * @param name       label
     * @param type       record type
     * @param oldValue   value as listed
     * @param newValue   replacement
     * @param ttl        seconds, or null to keep the set's TTL
     * @param apply      commit right away
     * @return result envelope; ERROR {@code EDIT_ZONE} when {@code oldValue} is not in the set
     */
    public Map<String, Object> replaceResourceRecordValue(String domainName, String name, String type,
                                                          String oldValue, String newValue, Integer ttl,
                                                          boolean apply) {
        return client.replaceResourceRecordValue(domainName, name, type, oldValue, newValue, ttl, apply);
    }

    // ================================================================ forwarding

    /**
     * URL forwarding settings. "Not set" is reported as OK with {@code Enabled: false}.
     *
     * @param domainName domain
     * @return {@code {result, data: {Enabled, RedirectAddress, ForwardType}}}
     */
    public Map<String, Object> getForwarding(String domainName) {
        return client.getForwarding(domainName);
    }

    /** Standard (plain redirect) forwarding. */
    public Map<String, Object> setForwarding(String domainName, String address) {
        return client.setForwarding(domainName, address, "Standard");
    }

    /**
     * Point the domain at a URL.
     *
     * @param domainName domain
     * @param address    target URL
     * @param type       "Standard" (redirect) or "Frame" (masked), any case
     * @return {@code {result, data: {RedirectAddress, ForwardType}}}
     */
    public Map<String, Object> setForwarding(String domainName, String address, String type) {
        return client.setForwarding(domainName, address, type);
    }

    /**
     * Remove URL forwarding.
     *
     * @param domainName domain
     * @return {@code {result, data: {DomainName}}}
     */
    public Map<String, Object> deleteForwarding(String domainName) {
        return client.deleteForwarding(domainName);
    }

    // ================================================================ utilities

    /**
     * Whether a domain ends in {@code .tr} (no network).
     *
     * @param domain domain
     * @return true for .tr domains
     */
    public boolean isTrTLD(String domain) {
        return client.isTrTLD(domain);
    }

    /**
     * Complete and normalise a contact the way registration does (no network): missing
     * fields get placeholders, names lose digits and symbols, the country becomes an ISO
     * code, phone numbers are split into number and country code.
     *
     * @param contact contact fields ({@code FirstName}, {@code Phone}, ...)
     * @return a new map with the normalised contact
     */
    public Map<String, Object> validateContact(Map<String, ?> contact) {
        return client.validateContact(contact);
    }

    // ================================================================ diagnostics

    /** @return last request: {@code {url, method, payload}} */
    public Map<String, Object> getRequestData() {
        return client.lastRequest;
    }

    /** @return last decoded response body, or {@code {raw_response: body}} when it was not JSON */
    public Object getResponseData() {
        return client.lastResponse;
    }

    /** @return transfer info of the last call: {@code {url, http_code, content_type, total_time, headers}} */
    public Map<String, Object> getResponseHeaders() {
        return client.lastResponseHeaders;
    }

    /** @return last parsed response, or the {Code, Message, Details} of the last error */
    public Object getParsedResponseData() {
        return client.lastParsedResponse;
    }

    /** @return operation of the last call, e.g. {@code "request:GET domains/info"} */
    public String getLastFunction() {
        return client.lastFunction;
    }

    /** @return gateway base URL in use */
    public String getServiceUrl() {
        return client.serviceUrl;
    }

    /** @param url gateway base URL, e.g. {@link #URL_OTE} */
    public void setServiceUrl(String url) {
        client.serviceUrl = url;
    }

    // ================================================================ settings

    /**
     * Turn error/performance telemetry on (default) or off.
     *
     * @param enabled false to send nothing
     */
    public void setErrorReportingEnabled(boolean enabled) {
        client.telemetry.setEnabled(enabled);
    }

    /**
     * Minimal telemetry: only error code and message, endpoint, timing, versions and
     * reseller ID; no request/response data, domain or host details.
     *
     * @param minimal true for the minimal payload
     */
    public void setTelemetryMinimal(boolean minimal) {
        client.telemetry.setMinimal(minimal);
    }

    /**
     * TLS certificate verification for gateway calls (on by default). Switch off only
     * on a host that cannot validate the certificate.
     *
     * @param enabled false to skip verification
     */
    public void setSslVerification(boolean enabled) {
        client.sslVerify = enabled;
    }

    // ================================================================ Java extras

    /**
     * Replace the HTTP layer (tests, proxies, custom TLS).
     *
     * @param transport transport to use
     */
    public void setTransport(HttpTransport transport) {
        client.transport = transport == null ? new JdkHttpTransport() : transport;
    }

    /**
     * Minimum gap between two {@code domains/zones} calls (default 1 second; the
     * gateway answers 429 to bursts). Zero disables pacing.
     *
     * @param seconds interval in seconds
     */
    public void setZoneMinInterval(double seconds) {
        client.setZoneMinInterval(seconds);
    }

    /**
     * Override the Sentry DSN telemetry is sent to. An empty DSN disables sending.
     *
     * @param dsn Sentry DSN ({@code https://key@host/project})
     */
    public void setTelemetryDsn(String dsn) {
        client.telemetry.setDsn(dsn);
    }

    /** Package access for tests. */
    DNARest engine() {
        return client;
    }
}
