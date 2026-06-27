# Infrastructure Tests — User Management

> **Implementation Order**: Failure handling → recovery validation.

---

## 1. Database Failure During List

### 1.1 Database unavailable during list returns 500 with a Problem Detail
**Level:** L1 acceptance  <!-- full-context resilience: needs the real app + a broken DB -->

**Given** an authenticated admin
**And** the database is unavailable
**When** the admin requests the user list
**Then** the response status is 500
**And** the response is an RFC 9457 Problem Detail with no internal details leaked

---

## 2. Database Recovery

### 2.1 Database recovery allows the list after an outage
**Level:** L1 acceptance  <!-- full-context resilience: real app, DB outage then recovery -->

**Given** an authenticated admin
**And** the database is unavailable
**When** the admin requests the user list
**Then** the response status is 500
**When** the database becomes available
**And** the admin requests the user list again
**Then** the response status is 200
**And** the response contains the user rows

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the admin requests the user list` | GET /api/admin/users with an authenticated session |
| `the database is unavailable` | Stop the PostgreSQL container or drop the connection; verify connectivity is lost |
| `the database becomes available` | Restart the DB container / restore the connection; verify health is restored |
| `an RFC 9457 Problem Detail with no internal details leaked` | application/problem+json body with type/title/status; no stack trace or SQL leaked |
