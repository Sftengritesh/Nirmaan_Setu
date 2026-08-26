# Workforce Requirement Architecture

## 1. Requirement Purpose & Ownership Chain

A `workforce_requirement` represents specific workforce demand for a client's construction project (e.g. quantity of workers, skill required, duration, location, and compensation).
- **Ownership Chain**: `AuthPrincipal` → `app_user.id` → `client_profile.id` → `project.client_profile_id` → `workforce_requirement.project_id`.
- **Server-Side Ownership Verification**: The server resolves the client profile for the authenticated principal's user ID, fetches the target project, and verifies that `project.client_profile_id` matches the client's profile ID. Untrusted request bodies carrying `userId` or `clientProfileId` are ignored.

## 2. Role Boundaries & Multi-Role Support

- **Authorization**: All requirement endpoints are guarded by `@PreAuthorize("hasRole('CLIENT')")`.
- **Multi-Role Compatibility**: Dual or triple role users (e.g. `CLIENT + CONTRACTOR` or `CLIENT + WORKER`) can create and manage project requirements without role mutation. The application never alters user roles (`app_role` / `user_role`).

## 3. Compensation Model & Database Enforcement Notice

- **Compensation Constraint**: A requirement must specify **either** a `daily_rate` **or** a `budget_amount`, but never both and never neither.
  - Option A: `daily_rate >= 0` AND `budget_amount == null`
  - Option B: `daily_rate == null` AND `budget_amount >= 0`
- **Database Verification Notice**: The V1 schema contains individual non-negative check constraints (`chk_requirement_daily_rate` and `chk_requirement_budget_amount`), but does **not** contain a SQL XOR CHECK constraint. Therefore, mutual exclusion is **enforced by the application/domain layer**.

## 4. Requirement Status Lifecycle & Explicit Actions

- **Status Vocabulary**: `DRAFT`, `OPEN`, `FULFILLED`, `CANCELLED`.
- **Default Status**: Newly created requirements default to `DRAFT`.
- **Client-Side State Transitions**:
  - `DRAFT` → `OPEN`: Triggered via `POST /api/requirements/{requirementId}/open`
  - `DRAFT` → `CANCELLED`: Triggered via `POST /api/requirements/{requirementId}/cancel`
  - `OPEN` → `CANCELLED`: Triggered via `POST /api/requirements/{requirementId}/cancel`
- **State Transition Restrictions**:
  - Clients **cannot** manually transition a requirement to `FULFILLED`. Transition to `FULFILLED` is controlled exclusively by the Booking module (Agent 08) when bookings fulfill the requested quantity.
  - Arbitrary status updates via generic status endpoints are prohibited.
- **Update Rules**:
  - A requirement is editable **only** while in `DRAFT` status.
  - Once `OPEN`, standard requirement fields cannot be updated (only cancellation is allowed).
  - `FULFILLED` and `CANCELLED` requirements are terminal and immutable.

## 5. Skill & Worker Type Rules

- **Worker Types**: `SKILLED_WORKER`, `LABOUR` (strictly matching V1 constraint `chk_requirement_worker_type`).
- **Skill Foreign Key**: `skill_id` references the `skill` table. The application validates that `skill_id` exists and has `is_active == true`. Skill reference data is strictly read-only.

## 6. API Endpoints Summary

- `POST /api/projects/{projectId}/requirements` — Create requirement for project (defaults to `DRAFT`)
- `GET /api/projects/{projectId}/requirements` — List all requirements for project (ownership checked)
- `GET /api/requirements/{requirementId}` — Get requirement by ID (ownership checked)
- `PUT /api/requirements/{requirementId}` — Update requirement while `DRAFT` (ownership checked)
- `POST /api/requirements/{requirementId}/open` — Publish requirement from `DRAFT` to `OPEN` (ownership checked)
- `POST /api/requirements/{requirementId}/cancel` — Cancel requirement from `DRAFT` or `OPEN` to `CANCELLED` (ownership checked)
