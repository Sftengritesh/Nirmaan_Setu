# Worker API

## Purpose
Manages worker profiles, skill assignments, and worker availability statuses.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`WORKER`

---

## Endpoints

### POST /api/workers

**Purpose**: Creates a worker profile for the current user.

**Authentication**: Required

**Required Role**: `WORKER`

**Request**:
```json
{
  "fullName": "Ramesh Kumar",
  "dailyWageRate": 850.00,
  "experienceYears": 5,
  "locationCity": "Mumbai"
}
```

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e89b-12d3-a456-426614174000",
  "fullName": "Ramesh Kumar",
  "dailyWageRate": 850.00,
  "experienceYears": 5,
  "locationCity": "Mumbai",
  "available": true,
  "skills": []
}
```

**Possible Errors**:
- `400 Bad Request`: Validation failure.
- `409 Conflict`: Worker profile already exists for user.

---

### GET /api/workers/me

**Purpose**: Fetches current user's worker profile.

**Authentication**: Required

**Required Role**: `WORKER`

**Request Body**: None

**Response**:
- Status: `200 OK`
- Body: Worker profile object.

---

### PUT /api/workers/me

**Purpose**: Updates current user's worker profile attributes.

**Authentication**: Required

**Required Role**: `WORKER`

**Request**:
```json
{
  "fullName": "Ramesh Kumar",
  "dailyWageRate": 900.00,
  "experienceYears": 6,
  "locationCity": "Pune",
  "available": false
}
```

**Response**:
- Status: `200 OK`

---

### POST /api/workers/me/skills

**Purpose**: Associates a skill with current worker profile.

**Authentication**: Required

**Required Role**: `WORKER`

**Request**:
```json
{
  "skillId": "a1b2c3d4-e89b-12d3-a456-426614174000"
}
```

**Response**:
- Status: `200 OK`

---

### DELETE /api/workers/me/skills/{skillId}

**Purpose**: Removes a skill association from worker profile.

**Authentication**: Required

**Required Role**: `WORKER`

**Path Parameters**:
- `skillId` (UUID): ID of skill to disassociate.

**Request Body**: None

**Response**:
- Status: `200 OK`
