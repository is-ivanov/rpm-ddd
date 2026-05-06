# User Login - Notes & Considerations

## Warnings

### Functional Warnings
- Activation token replay: a used token still validates until expiry, but the user is already ACTIVE so re-setting password either has no effect or returns an error
- PENDING users have a placeholder password (random UUID hash) — they cannot log in until activated
- PasswordPolicy validation during activation must run before status change — partial state (password set but still PENDING) must not occur

### UI/UX Warnings
- Login page must display user-status-specific messages (PENDING → "Activate your account", LOCKED → "Account locked", INACTIVE → "Account deactivated")
- Activation page should show the user's email/login from the token so they know which account they're activating
- Password complexity rules must be visible before submission, not just on error

### Technical Warnings
- CSRF token must be obtained from GET /api/auth/csrf before any state-changing request
- JWT activation tokens are not tracked in DB — no revocation mechanism until Story 2 adds real email delivery
- Session cookie is HttpOnly — frontend cannot read JSESSIONID directly

---

## Suggestions & Future Enhancements

### Functional Suggestions
- Remember-me functionality (out of scope)
- Password reset / forgot password flow (out of scope)
- Account lockout after N failed attempts

### UI/UX Suggestions
- Show password strength indicator on activation form
- Redirect to login page after successful activation with success message

### Technical Suggestions
- Add rate limiting on login endpoint to prevent brute force
- Consider jti tracking for activation tokens for proper revocation
- Two-factor authentication (out of scope)
- OAuth / SSO integration (out of scope)

---

## Technical Notes

### Load Considerations
- First story, single-user development phase
- No significant load concerns at this stage

### Security Considerations
- Session-based auth with CSRF protection mitigates CSRF attacks
- Password hashing via PasswordEncoder (BCrypt)
- JWT tokens signed with configurable secret key
- Activation tokens have configurable expiration
- HttpOnly + Secure flags on session cookie
- PasswordPolicy enforces strong passwords (12+ chars, complexity rules)

### Infrastructure Notes
- Session-based auth requires sticky sessions or shared session store for multi-instance deployment
- JWT signing key must be consistent across instances
- No external service dependencies for this story (NoOp email sender logs to console)

### Integration Notes
- NoOpEmailNotificationSender currently delivers activation tokens via console logs
- Story 2 replaces NoOp with real email delivery via Spring Mail
- Activation flow works end-to-end without Story 2 (tokens visible in server logs)

---

## Additional Context

- See `interview.md` for detailed architectural decisions and existing implementation inventory
- UserRegisteredEvent is published on registration — triggers activation token generation
- Existing tested infrastructure: AuthResource (login + csrf endpoints), SecurityConfig, RpmUserDetailsService
- Domain model already has: User entity, UserRepository, UserId, Login, Password, EmailAddress, PersonName, UserStatus, UserRegisteredEvent
