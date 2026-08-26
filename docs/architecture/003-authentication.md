# Authentication Foundation

## Authentication flow

1. `POST /api/auth/otp/start` accepts an Indian phone number and creates a short-lived OTP challenge.
2. `POST /api/auth/otp/verify` validates the challenge. For a new phone identity it creates an `app_user` and `auth_phone_identity`; it does not create any worker, contractor, or client profile.
3. Successful verification creates an opaque bearer session and returns its token, expiry, user ID, and server-read roles.
4. `GET /api/auth/me` returns the authenticated identity and roles. `POST /api/auth/logout` revokes the current session.

Start responses do not reveal whether a phone already has an account. Failed verification uses a single safe error response.

## Phone normalization

The MVP accepts Indian mobile numbers only and stores them as E.164: `+91` followed by a valid ten-digit Indian mobile number. Inputs with spaces, parentheses, or hyphens are normalized before comparison. Phone identity is stored only in `auth_phone_identity`, never in role profiles.

## OTP boundary

`OtpDelivery` is the only delivery boundary. OTP values are generated with `SecureRandom`, BCrypt-hashed before persistence, expire after ten minutes, allow five attempts, and are single-use. Used and expired challenges are removed during future OTP requests after a one-day retention period. OTP values are never logged or included in API responses.

There is no SMS provider in this repository. The default delivery adapter safely reports authentication as unavailable. Unit tests provide an in-memory delivery double that does not send SMS and is available only in test code.

## Session and token strategy

The MVP uses an opaque bearer token. The client sends it in `Authorization: Bearer <token>`. Only its SHA-256 hash is persisted in `auth_session`; the server validates hash, expiry, revocation state, and the user's current `ACTIVE` account status on every request. Tokens expire after twelve hours. Logout marks the current session revoked. No refresh tokens or distributed session store are introduced.

## Roles and authorization

Roles are read from `user_role` and `app_role` for every authenticated principal. The client never supplies roles. Spring Security is stateless and protects all routes except the two OTP-start/verify endpoints. Method security is enabled; future feature endpoints can use `@PreAuthorize` with the `roleAuthorization` helper or standard `ROLE_CLIENT`, `ROLE_WORKER`, `ROLE_CONTRACTOR`, and `ROLE_ADMIN` authorities.

## Account status and security

Only `ACTIVE` users can establish or use a session. `DISABLED` users are rejected without a revealing account-state response. Authentication errors are normalized and do not expose OTP values, database errors, tokens, stack traces, or security configuration. CSRF is disabled because the API is stateless bearer-token based; browser clients must avoid storing tokens in locations exposed to untrusted scripts.

## Production SMS and rate limiting

A production SMS adapter must implement `OtpDelivery`, receive credentials from managed configuration, and be added only with provider failure handling and audit decisions. OTP rate limiting is not implemented: a future application or edge-layer limiter must be introduced without Redis unless distributed requirements demonstrate it is needed. The present per-challenge attempt limit is not a production-grade rate limiter.

## Database change

`V2__authentication_foundation.sql` adds authentication-owned phone identity, OTP challenge, and opaque session tables. It leaves V1 and every business-profile table unchanged. No password, payment, file, or external-provider data is stored.

## Known limitations

- No SMS delivery provider is configured, so production OTP initiation returns a safe unavailable response until one is added.
- PostgreSQL integration tests require a local or CI PostgreSQL instance with V1 and V2 migrations applied.
- Role assignment is intentionally not part of registration; later profile/admin workflows own which role a new user receives.
