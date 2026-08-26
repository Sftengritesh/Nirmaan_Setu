# Database Design

## Entity overview

The MVP uses PostgreSQL with normalized module-owned tables:

| Module | Tables |
| --- | --- |
| users | `app_user`, `app_role`, `user_role` |
| workers | `worker_profile`, `skill`, `worker_skill` |
| contractors | `contractor_profile`, `contractor_worker_association` |
| clients | `client_profile` |
| teams | `team`, `team_member` |
| projects | `project` |
| requirements | `workforce_requirement` |
| bookings | `booking` |
| verification | `verification` |

`app_user` intentionally contains account identity only. It does not contain credentials, phone numbers, or role-specific details; authentication implementation owns credential design later.

## Relationship overview

```text
app_user --< user_role >-- app_role
app_user --0..1 worker_profile --< worker_skill >-- skill
app_user --0..1 contractor_profile --< contractor_worker_association >-- worker_profile
app_user --0..1 client_profile --< project --< workforce_requirement --< booking
team --< team_member >-- worker_profile
booking -> exactly one provider: worker_profile | team | contractor_profile
verification -> exactly one subject: app_user | worker_profile | contractor_profile | client_profile
```

One user can have multiple roles and separate worker, contractor, and client profiles. A user who is both a worker and contractor therefore has one identity and two profiles.

## Important constraints

- `user_role` has a composite primary key, preventing a duplicated role assignment while allowing multiple roles per user.
- Worker profiles, contractor profiles, and client profiles each have a unique user reference, preventing duplicate profiles of the same type without preventing multiple profile types.
- `worker_skill` is a many-to-many table; skills are controlled reference rows, never comma-separated text.
- `team_member` and `contractor_worker_association` use a start date in their primary keys and optional end date. They retain history and do not restrict a worker to a single team or contractor.
- Date ranges cannot end before they start. Overlapping associations are intentionally allowed because exclusivity is not an approved business rule.
- Requirements require a positive quantity and duration and a reusable skill. Compensation may be specified as daily rate, total budget, or left unspecified for the MVP; supplied amounts cannot be negative.
- `worker_type` is the broad workforce category, while `skill` identifies the specific trade or capability. For example, `SKILLED_WORKER` with `RAJ_MISTRI`, or `LABOUR` with `LABOUR`.
- A booking has one `provider_type`, exactly one matching provider reference, and a positive fulfilled quantity. Worker bookings are fixed at quantity `1`; team and contractor bookings may supply more than one worker. Foreign keys prevent references to nonexistent providers.
- Verification has one typed subject and supports only the approved manual verification mechanism.

## Identifier strategy

Every entity uses a UUID primary key. The future application layer supplies UUIDs, so the database does not require an extension or use a second identifier strategy. Fixed UUIDs in the migration are controlled reference-data identifiers only.

## Timestamp strategy

Mutable business entities have `created_at` and `updated_at` as `TIMESTAMPTZ`; a small database trigger maintains `updated_at` consistently. Join/reference tables receive only timestamps that explain their history or assignment. Historical workforce relationships use `starts_on` and `ends_on` as business-effective dates.

## Status strategy

The schema uses checked `VARCHAR` codes rather than PostgreSQL enums. This keeps allowed MVP values explicit while allowing a future migration to add a value without changing a database type. Statuses are deliberately small: account (`ACTIVE`, `DISABLED`), availability, project, requirement, booking, team, and verification. The migration does not introduce payment status because its approved vocabulary is still unresolved and no payment workflow exists.

## Index strategy

- Available worker location supports local workforce search.
- Skill-to-worker index supports matching workers by skill.
- Contractor location supports local contractor search.
- Team and contractor-association worker/date indexes support current and historical association lookup.
- Project client/status supports a client's project list.
- Open requirement location/start-date supports requirement discovery.
- Booking requirement and provider indexes support request and provider history.

Primary keys, unique constraints, and foreign-key-side indexes needed by these queries are not duplicated without a query purpose.

## Migration strategy

The migration uses Flyway's conventional versioned filename and `db/migration` location: `V1__initial_mvp_schema.sql`. Flyway is selected because it is mature, conventional for Spring Boot/PostgreSQL, and supports forward-only, ordered SQL migrations. No Flyway dependency is added here because the backend Maven project does not yet exist. The Backend Engineer should configure the existing migration directory as the Flyway location when bootstrapping Maven.

Apply migrations against an empty disposable PostgreSQL database with:

```text
flyway -locations=filesystem:db/migration migrate
```

Then run the focused transactional verification script:

```text
psql -v ON_ERROR_STOP=1 -d <database> -f db/test/verify_v1_schema.sql
```

The test script creates deterministic temporary rows and rolls them back. It is not seed or demo data.

## Provider and booking decision

One `booking` table stores provider requests and their outcomes. Its `provider_type` plus three nullable foreign-key columns is guarded by a check constraint that requires exactly one matching provider: worker, team, or contractor. `quantity` records how many workers that provider supplies: it must be positive, is exactly `1` for an individual worker, and may be greater than `1` for a team or contractor. `PENDING` represents a request; `ACCEPTED` is the product's booking; `REJECTED` records a declined request. The schema intentionally does not calculate total fulfilment or prevent overbooking yet.

## Workforce relationship decisions

`team_member` models dated worker-team membership. `contractor_worker_association` models dated, non-employment business association. Neither relationship is stored on `worker_profile`, so workers remain independent and can change associations while preserving history.

## Future considerations

- Define cancellation, expiry, replacement, and payment-history status vocabulary before adding corresponding fields or statuses.
- Define manual-verification types, reviewer authorization, and retention before expanding `verification`.
- Add location normalization or geospatial search only when real search needs justify it.
- Add credential and contact fields only within the authentication/identity design, with the security and privacy rules applied.
