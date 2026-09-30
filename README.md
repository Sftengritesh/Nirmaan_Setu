# NirmaanSetu

**India-first construction workforce platform** connecting skilled tradespeople (Raj Mistri, Carpenter, Electrician, Plumber, Painter, Tile Worker, Labour) with contractors and clients.

---

## 1. Project Overview

NirmaanSetu is a full-stack application with:

- **Backend**: Spring Boot modular monolith (Java 17, PostgreSQL, Flyway)
- **Frontend**: React + TypeScript (Vite, Stitch design system)

A user can hold multiple roles simultaneously (`WORKER`, `CONTRACTOR`, `CLIENT`, `ADMIN`). Role assignments are authoritative from the server — the client cannot select or inject roles.

---

## 2. Features

- Phone + OTP authentication (no password, no SMS provider required in dev)
- Session tokens (opaque, SHA-256 hashed in database)
- Role-based authorization at every endpoint (`@PreAuthorize`)
- Worker profiles, normalized skill associations, availability status
- Contractor profiles and worker roster management
- Team creation and team manager authorization
- Client profiles
- Client project management
- Workforce requirements (DRAFT → OPEN → FULFILLED / CANCELLED)
- Booking/fulfillment with overbooking protection (pessimistic write lock)
- Worker/Contractor/Client identity verification (PENDING → VERIFIED / REJECTED)
- Discovery/marketplace: search workers, contractors, teams, open requirements
- In-process notifications via `@TransactionalEventListener`
- OpenAPI 3.1 documentation (Swagger UI)
- Spring Actuator health endpoint
- Configurable CORS

---

## 3. Architecture

```
Modular monolith — 11 domain modules
───────────────────────────────────────
auth          Phone-OTP login, sessions, security filter
worker        Worker profiles + skills + availability
contractor    Contractor profiles + worker rosters
team          Teams + team member management
client        Client profiles
project       Client construction projects
requirement   Workforce requirements (DRAFT/OPEN/FULFILLED/CANCELLED)
booking       Booking/fulfillment with overbooking lock
verification  Identity verification (admin workflow)
discovery     Read-only marketplace search
notification  Transactional event listeners + notification CRUD
───────────────────────────────────────
Each module has: domain/ | application/ | infrastructure/ | api/
```

Authorization rules:
- `userId` is always sourced from the authenticated `AuthPrincipal` — never from request bodies
- Ownership is enforced at the service layer per module
- Security exceptions propagate to the Spring Security filter chain (401/403 JSON)

---

## 4. Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.0 |
| Security | Spring Security (stateless, opaque session tokens) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL 17 |
| Migrations | Flyway (V1 → V2 → V3) |
| Build | Maven |
| API Docs | Springdoc OpenAPI 2.8.5 |
| Monitoring | Spring Actuator |
| Frontend | React 18, TypeScript 5, Vite 5 |
| UI | Stitch design system (custom) |

---

## 5. Repository Structure

```
NirmaanSetu/
├── db/migration/
│   ├── V1__initial_mvp_schema.sql       — All domain tables
│   ├── V2__authentication_foundation.sql — Auth session + phone identity tables
│   └── V3__create_notification_schema.sql
├── docs/
│   ├── api/                              — API contract documentation
│   └── architecture/                     — Per-module architecture docs
├── frontend/
│   ├── src/
│   │   ├── pages/                        — One directory per domain
│   │   ├── services/                     — Backend API client per domain
│   │   └── components/                   — Shared layout components
│   └── vite.config.ts
├── src/main/java/com/nirmaansetu/
│   ├── auth/
│   ├── worker/
│   ├── contractor/
│   ├── team/
│   ├── client/
│   ├── project/
│   ├── requirement/
│   ├── booking/
│   ├── verification/
│   ├── discovery/
│   └── notification/
└── src/test/java/com/nirmaansetu/       — 116 unit tests (Mockito, no DB required)
```

---

## 6. Backend Setup

### Prerequisites

- JDK 17
- Maven 3.8+
- PostgreSQL 14+ running locally

### Create database and user

```sql
CREATE USER nirmaansetu WITH PASSWORD 'nirmaansetu_dev_pass';
CREATE DATABASE nirmaansetu OWNER nirmaansetu;
```

### Run

```bash
mvn spring-boot:run
```

Flyway will automatically apply V1 → V2 → V3 on first startup.

---

## 7. Frontend Setup

```bash
cd frontend
npm install
npm run dev    # Development server on http://localhost:3000
npm run build  # Production build → frontend/dist/
```

The Vite dev server proxies `/api/*` to `http://localhost:8080` — no CORS configuration needed in development.

---

## 8. Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NIRMAANSETU_SERVER_PORT` | HTTP port | `8080` |
| `NIRMAANSETU_DATABASE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/nirmaansetu` |
| `NIRMAANSETU_DATABASE_USERNAME` | DB user | `nirmaansetu` |
| `NIRMAANSETU_DATABASE_PASSWORD` | DB password | `nirmaansetu_dev_pass` |
| `NIRMAANSETU_OTP_TTL` | OTP expiry | `PT10M` |
| `NIRMAANSETU_OTP_DEV_MODE` | Print OTP to logs (dev only) | `true` |
| `NIRMAANSETU_SESSION_TTL` | Session expiry | `PT12H` |
| `NIRMAANSETU_CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000,http://localhost:3001,http://localhost:5173` |

