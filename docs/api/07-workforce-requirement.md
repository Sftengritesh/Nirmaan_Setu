# Workforce Requirement API

## Purpose
Manages client workforce requirements, capacity demands, daily rate offers, and requirement state transitions (`DRAFT` -> `OPEN` -> `FULFILLED`/`CANCELLED`).

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CLIENT`

---

## Lifecycle States

- `DRAFT`: Newly created requirement. Editable.
- `OPEN`: Published requirement visible in discovery. Bookings accepted.
- `FULFILLED`: Capacity demand fully met by accepted bookings. Automatically updated.
- `CANCELLED`: Client cancelled requirement. No further bookings allowed.

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
  "skillId": "a1b2c3d4-e89b-12d3-a456-426614174000",
  "requiredQuantity": 5,
  "dailyRateOffered": 900.00,
  "startDate": "2026-09-01",
  "endDate": "2026-09-15",
  "description": "Need experienced masons for wall construction."
}
```

**Response**:
- Status: `201 Created`

---

### GET /api/projects/{projectId}/requirements

**Purpose**: Lists all workforce requirements for a project.

**Authentication**: Required

**Required Role**: `CLIENT`

---

### GET /api/requirements/{requirementId}

**Purpose**: Fetches details for a specific workforce requirement.

**Authentication**: Required

**Required Role**: `CLIENT`

---

### PUT /api/requirements/{requirementId}

**Purpose**: Updates a `DRAFT` requirement parameters.

**Authentication**: Required

**Required Role**: `CLIENT`

---

### POST /api/requirements/{requirementId}/open

**Purpose**: Transitions requirement from `DRAFT` to `OPEN` status, making it discoverable.

**Authentication**: Required

**Required Role**: `CLIENT`

---

### POST /api/requirements/{requirementId}/cancel

**Purpose**: Cancels an `OPEN` or `DRAFT` requirement.

**Authentication**: Required

**Required Role**: `CLIENT`
