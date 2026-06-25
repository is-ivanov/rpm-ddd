# Load Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Full list of 1000 users returns under 1s

**Given** 1000 users in the database
**When** the admin requests the full user list
**Then** the response status is 200
**And** the response contains all 1000 rows with resolved actor names
**And** the response time is under 1000ms

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `1000 users in the database` | Bulk-insert 1000 user rows with resolvable createdBy/updatedBy actors |
| `the admin requests the full user list` | GET /api/admin/users with an authenticated session |
| `response time is under Nms` | Measure wall-clock time from request send to response received; assert < N |
