# Discovery API

## Purpose
Read-only marketplace search and filtering endpoints for workers, contractors, teams, and open workforce requirements.

## Authentication
Required (`Authorization: Bearer <sessionToken>`) for all endpoints.

## Pagination Policy
Default page size: `20`. Max page size: `50`.

---

## Endpoints

### GET /api/discovery/workers
Search active worker profiles. `200 OK`.
- Query Params: `location`, `skillId`, `availabilityStatus`, `minExperience`, `maxDailyRate`, `isTravelWilling`, `isVerified`, `page`, `size`, `sort`.

### GET /api/discovery/contractors
Search contractor profiles. `200 OK`.
- Query Params: `location`, `name`, `isVerified`, `page`, `size`, `sort`.

### GET /api/discovery/teams
Search structured teams. `200 OK`.
- Query Params: `location`, `name`, `page`, `size`, `sort`.

### GET /api/discovery/requirements
Search open workforce requirements. `200 OK`.
- Query Params: `location`, `workerType`, `skillId`, `minDailyRate`, `maxDailyRate`, `accommodationAvailable`, `foodAvailable`, `page`, `size`, `sort`.
