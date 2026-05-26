# Story 1: User login — Progress

## Spec
- [x] interview
- [x] story
- [x] mockups
- [x] api-spec
- [x] test-spec

## Backend Scenarios

### Scenario 1.1: Login with valid ACTIVE user returns session
- [x] red-acceptance (AuthLoginIntegrationTest — already passes)
- [S] design (feature already implemented)
- [S] red-usecase (happy path covered by Level 1 acceptance)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain (no testable domain logic in this scenario)
- [S] green-domain
- [x] adapters-discovery (existing adapters sufficient)
- [x] green-acceptance

### Scenario 1.2: Login with non-ACTIVE user returns 401
- [x] red-acceptance (LoginStatusValidationIntegrationTest — 3 tests for PENDING/LOCKED/INACTIVE)
- [x] design (existing implementation)
- [x] red-usecase (AuthenticationServiceTest — PENDING and LOCKED cases)
- [x] green-usecase
- [S] red-domain (per-message variations covered by acceptance tests; UserStatus.authenticationErrorMessage() branches covered at L1)
- [S] green-domain
- [x] adapters-discovery (existing adapters sufficient: SecurityConfig maps UserAuthenticationException)
- [x] green-acceptance

### Scenario 2.1: Valid activation token returns user info
- [x] red-acceptance (ActivationTokenValidationIntegrationTest — @Disabled)
- [x] design
- [x] red-usecase (ActivationServiceTest.ValidateTokenTest)
- [x] green-usecase
- [S] red-domain (no testable domain logic)
- [S] green-domain
- [x] adapters-discovery
- [x] red-adapter rest (AuthResourceTest.ValidateActivationTokenTest)
- [x] green-adapter rest
- [x] green-acceptance

### Scenario 2.2: Invalid or expired activation token returns error
- [S] red-acceptance (error cases tested at Level 2 web slice, not Level 1 acceptance)
- [x] design
- [S] red-usecase (usecase already throws JWT exceptions — adapter maps them to 422)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (rest: JWT exceptions unmapped → 422)
- [x] red-adapter rest (AuthResourceTest.ValidateActivationTokenTest — @Disabled: invalid + expired token)
- [x] green-adapter rest
- [S] green-acceptance (no acceptance test to enable)

### Scenario 3.1: Activate with password violating policy returns validation errors
- [x] red-acceptance
- [x] design
- [x] red-usecase
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [~] adapters-discovery
- [ ] green-acceptance

### Scenario 3.2: Activate with expired token returns error
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 4.1: Activate with valid token and password succeeds
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.1: Authenticated user retrieves own info
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 6.1: Logout invalidates session
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Integration Scenarios
(none — no external service dependencies in this story)

## Frontend Scenarios

### Scenario 1.1: Login page shows login and password fields and submit button
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 2.1: Password visibility toggle shows and hides password
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 3.1: Wrong credentials show error banner
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 3.2: Inactive account shows error banner with activation message
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 4.1: Activation page shows password fields and complexity rules
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 5.1: Successful activation shows success message
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 5.2: Expired token shows error message
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

### Scenario 6.1: Clicking "Go to Sign In" navigates to login page
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

## Security Scenarios

### Scenario 5.1: SQL injection in login field does not bypass authentication
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.2: Login rate limiting blocks after N failed attempts
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.3: Passwords are stored hashed
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.4: Tampered JWT activation token is rejected
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.5: Expired JWT activation token is rejected
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.6: POST /api/auth/activate without CSRF token returns 403
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.7: Mass assignment on activate endpoint — extra fields ignored
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.8: Oversized password input rejected
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Load Scenarios

### Scenario 3.1: Login response time under 200ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 4.1: Concurrent login requests complete under 500ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.1: Activation token validation response time under 200ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Infrastructure Scenarios

### Scenario 4.1: Database unavailable during login returns 500
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.1: Database recovery allows login after outage
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance
