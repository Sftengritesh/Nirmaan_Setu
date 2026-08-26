# ADR 001: Start With a Modular Monolith

## Status

Accepted

## Context

NirmaanSetu launches through an existing local contractor and workforce network. The first team needs to learn the product workflows, evolve requirements quickly, and operate the system with limited infrastructure overhead. The domain has meaningful boundaries, but no demonstrated need for independent deployment or distributed messaging.

## Decision

Build one Spring Boot backend as a modular monolith with clearly owned domain modules, one PostgreSQL database, and explicit module interfaces. Use REST for the frontend boundary. Keep frontend features organized by business capability. Preserve separate user identity, multi-role assignment, role-specific profiles, and time-varying workforce business associations. Use the MVP workflow in which a client creates a requirement and an individual worker, team, or contractor accepts or rejects it; acceptance creates a booking. Payments remain off-platform and verification may be manual. Do not introduce microservices, Kafka, Redis, Kubernetes, event infrastructure, payment gateways, wallets, or escrow at this stage.

## Consequences

This choice reduces deployment, debugging, local-development, and data-consistency complexity while retaining internal boundaries for future extraction. The team must actively prevent a "big ball of mud" by preserving module ownership, avoiding circular dependencies, and prohibiting direct cross-module table access.

If a module later needs independent scaling, team ownership, release cadence, or availability isolation, document an extraction ADR before splitting it. The extraction plan must define contract ownership, data migration, operational support, monitoring, and failure behavior.
