# Security Tests — User Management

Attack surface this story: `GET /api/admin/users` (reads and renders user-provided text), `POST /api/admin/users` (accepts a JSON body, now with `timeZone`). Auth is required for both, but any authenticated user may reach them this story (no role gate yet — deferred to the roles story). Generic unauthenticated-access (401), security headers, CORS, and HTTPS are tested globally and excluded here. Filter/sort run client-side over the already-fetched list, so they reach no server query — no server-side injection surface there.

## 5.1 SQL injection in create fields is treated as literal text

```gherkin
Scenario: SQL injection payloads in create-user fields do not execute
  Given an authenticated admin
  When the admin submits a create-user request with an SQL injection payload in the login, email, and name fields
  Then no SQL is executed against the database
  And the request is either rejected with 422 or the payload is stored as literal text
  And the user table is intact
```

## 5.2 Stored XSS in a user name is escaped when rendered in the grid

```gherkin
Scenario: A script payload in a user name does not execute in the grid
  Given a user was created with a script payload in their name
  When an admin opens the Users page and the grid renders that row
  Then the payload is shown as inert text
  And no script executes in the browser
```

## 5.3 Mass assignment — extra fields on create are ignored

```gherkin
Scenario: Create request with extra privileged fields does not elevate the user
  Given an authenticated admin
  When the admin submits a create-user request with extra JSON fields "role: ADMIN", "status: ACTIVE", and "userId: <chosen-uuid>"
  Then the response status is 201
  And the created user has status PENDING (from the normal flow, not the injected field)
  And the created user has no elevated role
  And the user's id is server-generated, not the injected value
```

## 5.4 Input length limits on create fields are enforced

```gherkin
Scenario: Over-length create fields are rejected
  Given an authenticated admin
  When the admin submits a create-user request with a middle name over 255 characters and a login and email over their maximum length
  Then the response status is 422
  And the response is an RFC 9457 Problem Detail with field-level errors
```

## 5.5 Invalid timezone value is rejected

```gherkin
Scenario: A non-IANA timezone value is rejected
  Given an authenticated admin
  When the admin submits a create-user request with timezone "Mars/Olympus"
  Then the response status is 422
  And the response is an RFC 9457 Problem Detail with a field error for timezone
```

## 5.6 POST /api/admin/users without a CSRF token returns 403

```gherkin
Scenario: Create endpoint rejects a request missing the CSRF token
  Given an authenticated admin
  When a POST /api/admin/users is sent without a CSRF token
  Then the response status is 403
```

---

### DSL Technical Reference

| Scenario | Method | Endpoint | Key Assertions |
|----------|--------|----------|----------------|
| 5.1 | POST | /api/admin/users | injection payloads in login/email/name stored literally or 422; table intact |
| 5.2 | GET (render) | /api/admin/users → grid | script payload in a name renders inert; no execution |
| 5.3 | POST | /api/admin/users | 201; status PENDING; no role; server-generated id (injected role/status/userId ignored) |
| 5.4 | POST | /api/admin/users | 422 Problem Detail with field errors for over-length fields |
| 5.5 | POST | /api/admin/users | 422 Problem Detail with a `timeZone` field error for a non-IANA zone |
| 5.6 | POST | /api/admin/users | 403 when the CSRF token is missing |