> **Production**: Set `NIRMAANSETU_OTP_DEV_MODE=false`. Without a real SMS provider wired in, OTP delivery will throw `OtpDeliveryUnavailableException`. Integrate a real `OtpDelivery` bean before enabling production traffic.

---

## 9. Database Migrations

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__initial_mvp_schema.sql` | All domain tables (worker_profile, skill, contractor_profile, team, client_profile, project, workforce_requirement, booking, verification, app_role, app_user, user_role, phone_identity) |
| V2 | `V2__authentication_foundation.sql` | Auth session table, OTP challenge table |
| V3 | `V3__create_notification_schema.sql` | Notification table |

Flyway's `clean-disabled: true` prevents accidental database wipes.

---

## 10. Authentication Flow

```
1. POST /api/auth/otp/start    { phoneNumber: "+91XXXXXXXXXX" }
   → Normalizes phone, creates OTP challenge, delivers code (dev: logs to console)
   → 200 OK (no body)

2. POST /api/auth/otp/verify   { phoneNumber: "+91XXXXXXXXXX", otp: "123456" }
   → Verifies code, creates/retrieves app_user, creates opaque session token
   → 200 { userId, roles, accessToken, expiresAt }

3. GET  /api/auth/me
   Authorization: Bearer <accessToken>
   → 200 { userId, roles }

4. POST /api/auth/logout
   Authorization: Bearer <accessToken>
   → Revokes session token, 200
```

OTP attempt limit: 5. After 5 failed attempts, the challenge is exhausted and cannot be retried.

---

## 11. Roles

| Role | Description |
|------|-------------|
| `CLIENT` | Can create client profiles, projects, workforce requirements |
| `WORKER` | Can create/update worker profiles and skills |
| `CONTRACTOR` | Can create contractor profiles, manage teams and worker rosters |
| `ADMIN` | Can manage identity verifications |

A user can hold multiple roles simultaneously. Roles are assigned by the backend; the client cannot modify them.

---

## 12. API Documentation

When the backend is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health**: http://localhost:8080/actuator/health

46 endpoints across 11 domain modules are documented.

---

## 13. Testing

```bash
# Run all unit tests (no PostgreSQL required)
mvn clean test
```

**Results (verified)**:
```
Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests |
|-----------|-------|
| AuthServiceTest | 12 |
| PhoneNumberNormalizerTest | 2 |
| RoleAuthorizationTest | 1 |
| BookingServiceTest | 11 |
| ClientServiceTest | 7 |
| ContractorServiceTest | 12 |
| DiscoveryServiceTest | 6 |
| NotificationServiceTest | 9 |
| ProjectServiceTest | 8 |
| WorkforceRequirementServiceTest | 11 |
| TeamServiceTest | 11 |
| VerificationServiceTest | 11 |
| WorkerServiceTest | 15 |

PostgreSQL integration tests: **BLOCKED** (require a live PostgreSQL instance; no automated DB provisioning in CI).

```bash
# Frontend production build
cd frontend && npm run build
# ✓ Built in ~5s, 206 kB JS / 8.7 kB CSS
```

---

## 14. Deployment Notes

### Backend Dockerfile

A Dockerfile is present in the repository root. Build and run:

```bash
docker build -t nirmaansetu-backend:latest .
docker run -p 8080:8080 \
  -e NIRMAANSETU_DATABASE_URL=jdbc:postgresql://db-host:5432/nirmaansetu \
  -e NIRMAANSETU_DATABASE_USERNAME=nirmaansetu \
  -e NIRMAANSETU_DATABASE_PASSWORD=<secure_password> \
  -e NIRMAANSETU_OTP_DEV_MODE=false \
  -e NIRMAANSETU_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com \
  nirmaansetu-backend:latest
```

### Production Checklist

- [ ] Set `NIRMAANSETU_OTP_DEV_MODE=false`
- [ ] Wire a real `OtpDelivery` bean (e.g. Twilio, MSG91)
- [ ] Set strong `NIRMAANSETU_DATABASE_PASSWORD`
- [ ] Restrict `NIRMAANSETU_CORS_ALLOWED_ORIGINS` to production frontend URL
- [ ] Run behind a reverse proxy (nginx/Caddy) with TLS
- [ ] Enable request rate limiting on OTP endpoints at the proxy/WAF layer

---

## 15. Known Limitations

- **No SMS provider**: OTP delivery is console-only in dev. Production requires a real SMS provider bean.
- **No distributed rate limiting**: OTP start is rate-limited at the DB/application level only. Production must add request rate limiting (nginx rate_limit, WAF, or API gateway) before enabling live SMS.
- **Worker search not implemented**: Workers can only view their own profile (`GET /api/workers/me`). Public marketplace uses the discovery endpoints.
- **Verification is admin-only**: Worker/contractor verification requires ADMIN action via `/api/admin/verifications`.
- **No email/push notifications**: Notifications are in-database only; no email or push delivery is implemented.
- **Docker not verified locally**: Docker availability is not guaranteed in the current environment. The Dockerfile is present but was not smoke-tested.
