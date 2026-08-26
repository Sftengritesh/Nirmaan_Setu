# Worker Profile Architecture

## 1. Worker Profile Ownership

A `worker_profile` row belongs to exactly one `app_user`. The `user_id` column carries a `UNIQUE` constraint in V1. The application always derives `user_id` from the authenticated `AuthPrincipal`; it is never accepted from the HTTP request body. This prevents any user from creating or modifying another user's profile.

## 2. Worker Role Requirement

All worker profile endpoints are protected by `@PreAuthorize("hasRole('WORKER')")`. A user without the `WORKER` role receives a 403 response before the controller method is entered. The `auth` module is authoritative over role assignment; the `worker` module never writes to `user_role` or `app_role`.

## 3. Worker + Contractor Multiple-Role Behavior

A user may hold `WORKER`, `CONTRACTOR`, and other roles simultaneously. The worker profile service reads no role information beyond what Spring Security enforces at the `@PreAuthorize` boundary. Creating or updating a worker profile does not touch `user_role`, preserving any existing `CONTRACTOR` or other role assignments intact.

## 4. Skill Relationship

Skills are stored in the normalized `skill` reference table (seeded in V1). A worker's skills are associated through the `worker_skill` join table, which has a composite primary key `(worker_profile_id, skill_id)` — preventing duplicates at the database level. The application layer validates that the `skill_id` exists and is active (`is_active = TRUE`) before inserting. The worker profile API cannot create new skill reference rows; skill administration is a future concern.

## 5. Location Strategy

Location is a plain `VARCHAR(255)` field matching the V1 schema. No geospatial extensions (PostGIS, coordinates, geocoding) are introduced. Text-based location is sufficient for the MVP local-network launch. Proximity-based search can be added later with a schema change if product requirements justify it.

## 6. Availability Strategy

Availability is stored as `availability_status VARCHAR(20)` with a database check constraint enforcing `('AVAILABLE', 'LIMITED', 'UNAVAILABLE')`. The domain enum `AvailabilityStatus` matches this vocabulary exactly. The service rejects any value outside these three with a clean `WorkerProfileException` before the database is reached.

## 7. Verification Boundary

The `verification` table in V1 supports future manual verification of workers. This module does **not** implement KYC, Aadhaar verification, document OCR, or any government API integration. Verification status is an administrative workflow and is excluded from the current worker profile API scope.

## 8. Profile Completeness

Profile completeness scoring is NOT implemented in MVP. The API returns the persisted profile fields as-is. Completeness calculation may be added in a future phase if product requirements define a specific scoring model.

## 9. Authorization Rules

- **WORKER role** — required to call any worker profile endpoint.
- **Ownership** — `userId` is always sourced from `AuthPrincipal`, never from the client. A worker can only create, read, and update their own profile.
- **ADMIN access** — not implemented in this phase; broad admin functionality is deferred.
- **No public worker search** — no endpoints expose other workers' profiles.

## 10. Known Limitations

- Worker search and discovery endpoints are not implemented. Workers can only view their own profile.
- Verification status is not exposed; it requires a future verification workflow.
- Location is free-text only; geospatial queries are not supported.
- PostgreSQL integration tests require a live database and are not runnable in the current environment (BLOCKED in CI without a PostgreSQL instance).
- Skill administration (creating/deactivating skills) is not part of this module.
