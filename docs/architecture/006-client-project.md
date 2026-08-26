# Client Profile and Project Architecture

## 1. Client Profile Ownership & Role Boundaries

A `client_profile` record represents a construction client (e.g., homeowner, builder, business) on NirmaanSetu.
- **Ownership**: Each `client_profile` belongs to exactly one `app_user` (`user_id` column has a `UNIQUE` constraint in the V1 schema). The application derives `user_id` strictly from the authenticated `AuthPrincipal`; it is never accepted from request parameters.
- **Role Requirement**: All client and project endpoints are protected by `@PreAuthorize("hasRole('CLIENT')")`.
- **Multi-Role Compatibility**: Users holding multiple roles (e.g., `CLIENT + CONTRACTOR` or `CLIENT + WORKER`) can perform client operations without role mutation. The system never automatically assigns or modifies user roles (`app_role` / `user_role`).
- **Client Type Vocabulary**: Strictly adheres to the V1 schema check constraint: `HOMEOWNER`, `BUILDER`, `BUSINESS`, `OTHER`.

## 2. Project Domain & Ownership Strategy

A `project` represents a physical construction job site or work location belonging to a client.
- **Authoritative Ownership**:
  `AuthPrincipal` → `app_user.id` → `client_profile.id` → `project.client_profile_id`.
  Project creation automatically links the project to the authenticated user's resolved `client_profile`. Untrusted `clientProfileId` from request bodies is ignored.
- **Cross-Client Access Prevention**: Project queries (`getProjectById`, `updateProject`) strictly verify that `project.client_profile_id` matches the authenticated client's profile ID. Attempting to access another client's project returns an error. `CONTRACTOR` or `WORKER` roles do not grant access to client projects in this module.
- **Project Status Vocabulary**: Strictly adheres to V1 schema check constraint: `DRAFT`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
  - New projects default to `DRAFT`.
  - Project deactivation/cancellation is performed via status transition to `CANCELLED`. Physical row deletion (`DELETE`) is excluded to maintain historical integrity for future modules.
- **Location Representation**: Simple descriptive text string (`location VARCHAR(255)`). No geospatial coordinates, PostGIS, or map integrations are included.

## 3. Endpoints & Scope Summary

### Client Profile Endpoints
- `POST /api/clients/profile` — Create current user's client profile
- `GET /api/clients/profile/me` — Retrieve current user's client profile
- `PUT /api/clients/profile/me` — Update current user's client profile

### Project Endpoints
- `POST /api/projects` — Create project for current client (defaults status to `DRAFT`)
- `GET /api/projects` — List projects managed by current client
- `GET /api/projects/{projectId}` — Get project by ID (ownership check)
- `PUT /api/projects/{projectId}` — Update project by ID (ownership check)

## 4. Scope Exclusions & Bounded Monolith Boundaries

- **Workforce Requirements**: Excluded from this module. Projects will serve as aggregate roots for `workforce_requirement` in future modules.
- **Workforce Matching & Booking**: Excluded.
- **Payment & Messaging**: Excluded.
- **Physical Row Deletion**: Excluded for projects.
