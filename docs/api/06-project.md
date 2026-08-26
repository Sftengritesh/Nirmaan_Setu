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
  "title": "Green Valley Apartment Site A",
  "description": "Multi-story residential apartment construction site.",
  "location": "123 Outer Ring Road, Bellandur, Bengaluru",
  "startDate": "2026-09-01"
}
```

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "clientProfileId": "987e6543-e89b-12d3-a456-426614174000",
  "title": "Green Valley Apartment Site A",
  "description": "Multi-story residential apartment construction site.",
  "location": "123 Outer Ring Road, Bellandur, Bengaluru",
  "status": "ACTIVE",
  "startDate": "2026-09-01",
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

---

### GET /api/projects

**Purpose**: Returns all construction projects owned by the authenticated client.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: `List<ProjectResponse>`

---

### GET /api/projects/{projectId}

**Purpose**: Fetches details for a specific project site.

**Authentication**: Required

**Required Role**: `CLIENT` (Must be project owner)

**Response**:
- Status: `200 OK`
- Body: ProjectResponse object.

---

### PUT /api/projects/{projectId}

**Purpose**: Updates project information.

**Authentication**: Required

**Required Role**: `CLIENT` (Must be project owner)

**Request**:
```json
{
  "title": "Green Valley Apartment Site A - Phase 2",
  "description": "Updated phase 2 scope.",
  "location": "123 Outer Ring Road, Bellandur, Bengaluru",
  "startDate": "2026-09-15"
}
```

**Response**:
- Status: `200 OK`
- Body: ProjectResponse object.
