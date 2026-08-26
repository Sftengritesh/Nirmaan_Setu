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

**Path Parameters**:
- `requirementId` (UUID): Workforce requirement ID.

**Request**:
```json
{
  "providerType": "WORKER",
  "providerWorkerProfileId": "123e4567-e89b-12d3-a456-426614174000",
  "providerTeamId": null,
  "providerContractorProfileId": null,
  "quantity": 1
}
```

*Supported `providerType` Enum Values*: `WORKER`, `TEAM`, `CONTRACTOR`. Note: Exactly one corresponding profile/team ID field should match `providerType`.

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "requirementId": "888e4567-e89b-12d3-a456-426614174000",
  "providerType": "WORKER",
  "providerWorkerProfileId": "123e4567-e89b-12d3-a456-426614174000",
  "providerTeamId": null,
  "providerContractorProfileId": null,
  "quantity": 1,
  "status": "REQUESTED",
  "requestedAt": "2026-08-26T10:00:00Z",
  "respondedAt": null,
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

---

### GET /api/requirements/{requirementId}/bookings

**Purpose**: Returns all bookings initiated for a specific requirement.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: `List<BookingResponse>`

---

### GET /api/bookings/{bookingId}

**Purpose**: Fetches details for a specific booking.

**Authentication**: Required

**Required Role**: `CLIENT`, `WORKER`, or `CONTRACTOR` (Must be booking participant)

**Response**:
- Status: `200 OK`
- Body: BookingResponse object.

---

### GET /api/bookings/provider

**Purpose**: Provider fetches all incoming booking requests directed to them or their managed teams/contractor profiles.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: `List<BookingResponse>`

---

### POST /api/bookings/{bookingId}/accept

**Purpose**: Service provider accepts incoming booking request. Performs pessimistic lock calculation on capacity. If accepted quantity meets requirement capacity, triggers requirement state transition to `FULFILLED`.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Updated BookingResponse object (`status: "ACCEPTED"`).

---

### POST /api/bookings/{bookingId}/reject

**Purpose**: Service provider rejects incoming booking request.

**Authentication**: Required

**Required Role**: `WORKER` or `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Updated BookingResponse object (`status: "REJECTED"`).
