# Team API

## Purpose
Manages structured worker teams, team lead assignments, and worker team memberships.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CONTRACTOR`

---

## Endpoints

### POST /api/teams

**Purpose**: Creates a new team owned by the contractor.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Request**:
```json
{
  "name": "Masonry Team Alpha",
  "specialization": "Masonry",
  "city": "Mumbai"
}
```

**Response**:
- Status: `201 Created`
- Body: Team response object.

---

### GET /api/teams

**Purpose**: Returns all teams owned by the authenticated contractor.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Array of Team response objects.

---

### GET /api/teams/{teamId}

**Purpose**: Fetches details for a specific team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team owner)

**Response**:
- Status: `200 OK`

---

### PUT /api/teams/{teamId}

**Purpose**: Updates team metadata or availability status.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team owner)

**Request**:
```json
{
  "name": "Masonry Team Alpha Updated",
  "specialization": "Masonry & Plaster",
  "city": "Navi Mumbai",
  "available": true
}
```

**Response**:
- Status: `200 OK`

---

### POST /api/teams/{teamId}/members

**Purpose**: Adds a worker to the team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team owner)

**Request**:
```json
{
  "workerProfileId": "123e4567-e89b-12d3-a456-426614174000",
  "roleInTeam": "MASON",
  "startsOn": "2026-09-01"
}
```

**Response**:
- Status: `201 Created`

---

### GET /api/teams/{teamId}/members

**Purpose**: Lists active members in a team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team owner)

**Response**:
- Status: `200 OK`

---

### POST /api/teams/{teamId}/members/{workerProfileId}/end

**Purpose**: Ends worker membership in a team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team owner)

**Query Parameters**:
- `startsOn` (ISO Date, required)
- `endsOn` (ISO Date, optional)

**Response**:
- Status: `200 OK`
