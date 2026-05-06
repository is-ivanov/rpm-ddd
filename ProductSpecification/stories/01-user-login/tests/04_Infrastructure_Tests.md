# Infrastructure Tests — User Login

> **Implementation Order**: Failure handling → recovery validation.

---

## 4. Database Failure During Login

### 4.1 Database unavailable during login returns 500 with error message

**Given** a registered ACTIVE user
**And** the database is unavailable
**When** the user submits login credentials
**Then** the response status is 500
**And** the response contains an error message indicating a server error

---

## 5. Database Recovery

### 5.1 Database recovery allows login after outage

**Given** a registered ACTIVE user
**And** the database is unavailable
**When** the user submits login credentials
**Then** the response status is 500
**When** the database becomes available
**And** the user submits login credentials again
**Then** the response status is 200
**And** the response contains a valid session

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a registered ACTIVE user` | User record in DB with status ACTIVE, known login and password hash |
| `the user submits login credentials` | POST /api/auth/login with { login, password } |
| `the database is unavailable` | Drop network connection to PostgreSQL or stop the DB container; verify connectivity is lost |
| `the database becomes available` | Restore network connection or restart the DB container; verify health endpoint returns healthy |
| `the response contains an error message indicating a server error` | JSON body with non-empty `error` or `message` field; no stack trace or internal details leaked |
| `the response contains a valid session` | Response includes Set-Cookie header with JSESSIONID; subsequent request with that cookie to GET /api/auth/me returns 200 |
