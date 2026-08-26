# Development Guide

## Technology direction

The intended stack is Java, Spring Boot, Maven, PostgreSQL, Next.js, and TypeScript. Do not add a dependency, build tool, or infrastructure service without a concrete requirement and a short decision record when the choice is significant.

## Working conventions

- Keep backend code grouped by domain module, not by one global controller/service/repository layer.
- Keep frontend code grouped by business feature; isolate reusable UI primitives and shared API infrastructure.
- Give each module one clear responsibility and keep dependencies acyclic.
- Prefer explicit, readable code over framework-heavy abstractions.
- Use DTOs at API boundaries; do not expose persistence entities as contracts.
- Treat migrations as code: version-controlled, forward-only, reviewed, and exercised on fresh and upgraded databases.
- Keep configuration outside source code. Provide documented example configuration only after actual configuration exists.

## Local development direction

When implementation starts, local setup should use a documented minimum path: frontend, backend, and PostgreSQL with non-production credentials. Docker is appropriate when it reduces setup variance, but it is not required before the application exists. Do not commit real credentials, production exports, or personal documents.

## Testing expectations

Write tests with the behavior they protect. Business rules receive unit tests; persistence and API boundaries receive integration tests; only critical cross-feature journeys need end-to-end coverage. Test independent workers and changing workforce associations as first-class cases.

## Documentation maintenance

Update `ARCHITECTURE.md` when a module boundary or deployment direction changes. Add an ADR under `docs/decisions/` for consequential, long-lived choices. Do not create ADRs for routine implementation details.
