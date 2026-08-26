# Contributing to NirmaanSetu

## Before starting

Read `ARCHITECTURE.md` and identify the owning module. Keep changes within that boundary where possible. If a change requires two modules, make the dependency explicit and avoid reaching into another module's internals.

## Change standards

- Keep pull requests focused and explain the user-facing or operational reason for the change.
- Include tests that demonstrate changed behavior, proportional to the risk.
- Validate input and enforce authorization in the feature that owns the action.
- Do not introduce generic base classes, cross-module table access, or unreviewed third-party dependencies for convenience.
- Do not commit secrets, customer data, identity documents, generated build output, or external images without approval and provenance.
- Update architecture documentation or an ADR when the change alters a durable system decision.

## Review checklist

- The change has a clear module owner and no new circular dependency.
- Public API contracts are explicit and do not expose persistence entities.
- Data ownership, authorization, error handling, and migration impact have been considered.
- Logs and tests do not leak sensitive data.
- Independent workers and time-varying team or contractor associations remain supported.
