# Search & Discovery Architecture

## 1. Purpose

The `discovery` domain provides read-only search capabilities allowing:
- **Clients**: To search for available workers, contractors, and active teams.
- **Workforce Providers (Workers, Contractors, Team Managers)**: To search for open workforce requirements posted by clients.

## 2. Search Domains & Visibility Rules

| Entity | Primary Filters | Default Visibility Constraints |
|---|---|---|
| **Workers** | `location`, `skillId`, `availability`, `minExperience`, `maxDailyRate`, `isTravelWilling`, `isVerified` | `app_user.account_status = ACTIVE`, default `availability = AVAILABLE` |
| **Contractors** | `location`, `displayName`, `isVerified` | `app_user.account_status = ACTIVE` |
| **Teams** | `location`, `name` | `team.status = ACTIVE` (returns header metadata only, no private member profile data) |
| **Requirements** | `location`, `workerType`, `skillId`, `minDailyRate`, `maxDailyRate`, `accommodationAvailable`, `foodAvailable` | **ONLY** `status = OPEN` |

## 3. Public Data Model & Security Boundaries

Response DTOs strip all sensitive PII and authentication data:
- **Excluded**: Phone numbers, password hashes, session IDs, private notes, reviewer IDs, review timestamps, private team membership details.
- **Verification Status**: Exposed as a simple boolean flag (`isVerified`) derived read-only from `verification` table (`status = 'VERIFIED'`).
- **Read-Only Operation**: Search operations execute in `@Transactional(readOnly = true)`. Discovery cannot create, update, or delete any entity.

## 4. API Endpoints

All search endpoints use HTTP `GET` with pagination (`page`, `size`, `sort`):
- `GET /api/discovery/workers`
- `GET /api/discovery/contractors`
- `GET /api/discovery/teams`
- `GET /api/discovery/requirements`

Default page size is 20; maximum page size cap is 50.
