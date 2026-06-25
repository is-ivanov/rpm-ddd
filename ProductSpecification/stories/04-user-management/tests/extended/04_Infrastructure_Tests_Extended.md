# Infrastructure Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Database failure during create returns 500 without partial state

**Given** an authenticated admin
**And** the database is unavailable
**When** the admin submits a valid create-user request
**Then** the response status is 500
**And** no partial user row is persisted
**And** no activation email is dispatched for the failed create

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the database is unavailable` | Stop the PostgreSQL container or drop the connection |
| `the admin submits a valid create-user request` | POST /api/admin/users with a valid body and CSRF token |
| `no partial user row is persisted` | After recovery, GET /api/admin/users does not contain the attempted user |
