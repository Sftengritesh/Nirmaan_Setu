# Booking API

## Purpose
Handles workforce booking proposals from clients to service providers (individual workers, teams, or contractors) and provider accept/reject responses.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CLIENT`, `WORKER`, `CONTRACTOR`

---

## Status Lifecycle

- `REQUESTED`: Client initiated booking proposal.
- `ACCEPTED`: Provider accepted booking proposal. Triggers pessimistic capacity check on requirement.
- `REJECTED`: Provider rejected booking proposal.

---

## Endpoints

### POST /api/requirements/{requirementId}/bookings

**Purpose**: Client creates a booking request directed to a specific provider.

**Authentication**: Required

**Required Role**: `CLIENT`

**Request**:
```json
{
  "providerType": "WORKER", // WORKER, TEAM, CONTRACTOR
  "providerWorkerProfileId": "123e4567-e89b-12d3-a456-426614174000",
  "providerTeamId": null,
  "providerContractorProfileId": null,
  "quantity": 1,
  "dailyRate": 900.00,
  "startDate": "2026-09-01",
  "endDate": "2026-09-15"
}
```

**Response**:
- Status: `201 Created`
- Body: Booking response object.

---

### GET /api/requirements/{requirementId}/bookings

**Purpose**: Returns all bookings initiated for a specific requirement.

**Authentication**: Required

**Required Role**: `CLIENT`

---

### GET /api/bookings/{bookingId}

**Purpose**: Fetches details for a specific booking.

**Authentication**: Required

**Required Role**: `CLIENT`, `WORKER`, or `CONTRACTOR` (Must be booking participant)

---

### GET /api/bookings/provider

**Purpose**: Provider fetches all incoming booking requests directed to them or their managed teams/contractor profiles.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

---

### POST /api/bookings/{bookingId}/accept

**Purpose**: Service provider accepts incoming booking request. Performs pessimistic lock calculation on capacity. If accepted quantity meets requirement capacity, triggers `RequirementFulfilledEvent`.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Updated booking response (`status: "ACCEPTED"`).

---

### POST /api/bookings/{bookingId}/reject

**Purpose**: Service provider rejects incoming booking request.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Updated booking response (`status: "REJECTED"`).
