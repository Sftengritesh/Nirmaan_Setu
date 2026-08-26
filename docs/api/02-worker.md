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
  "displayName": "Ramesh Kumar",
  "location": "Mumbai",
  "availabilityStatus": "AVAILABLE",
  "experienceYears": 5,
  "dailyRate": 850.00,
  "profileDescription": "Experienced mason specializing in residential projects.",
  "isTravelWilling": true
}
```

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e89b-12d3-a456-426614174000",
  "displayName": "Ramesh Kumar",
  "experienceYears": 5,
  "location": "Mumbai",
  "availabilityStatus": "AVAILABLE",
  "dailyRate": 850.00,
  "profileDescription": "Experienced mason specializing in residential projects.",
  "isTravelWilling": true,
  "skills": [],
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

**Possible Errors**:
- `400 Bad Request`: `{"code": "INVALID_REQUEST", "message": "Invalid request data."}`
- `422 Unprocessable Entity`: `{"code": "WORKER_PROFILE_ERROR", "message": "Worker profile already exists for user"}`

---

### GET /api/workers/me

**Purpose**: Fetches current user's worker profile.

**Authentication**: Required

**Required Role**: `WORKER`

**Request Body**: None

**Response**:
- Status: `200 OK`
- Body: Worker profile response object.

---

### PUT /api/workers/me

**Purpose**: Updates current user's worker profile attributes.

**Authentication**: Required

**Required Role**: `WORKER`

**Request**:
```json
{
  "displayName": "Ramesh Kumar",
  "location": "Pune",
  "availabilityStatus": "BUSY",
  "experienceYears": 6,
  "dailyRate": 900.00,
  "profileDescription": "Senior mason specializing in commercial and residential projects.",
  "isTravelWilling": false
}
```

**Response**:
- Status: `200 OK`
- Body: Updated Worker profile response object.

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
- Body: Updated Worker profile response object.

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
- Body: Updated Worker profile response object.
