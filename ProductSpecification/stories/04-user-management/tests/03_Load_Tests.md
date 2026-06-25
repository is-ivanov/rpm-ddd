# Load Tests — User Management

> **Implementation Order**: Single-request baseline → concurrent load → volume (full list).

The grid filters and sorts client-side over the full list, so the backend always returns every user in one response. These tests guard that the full-list response stays fast as the user count grows.

---

## 1. List Response Time

### 1.1 List users response time under 200ms

**Given** a populated user table
**When** the admin requests the user list
**Then** the response status is 200
**And** the response time is under 200ms

---

## 2. Concurrent List

### 2.1 Concurrent list requests complete under 500ms

**Given** 50 authenticated admins
**When** all 50 request the user list simultaneously
**Then** all responses have status 200
**And** the maximum response time across all requests is under 500ms

---

## 3. Volume

### 3.1 Full list of 500 users returns under 500ms

**Given** 500 users in the database
**When** the admin requests the full user list
**Then** the response status is 200
**And** the response contains all 500 rows with resolved actor names
**And** the response time is under 500ms

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a populated user table` | DB seeded with a representative set of users (varied statuses, actors) |
| `the admin requests the user list` | GET /api/admin/users with an authenticated session |
| `response time is under Nms` | Measure wall-clock time from request send to response received; assert < N |
| `50 authenticated admins` | 50 sessions; fire 50 concurrent GET requests via a load-test client |
| `maximum response time across all requests` | Collect per-request durations, assert max(durations) < threshold |
| `500 users in the database` | Bulk-insert 500 user rows with resolvable createdBy/updatedBy actors |
