# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Unreleased

First release: a faithful Java port of [php-dna](https://github.com/domainreseller/php-dna)
(4.0.4 plus the fixes of php-dna commit f946d69).

### Added

- `DomainNameApi` client with all 45 php-dna methods: account and balance, availability
  check, domain and TLD lists, domain details, sync, contacts, nameservers, theft protection
  lock, privacy protection, renewal, registration (including .tr TRABIS attributes), child
  nameservers, transfers (start, check, cancel, approve, reject), DNS zone records (record and
  record-set level, value-level add/replace/remove, apply) and URL forwarding.
- Overloads for every php-dna default parameter; constructor accepts the test-mode flag as a
  boolean or as a string (`"true"`, `"1"`, `"on"`, `"yes"`).
- Return values identical to php-dna (`LinkedHashMap` / `ArrayList`): same keys, string flags,
  formatted money strings and OK/ERROR envelopes. Errors are returned, never thrown.
- HTTP layer on `java.net.http` with the `DomainNameAPI-Java/1.0.0` User-Agent, 30 s / 10 s
  timeouts, optional TLS verification bypass, PHP-compatible query strings and JSON bodies,
  one-second pacing of DNS zone calls, and a pluggable `HttpTransport`.
- php-dna error mapping: `API_<status>` codes with the gateway's own code as `GatewayCode`,
  credential redirects, empty bodies, ABP boilerplate messages (never replaced by the raw body),
  flattened validation errors, and transport failures as errors with `CURL_<errno>` codes
  (`CURL_28` timeout).
- Refusals are errors, not false successes: `success:false` from register, transfer and renew
  returns `REGISTER_DOMAIN` / `TRANSFER_DOMAIN` / `RENEW_DOMAIN`; a failed automatic zone apply
  returns the apply error; `getCurrentBalance` returns `BALANCE` for currencies the account does
  not report.
- Transfers never send contacts (`contacts: []`); the API has no contacts parameter.
- Forwarding: only "Redirect information not found" means "not set"; `deleteForwarding` reports
  `Removed: true/false`.
- `modifyPrivacyProtectionStatus` accepts the status as text (`"1"`, `"true"`, `"on"`, `"yes"`);
  the reason is not sent.
- Contact types are matched case-insensitively and unknown types are kept; phone numbers with
  their own `+` country code keep it; a missing State falls back to the city.
- Sentry telemetry ported from php-dna (credential masking, ignored errors, performance
  sampling, minimal mode, `application=DomainNameAPI-Java`); the built-in DSN is empty until the
  Java Sentry project exists, and `setTelemetryDsn` overrides it.
- Built-in JSON codec (`Json`) — no runtime dependencies.
- Conformance test running all 134 php-dna fixtures, plus unit tests for the HTTP layer, error
  mapping, telemetry and utilities.
- Runnable examples for every php-dna example plus DNS zone, forwarding, transfer approval,
  `isTrTLD` and `validateContact`.
- README in English, Turkish, German, French, Italian, Dutch, Azerbaijani, Arabic and
  Simplified Chinese.

[1.0.0]: https://github.com/domainreseller/java-dna/releases/tag/V1.0.0
