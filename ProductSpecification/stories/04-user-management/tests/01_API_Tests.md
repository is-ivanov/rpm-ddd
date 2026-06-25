# API Tests — User Management (Admin User Grid & Create User)

> **Implementation Order**: List users (GET, the new endpoint) → Create user validation (POST) → Create user happy path (POST, extends the existing registration acceptance test).

The create endpoint (`POST /api/admin/users`) already exists and is reused; this story only adds a `timeZone` field and the audit data the grid needs. Acceptance covers the happy path only — per-field and per-status variations live in web-slice (Level 2) and domain/usecase (Level 3-4) tests.

---

## 1. List Users (GET)

### 1.1 Authenticated user lists all users with resolved actor names

**Given** several registered users created by the seed actor and by an admin
**When** the user requests the admin user list
**Then** the response status is 200
**And** each row contains the user's name parts, login, email, status, createdAt, createdBy, updatedAt, and updatedBy
**And** the seed actor is returned with name "System" (never a raw UUID)
**And** an admin-created user's createdBy and updatedBy resolve to that admin's name
**And** the rows are returned in deterministic order: createdAt descending, then userId descending

---

## 2. Create User (POST) — Validation

### 2.1 Create with a duplicate login returns a field-level 422

**Given** an existing user with login "alice"
**When** an admin submits a create-user request with login "alice" and an otherwise valid body
**Then** the response status is 422
**And** the response is an RFC 9457 Problem Detail
**And** the `fieldErrors` array contains an entry for the `login` field

---

## 3. Create User (POST) — Happy Path

> Extends the existing registration acceptance test — add `timeZone` to the request and assert the new user is listed in the grid. Do NOT create a parallel acceptance class: the existing test already provisions the full context and asserts the activation email.

### 3.1 Create user with a timezone succeeds and appears in the grid

**Given** an authenticated admin
**When** the admin submits a valid create-user request including a timezone "Europe/Berlin"
**Then** the response status is 201
**And** the response Location header points to the created user
**And** the activation email is delivered to the new user (existing consequence)
**And** the new user appears in the admin user list with status PENDING
**And** the new user's createdAt equals its updatedAt
**And** the new user's createdBy and updatedBy both resolve to the creating admin's name

---

## DSL Technical Reference

| Scenario | Method | Path | Auth | Request Body | Success Status | Key Assertions |
|---|---|---|---|---|---|---|
| 1.1 | GET | /api/admin/users | JSESSIONID | — | 200 | Rows carry name parts, login, email, status, createdAt/By, updatedAt/By; seed actor name = "System"; order createdAt DESC, userId DESC |
| 2.1 | POST | /api/admin/users | JSESSIONID + CSRF | `{ firstName, lastName, login: "alice", email, timeZone }` | 422 | Problem Detail with `fieldErrors[].property == "login"` |
| 3.1 | POST | /api/admin/users | JSESSIONID + CSRF | `{ firstName, middleName?, lastName, login, email, timeZone: "Europe/Berlin" }` | 201 | Location header; activation email delivered; user listed PENDING; createdAt == updatedAt; createdBy == updatedBy == admin name |
