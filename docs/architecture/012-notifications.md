# Notifications Architecture

## 1. Overview & Purpose

The `notification` domain provides synchronous in-process notification persistence and user notification retrieval for the NirmaanSetu platform.

It listens to application domain events published by core services (`BookingService`, `VerificationService`, `WorkforceRequirementService`) via `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to ensure notifications are only generated after the originating transaction successfully commits.

## 2. Event Ownership & Shared Events

Events are stored in the shared application event package `com.nirmaansetu.events`:
- `BookingCreatedEvent`
- `BookingStatusChangedEvent`
- `VerificationReviewedEvent`
- `RequirementFulfilledEvent`

`NotificationEventListener` acts strictly as an event consumer.

## 3. Event & Recipient Resolution Matrix

| Domain Event | Triggering Source | Recipient Resolution Path |
|---|---|---|
| `BookingCreatedEvent` | Client requests workforce | Provider User ID (`worker_profile.user_id`, `contractor_profile.user_id`, or `team.manager_user_id`) |
| `BookingStatusChangedEvent` (ACCEPTED / REJECTED) | Provider responds to booking | Client User ID (`requirement -> project -> client_profile.user_id`) |
| `VerificationReviewedEvent` (VERIFIED / REJECTED) | Admin reviews subject identity | Subject User ID (`subject_user_id` or profile `user_id`) |
| `RequirementFulfilledEvent` | Requirement capacity auto-fulfilled | Client User ID (`requirement -> project -> client_profile.user_id`) |

## 4. Data Model & Database Migration

- **Flyway Migration**: `db/migration/V3__create_notification_schema.sql` creates the `notification` table.
- **Entity**: `NotificationEntity` (`id`, `recipientUserId`, `type`, `title`, `message`, `referenceEntityType`, `referenceEntityId`, `status`, `readAt`, `createdAt`, `updatedAt`).
- **Lifecycle Statuses**:
  - `UNREAD`: Initial state upon notification creation.
  - `READ`: State when marked read by recipient user.

## 5. API Surface & Security

- `GET /api/notifications`: Paginated list of notifications for `AuthPrincipal.userId()` (max size 50).
- `GET /api/notifications/unread-count`: Unread count for `AuthPrincipal.userId()`.
- `POST /api/notifications/{id}/read`: Marks notification `READ` if owned by `AuthPrincipal.userId()`.
- `POST /api/notifications/read-all`: Marks all `UNREAD` notifications for `AuthPrincipal.userId()` as `READ`.

All operations verify `recipientUserId == AuthPrincipal.userId()`.
