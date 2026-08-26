# Module Boundaries

| Module | Owns | May depend on |
| --- | --- | --- |
| auth | Authentication mechanism and session/token concerns | users |
| users | Accounts, multi-role assignments (`CLIENT`, `WORKER`, `CONTRACTOR`, `ADMIN`), account lifecycle | None |
| workers | Worker profiles | users |
| contractors | Contractor profiles and time-varying worker business associations | users, workers |
| clients | Client profiles | users |
| teams | Teams and worker-team memberships | workers, contractors when a contractor sponsors a team |
| projects | Projects | clients |
| requirements | Workforce requirements: location, schedule, skills, quantity, budget/rate, accommodation, food, notes | projects |
| bookings | Provider request, accept/reject outcome, and accepted provider-to-requirement agreement | requirements, workers, teams, contractors |
| verification | Manual verification cases and status for the initial launch | users, workers, contractors, clients as applicable |
| attendance (future) | Attendance against a booking | bookings |
| payments (future) | Payment processing, provider integration, wallet, or escrow; MVP keeps only necessary payment-related booking status | bookings |
| reviews (future) | Review and moderation lifecycle | bookings |

Dependencies show permitted business references, not permission to query another module's tables. The owning module provides an application-level interface for needed reads or commands. `auth` may share narrowly scoped current-user context, but no feature module should own authentication mechanics.

## Extraction rule

Keep modules in one deployable application until there is evidence that a module needs independent scaling, release timing, availability isolation, or operational ownership. Extraction requires a dedicated API/event contract, data ownership plan, observability, and failure handling; it is not a response to package size alone.
