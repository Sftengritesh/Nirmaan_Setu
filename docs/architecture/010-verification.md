# Verification Architecture

## 1. Purpose

The `verification` domain provides a manual admin review workflow for validating the identity of platform subjects.

Subject types (matching V1 `chk_verification_subject`):
- `USER` — a raw `app_user` record
- `WORKER` — a `worker_profile` record
- `CONTRACTOR` — a `contractor_profile` record
- `CLIENT` — a `client_profile` record

## 2. Subject Resolution — Server-Side Only

Subjects are **always resolved server-side** from the authenticated principal. Clients never supply subject IDs or subject type in the request body.

| Subject Type | Resolution Path |
|---|---|
| `USER` | `AuthPrincipal.userId()` → `app_user.id` |
| `WORKER` | `AuthPrincipal.userId()` → `worker_profile.user_id` → `worker_profile.id` |
| `CONTRACTOR` | `AuthPrincipal.userId()` → `contractor_profile.user_id` → `contractor_profile.id` |
| `CLIENT` | `AuthPrincipal.userId()` → `client_profile.user_id` → `client_profile.id` |

`verification_type` is hardcoded to `MANUAL` (the only value allowed by `chk_verification_type`).

## 3. Status Lifecycle

```
[ Subject Submits ] → PENDING
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
[ Admin VERIFY ]                 [ Admin REJECT ]
   VERIFIED                         REJECTED
(reviewed_by, at set)          (reviewed_by, at set)
```

Allowed transitions:
- `PENDING` → `VERIFIED` (admin only)
- `PENDING` → `REJECTED` (admin only)

Forbidden:
- `VERIFIED` → anything (terminal)
- `REJECTED` → anything (terminal)

No `CANCELLED` status — not in V1.

## 4. Duplicate Submission Rules

- **Block**: A new submission is blocked if a `PENDING` record already exists for the same subject.
- **Allow**: Re-submission is allowed after `REJECTED`.
- **Block**: Re-submission is blocked after `VERIFIED`.

**Concurrency note**: These rules are enforced at the application level. V1 has no uniqueness constraint on `(subject_type, subject_*_id)`. Concurrent duplicate submission is a known limitation; a future migration could add a partial unique index on `status = 'PENDING'` per subject.

## 5. Admin Review

- `reviewed_by_user_id` = `AuthPrincipal.userId()` of the admin performing the action.
- `reviewed_at` = current `Clock.instant()`.
- `notes` = optional free-text field from `ReviewVerificationRequest`.
- Admin self-review is not prevented in MVP (future product decision).
- All admin endpoints require `@PreAuthorize("hasRole('ADMIN')")`.

## 6. API Endpoints

### Subject Self-Service (own verification only)
- `POST /api/verifications/user` — Submit USER verification (any authenticated user)
- `POST /api/verifications/worker` — Submit WORKER verification (`hasRole('WORKER')`)
- `POST /api/verifications/contractor` — Submit CONTRACTOR verification (`hasRole('CONTRACTOR')`)
- `POST /api/verifications/client` — Submit CLIENT verification (`hasRole('CLIENT')`)
- `GET /api/verifications/me` — View own verification records (any authenticated user)

### Admin Endpoints (`hasRole('ADMIN')`)
- `GET /api/admin/verifications` — List all verifications (optional `status` and `subjectType` query params)
- `GET /api/admin/verifications/{verificationId}` — Get a single verification
- `POST /api/admin/verifications/{verificationId}/verify` — Approve with optional notes
- `POST /api/admin/verifications/{verificationId}/reject` — Reject with optional notes
