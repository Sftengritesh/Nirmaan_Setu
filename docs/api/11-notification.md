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
Fetches paginated notifications for the authenticated user (`status` optional query param `UNREAD` / `READ`). `200 OK`.

### GET /api/notifications/unread-count
Returns total unread notification count `{"unreadCount": 3}`. `200 OK`.

### POST /api/notifications/{id}/read
Marks a specific notification as `READ`. `200 OK`.

### POST /api/notifications/read-all
Marks all unread notifications for current user as `READ` `{"updatedCount": 5}`. `200 OK`.
