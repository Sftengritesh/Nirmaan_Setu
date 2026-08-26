# Booking + Fulfillment Architecture

## 1. Booking Purpose & Model Structure

A `booking` record connects a client's `workforce_requirement` to a specific workforce provider (`WORKER`, `TEAM`, or `CONTRACTOR`).
- **Unified Domain**: Uses the single `booking` table. Separate sub-tables are excluded.
- **Provider Types**:
  - `WORKER`: `provider_worker_profile_id` set; `quantity` must equal 1.
  - `TEAM`: `provider_team_id` set; `quantity` > 0. (Does not alter team members).
  - `CONTRACTOR`: `provider_contractor_profile_id` set; `quantity` > 0. (Does not alter contractor associations).

## 2. Client Creation & Requirement Eligibility

- **Creator Authorization**: Protected by `@PreAuthorize("hasRole('CLIENT')")`.
- **Ownership Verification**: Server verifies `AuthPrincipal` → `client_profile` → `project` → `requirement`.
- **Eligibility**: Bookings can **only** be created for requirements in `OPEN` status. Creation against `DRAFT`, `FULFILLED`, or `CANCELLED` requirements is rejected.
- **Initial Status**: `PENDING` (`requested_at = current_timestamp`, `responded_at = null`).

## 3. Provider Response & Authorization Rules

Provider acceptance (`POST /api/bookings/{bookingId}/accept`) and rejection (`POST /api/bookings/{bookingId}/reject`) require explicit provider ownership:
- **WORKER Provider**: Requires `WORKER` role AND `worker_profile.user_id == AuthPrincipal.userId()`.
- **TEAM Provider**: Requires `WORKER` or `CONTRACTOR` role AND `team.manager_user_id == AuthPrincipal.userId()`.
- **CONTRACTOR Provider**: Requires `CONTRACTOR` role AND `contractor_profile.user_id == AuthPrincipal.userId()`.

Role authorization alone is insufficient; responder must be the explicit owner/manager of the referenced provider entity.

## 4. Capacity Calculation & Concurrency Control

- **Capacity Rule**: `PENDING` bookings do not reserve capacity. Only `ACCEPTED` bookings count towards requirement fulfillment.
- **Pessimistic Write Locking**: To prevent concurrent overbooking, `acceptBooking` executes within a `@Transactional` block that acquires a JPA Pessimistic Write Lock (`SELECT ... FOR UPDATE`) on the parent `WorkforceRequirementEntity`.
- **Capacity Verification**:
  1. Calculate `SUM(quantity)` of all existing `ACCEPTED` bookings for the requirement.
  2. Verify `currently_accepted + booking.quantity <= requirement.quantity`.
  3. If exceeded, reject acceptance with an overbooking domain error.
- **Automatic Fulfillment**: If `currently_accepted + booking.quantity == requirement.quantity`, `workforce_requirement.status` automatically transitions from `OPEN` to `FULFILLED`. Clients cannot manually mark requirements as `FULFILLED`.

## 5. API Endpoints Summary

### Client Endpoints
- `POST /api/requirements/{requirementId}/bookings` — Create booking request for provider
- `GET /api/requirements/{requirementId}/bookings` — List bookings for requirement (client owned)
- `GET /api/bookings/{bookingId}` — Get booking details (client owned or provider owned)

### Provider Endpoints
- `GET /api/bookings/provider` — List incoming booking requests for authenticated provider
- `POST /api/bookings/{bookingId}/accept` — Accept booking request (provider owned)
- `POST /api/bookings/{bookingId}/reject` — Reject booking request (provider owned)
