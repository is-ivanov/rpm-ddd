# Security Tests — User Login

## 5.1 SQL injection in login field does not bypass authentication

```gherkin
Scenario: SQL injection payload in login field is treated as literal text
  Given a registered user with login "alice" and password "Passw0rd!"
  When a POST /api/auth/login is sent with login "alice' OR '1'='1" and password "anything"
  Then the response status is 401
  And the response body contains error "Invalid credentials"
```

```gherkin
Scenario: SQL injection payload in password field is treated as literal text
  Given a registered user with login "alice" and password "Passw0rd!"
  When a POST /api/auth/login is sent with login "alice" and password "' OR '1'='1 --"
  Then the response status is 401
  And the response body contains error "Invalid credentials"
```

## 5.2 Login rate limiting blocks after N failed attempts

```gherkin
Scenario: Account is temporarily locked after 5 consecutive failed login attempts
  Given a registered user with login "alice" and password "Passw0rd!"
  When 5 consecutive POST /api/auth/login requests are sent with login "alice" and wrong password
  Then the 5th response status is 429
  And the response body contains error "Too many failed attempts"
  And a subsequent login with the correct password within the lockout window returns 429
```

## 5.3 Passwords are stored hashed (not plaintext)

```gherkin
Scenario: Password is not retrievable in plaintext from the database
  Given a registered user with login "alice" and password "Passw0rd!"
  When the user row is queried directly from the database
  Then the stored password does not contain the string "Passw0rd!"
  And the stored password starts with the BCrypt identifier "$2a$"
```

## 5.4 Tampered JWT activation token is rejected

```gherkin
Scenario: Activation with a JWT whose payload was modified fails
  Given a registered user with login "alice" in PENDING status
  And a valid activation token was issued for alice
  When a POST /api/auth/activate is sent with the token payload modified (changed login to "bob") and re-signed with a random secret
  Then the response status is 422
  And the response body contains error "Invalid activation token"
```

```gherkin
Scenario: Activation with a JWT signed with the wrong secret fails
  Given a registered user with login "alice" in PENDING status
  When a POST /api/auth/activate is sent with a token signed with "wrong-secret-key" instead of the application secret
  Then the response status is 422
  And the response body contains error "Invalid activation token"
```

## 5.5 Expired JWT activation token is rejected

```gherkin
Scenario: Activation with an expired JWT token fails
  Given a registered user with login "alice" in PENDING status
  And an activation token was issued with expiry in the past
  When a POST /api/auth/activate is sent with that expired token
  Then the response status is 422
  And the response body contains error "Activation token has expired"
```

## 5.6 POST /api/auth/activate without CSRF token returns 403

```gherkin
Scenario: Activate endpoint rejects request missing CSRF token
  Given a registered user with login "alice" in PENDING status
  And a valid activation token for alice
  When a POST /api/auth/activate is sent without a CSRF token
  Then the response status is 403
```

## 5.7 Mass assignment on activate endpoint — extra fields ignored

```gherkin
Scenario: Activate request with extra fields does not modify user state beyond activation
  Given a registered user with login "alice" in PENDING status
  And a valid activation token for alice
  When a POST /api/auth/activate is sent with the token and extra JSON fields "role: ADMIN" and "status: ACTIVE"
  Then the response status is 200
  And the user's role remains "USER"
  And the user's status is "ACTIVE" from normal activation, not from the extra field
```

## 5.8 Oversized password input rejected

```gherkin
Scenario: Password exceeding maximum length is rejected during activation
  Given a registered user with login "alice" in PENDING status
  And a valid activation token for alice
  When a POST /api/auth/activate is sent with a password of 200 characters
  Then the response status is 422
  And the response body contains error describing password length constraint
```

---

### DSL Technical Reference

| Scenario | Method | Endpoint | Key Assertions |
|----------|--------|----------|----------------|
| 5.1 (login injection) | POST | /api/auth/login | status 401, error message on SQL injection payloads in login and password fields |
| 5.1 (password injection) | POST | /api/auth/login | status 401, error message on SQL injection payload in password field |
| 5.2 | POST | /api/auth/login | status 429 on 5th attempt, 429 on correct password during lockout |
| 5.3 | DB query | direct JDBC | stored password != plaintext, starts with "$2a$" |
| 5.4 (tampered payload) | POST | /api/auth/activate | status 422, error "Invalid activation token" |
| 5.4 (wrong secret) | POST | /api/auth/activate | status 422, error "Invalid activation token" |
| 5.5 | POST | /api/auth/activate | status 422, error "Activation token has expired" |
| 5.6 | POST | /api/auth/activate | status 403 without CSRF token |
| 5.7 | POST | /api/auth/activate | status 200, role unchanged, status from normal flow |
| 5.8 | POST | /api/auth/activate | status 422, password length error |
