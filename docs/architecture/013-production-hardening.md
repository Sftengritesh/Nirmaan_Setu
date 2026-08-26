# Production Hardening Architecture

## 1. Overview & Strategy

The Production Hardening phase establishes infrastructure configuration, environment variable management, database migration safety, health monitoring, API documentation, containerization, and automated CI pipelines for the NirmaanSetu backend.

## 2. Configuration & Environment Variables

All sensitive values and environment-specific parameters are driven by standard environment variables:

- `NIRMAANSETU_SERVER_PORT`: Server HTTP port (default `8080`).
- `NIRMAANSETU_DATABASE_URL`: JDBC connection URL.
- `NIRMAANSETU_DATABASE_USERNAME`: PostgreSQL user.
- `NIRMAANSETU_DATABASE_PASSWORD`: PostgreSQL password.
- `NIRMAANSETU_CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed CORS origins.
- `NIRMAANSETU_OTP_TTL`: OTP validity duration.
- `NIRMAANSETU_SESSION_TTL`: Session TTL duration.

## 3. Database Migration & Safety Rules

- Flyway manages migration execution (`V1__initial_mvp_schema.sql`, `V2__seed_test_users.sql`, `V3__create_notification_schema.sql`).
- `spring.flyway.clean-disabled=true` is enforced in production configuration to prevent destructive database clean operations.
- `spring.flyway.out-of-order=false` enforces strict migration sequence.

## 4. Containerization & Local Development

- `docker-compose.yml`: Launches isolated PostgreSQL 16 container for local development.
- `Dockerfile`: Multi-stage build (Temurin JDK 17 builder -> Temurin JRE 17 runner) with non-root user `appuser`.

## 5. Health Probes & API Documentation

- **Health Probes**: Spring Boot Actuator endpoint `/actuator/health`.
- **API Documentation**: Springdoc OpenAPI 3.0 UI accessible at `/swagger-ui.html` and spec at `/v3/api-docs`.

## 6. CI Pipeline

- `.github/workflows/ci.yml`: Runs automated builds and unit test suites on pull requests and pushes to `main`.
