# Project API

## Purpose
Manages client construction project sites and locations.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CLIENT`

---

## Endpoints

### POST /api/projects

**Purpose**: Creates a construction project linked to the client profile.

**Authentication**: Required

**Required Role**: `CLIENT`

**Request**:
```json
{
  "name": "Green Valley Apartment Site A",
  "locationCity": "Bengaluru",
  "address": "123 Outer Ring Road, Bellandur"
}
```

**Response**:
- Status: `201 Created`
- Body: Project response object.

---

### GET /api/projects

**Purpose**: Returns all construction projects owned by the authenticated client.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: Array of Project response objects.

---

### GET /api/projects/{projectId}

**Purpose**: Fetches details for a specific project site.

**Authentication**: Required

**Required Role**: `CLIENT` (Must be project owner)

**Response**:
- Status: `200 OK`

---

### PUT /api/projects/{projectId}

**Purpose**: Updates project information.

**Authentication**: Required

**Required Role**: `CLIENT` (Must be project owner)

**Request**:
```json
{
  "name": "Green Valley Apartment Site A - Phase 2",
  "locationCity": "Bengaluru",
  "address": "123 Outer Ring Road, Bellandur"
}
```

**Response**:
- Status: `200 OK`
