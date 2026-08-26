# Workforce Requirement API

## Purpose
Manages client workforce requirements, capacity demands, daily rate or budget offers, and requirement state transitions (`DRAFT` -> `OPEN` -> `FULFILLED`/`CANCELLED`).

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CLIENT`

---

## Lifecycle States

- `DRAFT`: Newly created requirement. Editable.
- `OPEN`: Published requirement visible in discovery. Bookings accepted.
- `FULFILLED`: Capacity demand fully met by accepted bookings. Automatically updated by system.
- `CANCELLED`: Client cancelled requirement. No further bookings allowed.

---

## Compensation Rule Constraint

Application validation enforces that either `dailyRate` OR `budgetAmount` must be supplied (XOR constraint). Both cannot be null, and both cannot be set simultaneously.

---

## Endpoints

### POST /api/projects/{projectId}/requirements

**Purpose**: Creates a new workforce requirement draft for a project.

**Authentication**: Required

**Required Role**: `CLIENT`

**Path Parameters**:
- `projectId` (UUID): Project ID

**Request**:
```json
{
  "location": "Bellandur, Bengaluru",
  "startDate": "2026-09-01",
  "durationDays": 14,
  "workerType": "SKILLED_WORKER",
  "skillId": "a1b2c3d4-e89b-12d3-a456-426614174000",
  "quantity": 5,
  "dailyRate": 900.00,
  "budgetAmount": null,
  "currencyCode": "INR",
  "accommodationAvailable": true,
  "foodAvailable": false,
  "additionalNotes": "Need experienced masons for structural wall construction."
}
```

*Supported `workerType` Enum Values*: `SKILLED_WORKER`, `LABOUR`.

**Response**:
- Status: `201 Created`
- Body: WorkforceRequirementResponse object.

---

### GET /api/projects/{projectId}/requirements

**Purpose**: Lists all workforce requirements for a project.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: `List<WorkforceRequirementResponse>`

---

### GET /api/requirements/{requirementId}

**Purpose**: Fetches details for a specific workforce requirement.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: WorkforceRequirementResponse object.

---

### PUT /api/requirements/{requirementId}

**Purpose**: Updates a `DRAFT` requirement's parameters.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: WorkforceRequirementResponse object.

---

### POST /api/requirements/{requirementId}/open

**Purpose**: Transitions requirement status from `DRAFT` to `OPEN`, publishing it to the discovery marketplace.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: WorkforceRequirementResponse object (`status: "OPEN"`).

---

### POST /api/requirements/{requirementId}/cancel

**Purpose**: Cancels an `OPEN` or `DRAFT` requirement.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: WorkforceRequirementResponse object (`status: "CANCELLED"`).
