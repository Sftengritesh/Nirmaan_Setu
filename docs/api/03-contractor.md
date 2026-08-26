# Contractor API

## Purpose
Manages contractor profiles, business metadata, and worker-contractor labor associations.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CONTRACTOR`

---

## Endpoints

### POST /api/contractors/profile

**Purpose**: Creates a contractor profile for the current user.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Request**:
```json
{
  "companyName": "Apex Infrastructure",
  "gstNumber": "27AAAAA0000A1Z5",
  "experienceYears": 10,
  "operatingCity": "Delhi"
}
```

**Response**:
- Status: `21 Created`
- Body: Contractor profile object.

---

### GET /api/contractors/profile/me

**Purpose**: Fetches current contractor's profile.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`

---

### PUT /api/contractors/profile/me

**Purpose**: Updates current contractor's profile parameters.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`

---

### POST /api/contractors/workers

**Purpose**: Associates a worker profile with current contractor.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

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

---

### GET /api/contractors/workers

**Purpose**: Returns all worker profiles associated with current contractor.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Array of contractor worker associations.

---

### POST /api/contractors/workers/{workerProfileId}/end

**Purpose**: Terminates an active worker-contractor association.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Path Parameters**:
- `workerProfileId` (UUID): Associated worker profile ID.

**Query Parameters**:
- `startsOn` (ISO Date, required): Association start date.
- `endsOn` (ISO Date, optional): End date.

**Response**:
- Status: `200 OK`
