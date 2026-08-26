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
  "fullName": "Anil Sharma",
  "companyName": "Sharma Real Estate",
  "city": "Bengaluru"
}
```

**Response**:
- Status: `201 Created`
- Body: Client profile object.

---

### GET /api/clients/profile/me

**Purpose**: Fetches current user's client profile.

**Authentication**: Required

**Required Role**: `CLIENT`

**Response**:
- Status: `200 OK`

---

### PUT /api/clients/profile/me

**Purpose**: Updates current user's client profile.

**Authentication**: Required

**Required Role**: `CLIENT`

**Request**:
```json
{
  "fullName": "Anil Sharma",
  "companyName": "Sharma Builders & Developers",
  "city": "Bengaluru"
}
```

**Response**:
- Status: `200 OK`
