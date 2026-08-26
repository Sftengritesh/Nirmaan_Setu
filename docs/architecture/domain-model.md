# Domain Model Boundaries

## Relationship model

NirmaanSetu must not collapse people, organizations, and work arrangements into a single record.

```text
User (identity)
  -> One or more role assignments: CLIENT, WORKER, CONTRACTOR, ADMIN
  -> Worker / Contractor / Client profile (business profile)

Worker <-> Worker-Team Membership <-> Team
Worker <-> Worker-Contractor Association <-> Contractor
Client -> Project -> Requirement <- Booking <- Workforce Provider
```

A single user may hold multiple roles at the same time, including both `WORKER` and `CONTRACTOR`; all roles use the same login identity and role-specific profiles. A workforce provider in a booking is exactly one of: an individual worker, a team, or a contractor. The booking should identify the provider type and stable provider identity, while membership and association history remain separate. This avoids rewriting past bookings when a worker changes team or contractor.

## Ownership rules

- A client owns projects and projects own requirements.
- A requirement describes a need; it includes project, location, start date, duration, worker type, skill, quantity, rate or budget, accommodation, food, and additional notes. It does not create an employment relationship.
- A booking begins only when an individual worker, team, or contractor accepts a requirement request. Rejection does not create a booking. Quotations, negotiation, and contracts are outside MVP scope.
- Contractor-worker associations and team memberships are dated, changeable business relationships, not permanent ownership or assumed employment.
- Verification status belongs to the verified subject and verification process, not to the general user account.

## Questions deferred to product design

Booking cancellation/replacement policy, availability, worker location precision, the detailed manual-verification workflow, and the off-platform payment-status vocabulary must be decided before schema and workflow implementation.
