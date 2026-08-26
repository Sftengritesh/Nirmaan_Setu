# Authentication API

## Purpose
Manages phone-based OTP authentication, session token issuance, current user identity resolution, and user logout.

## Authentication
- `POST /api/auth/otp/start`: Unauthenticated
- `POST /api/auth/otp/verify`: Unauthenticated
- `GET /api/auth/me`: Authenticated (Requires valid `Authorization: Bearer <sessionToken>`)
- `POST /api/auth/logout`: Authenticated (Requires valid `Authorization: Bearer <sessionToken>`)

## Roles
No specific role required. Users acquire system roles (`CLIENT`, `WORKER`, `CONTRACTOR`, `ADMIN`) upon verification based on their identity record. A user can hold multiple roles simultaneously.

---

## Endpoints

### POST /api/auth/otp/start

**Purpose**: Initiates OTP delivery for a given phone number.

**Authentication**: None

**Required Role**: None

**Path Parameters**: None

**Query Parameters**: None

**Request**:
```json
{
  "phoneNumber": "+919876543210"
}
```

**Response**:
- Status: `202 Accepted`
- Body: Empty

**Possible Errors**:
- `400 Bad Request`: `{"code": "INVALID_AUTH_REQUEST", "message": "Invalid authentication request."}`

---

### POST /api/auth/otp/verify

**Purpose**: Verifies an OTP challenge and returns an authentication session token alongside user identity and roles.

**Authentication**: None

**Required Role**: None

**Path Parameters**: None

**Query Parameters**: None

**Request**:
```json
{
  "phoneNumber": "+919876543210",
  "otp": "123456"
}
```

**Response**:
- Status: `200 OK`
- Body:
```json
{
  "sessionToken": "d3b07384d113edec49eaa6238ad5ff00",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "phoneNumber": "+919876543210",
  "roles": ["CLIENT", "WORKER"]
}
```

**Possible Errors**:
- `400 Bad Request`: `{"code": "INVALID_AUTH_REQUEST", "message": "Invalid authentication request."}`
- `401 Unauthorized`: `{"code": "AUTHENTICATION_FAILED", "message": "Invalid or expired OTP."}`

---

### GET /api/auth/me

**Purpose**: Fetches current authenticated user context and granted roles.

**Authentication**: Required (`Authorization: Bearer <sessionToken>`)

**Required Role**: None

**Path Parameters**: None

**Query Parameters**: None

**Request Body**: None

**Response**:
- Status: `200 OK`
- Body:
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "phoneNumber": "+919876543210",
  "roles": ["CLIENT", "WORKER"]
}
```

**Possible Errors**:
- `401 Unauthorized`: Header missing, invalid, or expired token.

---

### POST /api/auth/logout

**Purpose**: Invalidates the active user session token.

**Authentication**: Required (`Authorization: Bearer <sessionToken>`)

**Required Role**: None

**Path Parameters**: None

**Query Parameters**: None

**Request Body**: None

**Response**:
- Status: `204 No Content`
- Body: Empty

**Possible Errors**:
- `401 Unauthorized`: Header missing, invalid, or expired token.
