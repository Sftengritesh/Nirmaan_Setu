# Security Direction

This document defines guardrails for implementation. It does not create authentication, payment, storage, or security infrastructure.

## Authentication and passwords

Use an established Spring Security approach when auth is implemented. Store passwords only with a modern adaptive one-way password hash; never log, return, or encrypt passwords for later recovery. Protect sessions/tokens with secure transport, expiration, rotation or revocation strategy, and appropriate browser protections.

## OTPs and rate limits

Generate OTPs securely, store only a protected representation where feasible, make them short-lived and single-use, and limit verification attempts and send frequency. Rate-limit login, OTP, password reset, file upload, and other abuse-prone actions at a suitable edge or application boundary. The OTP provider and exact thresholds require an implementation decision.

## Authorization and validation

Authorize every protected action server-side using role and resource-ownership checks. Treat all client input, uploaded filenames, query parameters, and webhook payloads as untrusted. Apply allow-list validation, size limits, safe error responses, and authorization before reading or changing sensitive resources.

## Sensitive data and audit logging

Collect only personal data needed for an approved purpose. Restrict access by role and ownership, use TLS in transit and platform-supported encryption at rest, and define retention/deletion rules before collecting identity documents. Audit sensitive actions such as role changes, verification decisions, booking status changes, payment-related changes, and privileged access. Audit logs must identify actor, action, target, time, and outcome without storing secrets or excessive personal data.

## Files

Validate file type by content as well as extension, enforce size limits, generate server-side names, store uploads outside the web root, and authorize every download. Define malware-scanning and quarantine behavior before accepting verification documents. Never trust client-provided content type or filename.

## Payments

Payments are off-platform for the MVP. NirmaanSetu may retain only the payment-related booking status needed for project history; it must not process card data, operate a gateway, wallet, escrow, or payout flow. If payments later enter the platform, use a compliant provider, keep card data out of NirmaanSetu systems, verify provider webhooks, and maintain an auditable transaction trail.

## Secrets and dependencies

Keep secrets in environment-specific secret storage, never source control or frontend bundles. Limit production-secret access, rotate credentials, and scan repositories and CI logs for accidental disclosure. Review dependencies for maintenance, license, and security impact; patch known vulnerabilities as part of normal maintenance.

## Incident readiness

Maintain useful structured logs, monitored failures, tested backups, and a documented process for revoking credentials, disabling compromised accounts, and notifying responsible owners. Specific legal retention and breach-notification requirements need local legal advice before launch.
