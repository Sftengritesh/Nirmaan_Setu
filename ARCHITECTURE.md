# NirmaanSetu Architecture

## 1. System overview

NirmaanSetu is an Indian construction-workforce platform. Clients create projects and workforce requirements. An individual worker, a temporary team, or a contractor can fulfil a requirement. The first release serves a known local network and must remain simple to operate while preserving a path to wider expansion.

The system starts as a modular monolith: one deployable backend application, one PostgreSQL database, and one frontend application. Business modules own their rules and data access. Cross-module communication stays explicit through module interfaces, not shared internal classes or tables.

## 2. Initial scope

| MVP modules | Responsibility |
| --- | --- |
| auth | Sign-in and session boundary; implementation is deferred. |
| users | Account identity, lifecycle, and role assignments. |
| workers | Worker work profile and availability-related details. |
| contractors | Contractor or workforce-supplier profile. |
| clients | Client profile. |
| teams | Time-bound worker groups. |
| projects | Client construction projects. |
| requirements | Workforce requested for a project. |
| bookings | Accepted provider-to-requirement relationship. |
| verification | Separately owned, manually operated verification status for the initial local launch. |

| Future modules | Why deferred |
| --- | --- |
| attendance | Needs confirmed booking and on-site operating rules. |
| payments | MVP records only necessary payment-related booking status; it does not process payments. Gateway, wallet, escrow, and payout design require later business and compliance rules. |
| reviews | Needs a completed-work lifecycle and moderation policy. |

`verification` remains an MVP boundary and may be performed manually by the platform/admin team for the local launch. It is kept separate so trust status is not mixed into worker or contractor profiles.

## 3. Frontend architecture

Use Next.js with TypeScript, organized by business feature: `projects`, `requirements`, `workers`, `contractors`, and so on. A feature owns its screens, feature-specific components, validation, and API client calls. Shared UI primitives belong in a small reusable UI layer; they must not contain business rules. Shared API-client infrastructure owns transport, authentication attachment, error normalization, and generated or hand-maintained contract types.

Avoid a single catch-all `components` directory, duplicated request logic, and frontend assumptions about database entities. Route organization may follow feature boundaries, but route files should compose feature modules rather than contain all behavior.

## 4. Backend architecture

Use Java with Spring Boot and Maven. Each module contains its own web adapter, application use cases, domain rules, and persistence adapter as needed. Package names should make ownership visible, for example `com.nirmaansetu.projects` and `com.nirmaansetu.requirements`.

Dependencies must point inward: web and persistence code depend on application/domain code; domain rules do not depend on HTTP or ORM concerns. Modules may use another module only through a deliberate public application interface. Do not use generic base services, controllers, repositories, or a catch-all `UserService`.

## 5. Domain boundaries

| Concept | Meaning | Boundary rule |
| --- | --- | --- |
| User | Account identity and login lifecycle. | Does not own business capabilities or work history. |
| Role | Permission-bearing capability assigned to a user. | A user may hold `CLIENT`, `WORKER`, `CONTRACTOR`, and `ADMIN` roles simultaneously; role changes do not delete the user. |
| Profile | Details specific to a worker, contractor, or client. | Kept separate from identity and authorization. |
| Worker | A person able to provide construction work. | Can work independently and may join or leave teams and contractor associations over time. |
| Contractor | Person or business that manages or supplies workforce. | A worker association is a changing business relationship, not permanent ownership or assumed employment. |
| Team | Group of workers associated for a period. | Membership is time-bound and separate from worker identity. |
| Client | Party requesting workforce. | Owns projects, not worker records. |
| Project | Construction work context created by a client. | Owns requirements. |
| Requirement | Workforce requested for a project. | Includes project, location, start date, duration, worker type, skill, quantity, rate or budget, accommodation, food, and additional notes; it can be offered to an individual worker, team, or contractor. |
| Booking | Accepted provider-to-requirement relationship. | A provider accepts or rejects a request; acceptance creates the booking, without changing identity or team membership. |

The core data rule is to separate identity, role, profile, and business relationship. A single user may hold multiple roles and has no separate login per role. Model worker-team membership and worker-contractor association as dated business relationships when implementation begins. A worker can work independently and can change associations while retaining their account and work profile.

## 6. Database responsibility

