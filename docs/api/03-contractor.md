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
  "displayName": "Apex Infrastructure",
  "location": "Delhi",
  "description": "General civil and structural works contractor."
}
```

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e89b-12d3-a456-426614174000",
  "displayName": "Apex Infrastructure",
  "description": "General civil and structural works contractor.",
  "location": "Delhi",
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

---

### GET /api/contractors/profile/me

**Purpose**: Fetches current contractor's profile.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: Contractor profile response object.

---

### PUT /api/contractors/profile/me

**Purpose**: Updates current contractor's profile parameters.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Request**:
```json
{
  "displayName": "Apex Infrastructure Pvt Ltd",
  "location": "Noida",
  "description": "Specialized in large scale commercial construction."
}
```

**Response**:
- Status: `200 OK`
- Body: Updated contractor profile response object.

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
- Body: ContractorWorkerResponse object.

---

### GET /api/contractors/workers

**Purpose**: Returns all worker profiles associated with current contractor.

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Response**:
- Status: `200 OK`
- Body: `List<ContractorWorkerResponse>`

---

### POST /api/contractors/workers/{workerProfileId}/end

**Purpose**: Terminates an active worker-contractor association by setting an `endsOn` date (Non-destructive).

**Authentication**: Required

**Required Role**: `CONTRACTOR`

**Path Parameters**:
- `workerProfileId` (UUID): Associated worker profile ID.

**Query Parameters**:
- `startsOn` (ISO Date, required): Association start date matching active association.
- `endsOn` (ISO Date, optional): End date.

**Response**:
- Status: `200 OK`
- Body: Updated ContractorWorkerResponse object.
