# Story 1: User login — Interview

## Scope

**In scope:**
- Login endpoint with user status validation (only ACTIVE users may log in)
- Activation/password-set flow: two-step — GET validates token + returns user info, POST sets password + activates account
- GET /api/auth/me — returns current authenticated user info
- POST /api/auth/logout — server-side session invalidation
- Frontend login page (first frontend story)
- Frontend activation page (set password after clicking email link)

**Out of scope:**
- Remember-me
- Password reset / forgot password
- Two-factor authentication
- OAuth / SSO
- Real email delivery (Story 2 replaces NoOpEmailNotificationSender)

## Key Architectural Decisions

- DECISION: Only ACTIVE users can log in. PENDING, LOCKED, INACTIVE → 401 Unauthorized with status-specific message.
- DECISION: Two-step activation. GET /api/auth/activate?token=XXX returns user info (login, email) from the JWT so frontend can display "Set password for user@example.com". POST /api/auth/activate with {token, newPassword} validates the token, applies PasswordPolicy, sets the password hash, changes user status to ACTIVE.
- DECISION: JWT expiry only for activation tokens. No jti tracking in DB — token replay is not checked. A used token still technically validates until it expires, but the user is already ACTIVE so re-setting password has no effect (or returns an error).
- DECISION: Login returns void (200 OK + JSESSIONID cookie). Frontend calls GET /api/auth/me separately to get user info.
- DECISION: Server-side session invalidation for logout (invalidate HttpSession, clear JSESSIONID cookie).
- DECISION: PasswordPolicy applies during activation (min 12 chars, at least one uppercase, lowercase, digit, special character, no whitespace). Same rules if user ever creates a new password in the future.

## Business Rules & Constraints

- User status flow: PENDING (registered) → ACTIVE (activated) → LOCKED/INACTIVE (admin action).
- PENDING users have a placeholder password (random UUID hash) — they cannot log in until they activate.
- Activation token is a JWT with claims: userId (subject), jti, expiration, type=activation.
- Activation token is generated on UserRegisteredEvent. Currently delivered via NoOpEmailNotificationSender (logs to console). Story 2 adds real email delivery.
- Password complexity enforced by PasswordPolicy (passay): 12–128 chars, upper, lower, digit, special, no whitespace.
- Session-based auth with CSRF protection (SPA cookie pattern). CSRF token obtained from GET /api/auth/csrf.

## Already Implemented (REUSE)

- **AuthResource** — POST /api/auth/login, GET /api/auth/csrf (working, tested)
- **SecurityConfig** — session-based auth, CSRF via SPA cookie, DaoAuthenticationProvider, permitAll on /api/auth/**
- **RpmUserDetailsService** — loads User from UserRepository by Login, returns RpmUserDetails
- **RpmUserDetails** — maps domain User to Spring Security UserDetails (userId, login, password hash)
- **LoginRequest** — record with login and password fields
- **AuthLoginIntegrationTest** — valid login (200 + JSESSIONID), wrong password (401)
- **AuthCsrfIntegrationTest** — CSRF token in XSRF-TOKEN cookie
- **AuthApi + AuthSessionFactory** — test fixtures for auth HTTP calls and session management
- **User domain** — User entity, UserRepository, UserId, Login, Password, EmailAddress, PersonName, UserStatus, UserRegisteredEvent
- **UserRegistrationService** — registers user with placeholder password, publishes UserRegisteredEvent
- **JwtActivationTokenGenerator** — generates JWT activation tokens with configurable expiration and signing key
- **UserRegisteredEventListener** — listens for UserRegisteredEvent, generates JWT, calls EmailNotificationSender
- **NoOpEmailNotificationSender** — logs activation token to console (replaced in Story 2)
- **PasswordPolicy** — validates password complexity and hashes via PasswordEncoder

## NOT Yet Implemented (Gaps)

- User status check during login (RpmUserDetailsService loads user but doesn't check status)
- GET /api/auth/activate?token=XXX — validate JWT, return user info (login, email)
- POST /api/auth/activate — validate JWT, apply PasswordPolicy, set password, change status to ACTIVE
- GET /api/auth/me — return current authenticated user info
- POST /api/auth/logout — invalidate session
- Frontend login page (Vue 3 + Tailwind)
- Frontend activation page (set password form)

## Cross-Story Dependencies

- **Story 2** (Email integration) will replace NoOpEmailNotificationSender with real email delivery via Spring Mail. The activation flow in this story works end-to-end — tokens just appear in logs instead of emails until Story 2 is done.
- No other story dependencies. This is the first story.