PostgreSQL is the system of record for transactional data. Each backend module owns its tables and database access; other modules must not query those tables directly. Foreign-key and reporting needs may require references across module boundaries, but writes and rules remain with the owning module.

Use version-controlled, forward-only migrations once schema work begins. Each migration must be reviewable, reversible through a new migration when necessary, and tested against an empty database and an upgrade path. No schema is defined by this architecture task.

## 7. Authentication and authorization boundary

Authentication establishes who is using the system. Authorization establishes which actions that user may take. `auth` owns authentication mechanisms and session/token handling; `users` owns account lifecycle and role assignment. Feature modules enforce authorization for their own actions using shared authorization policy interfaces.

Use role checks for broad capability and ownership checks for resource access. The initial roles are `CLIENT`, `WORKER`, `CONTRACTOR`, and `ADMIN`; a user can hold multiple roles concurrently. A contractor role alone must not grant access to every contractor, worker, project, or booking.

A newly created user starts with zero roles. No roles (such as CLIENT, WORKER, CONTRACTOR, or ADMIN) are automatically assigned to a user upon registration. Additionally, the system does not trust or accept role information supplied by the client. Role assignment is handled strictly via server-side administrative workflows.

## 8. API boundary

REST is the initial API style. Organize endpoints by resource and feature, version public APIs deliberately, and use request/response DTOs rather than exposing persistence entities. Contracts specify validation errors, authorization behavior, pagination, and stable identifiers. The backend owns business validation; the frontend may repeat validation only for usability.

No endpoints are created yet.

## 9. Files and storage boundary

Application files such as verification documents or project media must be stored outside the database, with the database retaining metadata, ownership, purpose, status, and storage reference. Storage access belongs behind a dedicated adapter so a local launch choice can later move to managed object storage without changing feature rules. Upload validation, malware scanning policy, and retention must be defined before accepting files.

Repository assets, when needed, are controlled source assets only:

| Directory | Purpose |
| --- | --- |
| `assets/brand/` | Approved logos, marks, and brand source files. |
| `assets/images/` | Curated product illustrations or owned imagery. |
| `assets/icons/` | Project-owned icons not supplied by the UI system. |
| `assets/ui-reference/` | Licensed or approved visual references with source attribution. |
| `assets/design-generated/` | Approved generated design assets, including provenance. |

These directories are not created until an approved asset needs a home. Do not commit downloaded stock imagery, external hotlinks, or placeholder photographs.

## 10. External integrations boundary

External services, including SMS/OTP providers, maps, and storage, sit behind module-owned adapter interfaces. Provider-specific DTOs and webhooks must not leak into core domain rules. Payments are off-platform in the MVP, so no payment-provider integration is planned. Add a future integration only with a documented business need, operational owner, failure behavior, and credential-management plan.

## 11. Testing strategy

Prioritize module-level unit tests for business rules, application tests for use cases and authorization, repository integration tests against PostgreSQL, and API contract tests for public behavior. Add a small set of end-to-end tests for critical MVP journeys: client creates a project and requirement; a provider receives it; the provider accepts or rejects it; acceptance creates a booking. Tests must cover independent workers as well as workers associated with teams or contractors.

## 12. Deployment direction

Deploy the backend as one containerized Spring Boot application and the frontend as one Next.js application. Use managed PostgreSQL in production. Keep configuration in environment variables or a secret manager, use separate environments, run migrations as a controlled release step, and make database backups and restore checks part of operations. GitHub-based CI/CD is a later operational decision; this repository adds no pipeline yet.

## 13. Observability direction

Use structured logs with request or correlation identifiers, health/readiness checks, error tracking, and baseline application and database metrics. Logs must not include passwords, OTPs, full identity documents, payment data, or unnecessary personal data. Track business events only after their ownership and privacy purpose are defined.

## 14. Security principles

Security direction is detailed in `SECURITY.md`. Core rules are least privilege, server-side validation, explicit authorization, secure credential handling, audited sensitive changes, protected uploads, rate limits on abuse-prone actions, and no secrets in source control.

## 15. Ambiguities requiring founder decisions

- The exact lifecycle and permissions of `ADMIN`, including who may assign or remove roles.
- The precise status vocabulary and audit requirements for manual verification.
- The payment-related booking status needed for project history while payments remain off-platform.
- Booking cancellation, replacement, and expiry behavior after an initial accept or reject decision.
