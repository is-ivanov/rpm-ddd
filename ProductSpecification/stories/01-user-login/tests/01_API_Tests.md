# API Tests — User Login

> **Implementation Order**: Login (prerequisite) → Activation read (GET) → Activation write validation (POST) → Activation happy path (POST) → Current user (GET) → Logout (POST).

---

## 1. Login

### 1.1 Login with valid ACTIVE user returns session

**Given** a registered ACTIVE user
**When** the user logs in with correct credentials
**Then** the response status is 200
**And** the response contains a valid session cookie

### 1.2 Login with non-ACTIVE user returns 401

**Given** a registered user with non-ACTIVE status
**When** the user attempts to log in with correct credentials
**Then** the response status is 401
**And** the response contains an error message

---

## 2. Activation Token Validation (GET)

### 2.1 Valid activation token returns user info

**Given** a pending user with a valid activation token
**When** the activation token is validated
**Then** the response status is 200
**And** the response contains the user's login
**And** the response contains the user's email

### 2.2 Invalid or expired activation token returns error

**Given** an invalid or expired activation token
**When** the activation token is validated
**Then** the response status is 422
**And** the response contains an error indicating the token issue

---

## 3. Account Activation (POST) — Validation

### 3.1 Activate with password violating policy returns validation errors

**Given** a pending user with a valid activation token
**When** the user submits activation with password "weak"
**Then** the response status is 422
**And** the response contains validation errors for password policy violations

### 3.2 Activate with expired token returns error

**Given** an expired activation token
**When** the user submits activation with a valid password
**Then** the response status is 422
**And** the response contains an error indicating the token has expired

---

## 4. Account Activation (POST) — Happy Path

### 4.1 Activate with valid token and password succeeds

**Given** a pending user with a valid activation token
**When** the user submits activation with a password meeting the policy
**Then** the response status is 200
**And** the user can subsequently log in and receive a session

---

## 5. Current User Info (GET)

### 5.1 Authenticated user retrieves own info

**Given** an authenticated user with ACTIVE status
**When** the user requests their own info
**Then** the response status is 200
**And** the response contains the user's userId, login, email, firstName, lastName, status, and roles

---

## 6. Logout

### 6.1 Logout invalidates session

**Given** an authenticated user with an active session
**When** the user logs out
**Then** the response status is 200
**And** subsequent requests with the same session cookie return 401

---

## DSL Technical Reference

| Scenario | Method | Path | Auth | Request Body | Success Status | Key Assertions |
|---|---|---|---|---|---|---|
| 1.1 | POST | /api/auth/login | No | `{ login, password }` | 200 | Set-Cookie with JSESSIONID |
| 1.2 | POST | /api/auth/login | No | `{ login, password }` | 401 | Error message present |
| 2.1 | GET | /api/auth/activate?token={token} | No | — | 200 | Response body contains `login` and `email` |
| 2.2 | GET | /api/auth/activate?token={token} | No | — | 422 | Error indicates token issue |
| 3.1 | POST | /api/auth/activate | No | `{ token, password: "weak" }` | 422 | Validation errors for password policy |
| 3.2 | POST | /api/auth/activate | No | `{ token, password }` | 422 | Error indicates expired token |
| 4.1 | POST | /api/auth/activate | No | `{ token, password }` | 200 | Follow-up login succeeds with session |
| 5.1 | GET | /api/auth/me | JSESSIONID | — | 200 | Response contains userId, login, email, firstName, lastName, status, roles |
| 6.1 | POST | /api/auth/logout | JSESSIONID + CSRF | — | 200 | Follow-up request with same session returns 401 |
