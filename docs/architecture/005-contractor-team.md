# Contractor Profile and Team Architecture

## 1. Contractor Profile Ownership

A `contractor_profile` record belongs to exactly one `app_user`. The `user_id` column carries a `UNIQUE` constraint in the V1 schema. The application always derives `user_id` from the authenticated `AuthPrincipal`; it is never accepted from the HTTP request body. This prevents any user from creating or modifying another user's contractor profile.

## 2. Contractor Role Requirement & Team Manager Authorization

- **Contractor Profile Operations**: Protected by `@PreAuthorize("hasRole('CONTRACTOR')")`.
- **Team Management Operations**: Protected by `@PreAuthorize("hasRole('CONTRACTOR')")`.
- **Role Boundary**:
  - A user with the `CONTRACTOR` role (or `WORKER` + `CONTRACTOR` dual roles) is authorized to create and manage teams.
  - A user holding only the `WORKER` role is **NOT** authorized to create or manage teams (receives HTTP 403 Forbidden).
  - The application never automatically modifies user roles. All authorization relies on Spring Security method security against the server-authenticated principal's assigned roles.

## 3. Contractor-Worker Association Strategy

Contractor-worker associations represent time-bound business associations stored in `contractor_worker_association`.
- **Nature of Relationship**: Represents a business association, NOT employment or legal ownership.
- **Worker Safety**: Association operations never modify `worker_profile`, mutate worker roles, or create/delete worker profiles.
- **Composite Primary Key**: `(contractor_profile_id, worker_profile_id, starts_on)`.
- **Overlapping Relationships**: Exclusivity locks are not enforced. Workers may hold active associations with multiple contractors or teams simultaneously.
- **Non-Destructive Ending**: `POST /api/contractors/workers/{workerProfileId}/end` sets `ends_on` on the historical association row. Historical rows are preserved and never physically deleted. Date ordering is validated (`ends_on >= starts_on`).

## 4. Team Ownership & Status Strategy

A `team` represents a crew of workers managed by a contractor.
- **Authoritative Ownership**: Sourced strictly via `team.manager_user_id` (`app_user.id` derived from `AuthPrincipal`). Holding the `CONTRACTOR` role alone does not grant access to teams owned by another contractor.
- **Team Status Vocabulary**: Preserves existing V1 schema vocabulary `status VARCHAR(20)` with values `ACTIVE` or `INACTIVE`. Only `ACTIVE` teams can accept new team members.
- **Non-Destructive Member Leaving/Removal**: `POST /api/teams/{teamId}/members/{workerProfileId}/end` sets `ends_on` on the historical `team_member` row. Historical membership rows are never physically deleted. Date ordering is validated (`ends_on >= starts_on`).

## 5. Endpoints & Scope Summary

### Contractor Endpoints
- `POST /api/contractors/profile` — Create contractor profile
- `GET /api/contractors/profile/me` — Get own contractor profile
- `PUT /api/contractors/profile/me` — Update own contractor profile
- `POST /api/contractors/workers` — Create contractor-worker association
- `GET /api/contractors/workers` — List associated workers
- `POST /api/contractors/workers/{workerProfileId}/end` — End worker association (sets `ends_on`)

### Team Endpoints
- `POST /api/teams` — Create team
- `GET /api/teams` — List teams managed by current user
- `GET /api/teams/{teamId}` — Get team details (ownership check)
- `PUT /api/teams/{teamId}` — Update team (ownership check)
- `POST /api/teams/{teamId}/members` — Add team member (ownership check)
- `GET /api/teams/{teamId}/members` — List team members (ownership check)
- `POST /api/teams/{teamId}/members/{workerProfileId}/end` — End team membership (sets `ends_on`)

## 6. Known Limitations

- Multi-manager co-ownership of teams is not supported in MVP.
- Public search for workers or contractors is excluded from MVP scope.
- Booking and quotation management are outside the scope of this module.
