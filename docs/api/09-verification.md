# Verification API

## Purpose
Manages manual identity verification requests submitted by platform subjects (`USER`, `WORKER`, `CONTRACTOR`, `CLIENT`) and admin review workflows (`PENDING` -> `VERIFIED` / `REJECTED`).

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
- Subject endpoints: `USER` (`isAuthenticated()`), `WORKER`, `CONTRACTOR`, `CLIENT`
- Admin endpoints: `ADMIN`

---

## Verification Rules
- `subjectType`, `subjectUserId`, and specific profile IDs are resolved server-side based on the calling user's principal and active profile.
- Verification type is automatically assigned as `MANUAL`.
- Only records in `PENDING` status may be reviewed (approved or rejected) by an admin.

---

## Subject Endpoints

### POST /api/verifications/user
Submits user-level identity verification request. `201 Created`.

### POST /api/verifications/worker
Submits worker profile verification request (`WORKER` role required). `201 Created`.

### POST /api/verifications/contractor
Submits contractor profile verification request (`CONTRACTOR` role required). `201 Created`.

### POST /api/verifications/client
Submits client profile verification request (`CLIENT` role required). `201 Created`.

### GET /api/verifications/me
Returns all verification requests belonging to current user. `200 OK`.

---

## Admin Review Endpoints

### GET /api/admin/verifications
Lists verification requests. Filterable by optional query parameters `subjectType` (`USER`, `WORKER`, `CONTRACTOR`, `CLIENT`) and `status` (`PENDING`, `VERIFIED`, `REJECTED`). (`ADMIN` role required). `200 OK`.

### GET /api/admin/verifications/{verificationId}
Fetches single verification request. (`ADMIN` role required). `200 OK`.

### POST /api/admin/verifications/{verificationId}/verify
Approves verification request (`notes` optional). (`ADMIN` role required). `200 OK`.

### POST /api/admin/verifications/{verificationId}/reject
Rejects verification request (`notes` optional). (`ADMIN` role required). `200 OK`.
