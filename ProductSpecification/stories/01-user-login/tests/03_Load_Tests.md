# Load Tests — User Login

> **Implementation Order**: Single-request baseline → concurrent load → ancillary endpoint baseline.

---

## 3. Login Response Time

### 3.1 Login response time under 200ms

**Given** a registered ACTIVE user
**When** the user submits login credentials
**Then** the response status is 200
**And** the response time is under 200ms

---

## 4. Concurrent Login

### 4.1 Concurrent login requests complete under 500ms

**Given** 50 registered ACTIVE users
**When** all 50 users submit login credentials simultaneously
**Then** all responses have status 200
**And** the maximum response time across all requests is under 500ms

---

## 5. Activation Response Time

### 5.1 Activation token validation response time under 200ms

**Given** a valid activation token for a PENDING user
**When** the user requests the activation info endpoint
**Then** the response status is 200
**And** the response time is under 200ms

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a registered ACTIVE user` | User record in DB with status ACTIVE, known login and password hash |
| `the user submits login credentials` | POST /api/auth/login with { login, password } |
| `response time is under Nms` | Measure wall-clock time from request send to response received; assert < N |
| `50 registered ACTIVE users` | 50 user records in DB with status ACTIVE, each with unique login and known password |
| `all 50 users submit login credentials simultaneously` | Fire 50 concurrent HTTP requests using a load-test client (e.g., JMeter, Gatling, or virtual threads) |
| `maximum response time across all requests` | Collect per-request durations, assert max(durations) < threshold |
| `a valid activation token for a PENDING user` | JWT with type=activation, unexpired, claims matching a PENDING user in DB |
| `the user requests the activation info endpoint` | GET /api/auth/activate?token={jwt} |
