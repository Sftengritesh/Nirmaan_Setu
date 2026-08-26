# Team API

## Purpose
Manages structured worker teams and worker team memberships.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CONTRACTOR`

---

## Endpoints

### POST /api/teams

**Purpose**: Creates a new team managed by the contractor user.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Request**:
```json
{
  "name": "Masonry Team Alpha",
  "description": "Specialized masonry squad for wall and flooring work."
}
```

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "managerUserId": "987e6543-e89b-12d3-a456-426614174000",
  "name": "Masonry Team Alpha",
  "description": "Specialized masonry squad for wall and flooring work.",
  "status": "ACTIVE",
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

---

### GET /api/teams

**Purpose**: Returns all teams managed by the authenticated contractor user.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: `List<TeamResponse>`

---

### GET /api/teams/{teamId}

**Purpose**: Fetches details for a specific team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team manager)

**Response**:
- Status: `200 OK`
- Body: TeamResponse object.

---

### PUT /api/teams/{teamId}

**Purpose**: Updates team metadata.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team manager)

**Request**:
```json
{
  "name": "Masonry Team Alpha Updated",
  "description": "Masonry and plastering team."
}
```

**Response**:
- Status: `200 OK`
- Body: TeamResponse object.

---

### POST /api/teams/{teamId}/members

**Purpose**: Adds a worker to the team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team manager)

**Request**:
```json
{
  "workerProfileId": "123e4567-e89b-12d3-a456-426614174000",
  "startsOn": "2026-09-01",
  "endsOn": null
}
```

**Response**:
- Status: `201 Created`
- Body: TeamMemberResponse object.

---

### GET /api/teams/{teamId}/members

**Purpose**: Lists all member records in a team.

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team manager)

**Response**:
- Status: `200 OK`
- Body: `List<TeamMemberResponse>`

---

### POST /api/teams/{teamId}/members/{workerProfileId}/end

**Purpose**: Ends worker membership in a team by setting an `endsOn` date (Non-destructive).

**Authentication**: Required

**Required Role**: `CONTRACTOR` (Must be team manager)

**Path Parameters**:
- `teamId` (UUID): Team ID.
- `workerProfileId` (UUID): Worker Profile ID.

**Query Parameters**:
- `startsOn` (ISO Date, required)
- `endsOn` (ISO Date, optional)

**Response**:
- Status: `200 OK`
- Body: Updated TeamMemberResponse object.
