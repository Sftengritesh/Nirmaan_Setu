# Client API

## Purpose
Manages client profiles and business/individual details.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Roles
`CLIENT`

---

## Endpoints

### POST /api/clients/profile

**Purpose**: Creates a client profile for the authenticated user.

**Authentication**: Required

**Required Role**: `CLIENT`

**Request**:
```json
{
  "clientType": "BUILDER",
  "displayName": "Sharma Builders & Developers"
}
```

*Supported `clientType` Enum Values*: `HOMEOWNER`, `BUILDER`, `BUSINESS`, `OTHER`.

**Response**:
- Status: `201 Created`
- Body:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e89b-12d3-a456-426614174000",
  "clientType": "BUILDER",
  "displayName": "Sharma Builders & Developers",
  "createdAt": "2026-08-26T10:00:00Z",
  "updatedAt": "2026-08-26T10:00:00Z"
}
```

---

### GET /api/clients/profile/me

**Purpose**: Fetches current user's client profile.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`
- Body: Client profile response object.

---

### PUT /api/clients/profile/me

**Purpose**: Updates current user's client profile.

**Authentication**: Required

**Required Role**: `CLIENT`

**Request**:
```json
{
  "clientType": "BUILDER",
  "displayName": "Sharma Builders Pvt Ltd"
}
```

**Response**:
- Status: `200 OK`
- Body: Updated client profile response object.
