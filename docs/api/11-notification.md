# Notification API

## Purpose
Manages user notifications, unread count polling, and read state transitions.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Status Enums
- `UNREAD`
- `READ`

---

## Endpoints

### GET /api/notifications

**Purpose**: Fetches paginated notifications for the authenticated user (`status` optional query param `UNREAD` / `READ`).

**Authentication**: Required

**Response**:
- Status: `200 OK`
- Body: `Page<NotificationResponse>` (Spring Data Page format, max size 50)

---

### GET /api/notifications/unread-count

**Purpose**: Returns total unread notification count.

**Authentication**: Required

**Response**:
- Status: `200 OK`
- Body: `{"unreadCount": 3}`

---

### POST /api/notifications/{id}/read

**Purpose**: Marks a specific notification as `READ`.

**Authentication**: Required

**Response**:
- Status: `200 OK`
- Body: NotificationResponse object (`status: "READ"`).

---

### POST /api/notifications/read-all

**Purpose**: Marks all unread notifications for current user as `READ`.

**Authentication**: Required

**Response**:
- Status: `200 OK`
- Body: `{"markedReadCount": 5}`
