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
- [x] adapters-discovery
- [x] red-adapter rest
- [x] green-adapter rest
- [S] green-acceptance (validation tested at adapter level; acceptance test removed)

### Scenario 3.2: Activate with expired token returns error
- [S] red-acceptance (error case tested at Level 2 web slice, not Level 1 acceptance)
- [S] design (existing implementation — activate() calls findUserByToken() which throws ExpiredJwtException, already mapped to 422)
- [S] red-usecase (same findUserByToken() path already tested by ValidateTokenErrorTest)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (existing REST adapter + global ExpiredJwtException→422 mapping already tested via GET endpoint)
- [S] green-acceptance (no acceptance test to enable)

### Scenario 4.1: Activate with valid token and password succeeds
- [x] red-acceptance (ActivateAccountIntegrationTest — already passes)
- [S] design (existing implementation from Scenario 3.1)
- [S] red-usecase (happy path covered by Level 1 acceptance)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (existing adapters from Scenario 3.1)
- [x] green-acceptance

### Scenario 5.1: Authenticated user retrieves own info
- [x] red-acceptance (CurrentUserInfoIntegrationTest — @Disabled)
- [x] design
- [x] red-usecase
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (rest: simple delegation — acceptance test covers happy path, adapter code created in green-acceptance; db: findById is simple Spring Data query → [S])
- [S] red-adapter rest (simple delegation — acceptance test covers happy path)
- [S] green-adapter rest
- [x] green-acceptance

### Scenario 6.1: Logout invalidates session
- [x] red-acceptance (LogoutIntegrationTest — @Disabled)
- [x] design (ADR: logout-decision.md — web/security adapter method, no usecase/domain)
- [S] red-usecase (no business logic — logout is pure infrastructure/security)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain (no testable domain logic in this scenario)
- [S] green-domain
- [x] adapters-discovery (ports: none; exceptions: none; response: simple delegation)
- [S] red-adapter rest (simple delegation — no @Valid body, no error mapping; acceptance covers 200+401)
- [S] green-adapter rest (logout endpoint created in green-acceptance)
- [x] green-acceptance (logout endpoint + SecurityConfig auth on /me & /logout — see ADR)

## Integration Scenarios
(none — no external service dependencies in this story)

## Frontend Scenarios

**Setup: Initialize frontend application** — Vue 3 + TS + Vite + Vitest + Tailwind + Vue Router scaffold in `frontend/` with a simple main page (`/` → HomePage).
- [x] scaffold-frontend

### Scenario 1.1: Login page shows login and password fields and submit button
- [x] red-playwright (login-page.spec.ts — @skip; bootstrapped Playwright harness: playwright.config.ts, acceptance/tests/)
- [S] red-frontend (pure display scenario — no input-varying logic; field visibility, password masking via type=password, fixed "Sign In" label are presentational constants handled in the component during align-design; visibility covered by red-playwright E2E)
- [S] green-frontend (no logic produced in red-frontend)
- [S] red-frontend-api (no API interaction in this scenario — page-display only: fields visible, password masked via type=password, fixed "Sign In" label; login API client POST /api/auth/login belongs to credential-submission scenarios 3.1/3.2)
- [S] green-frontend-api (no API client produced in red-frontend-api)
- [x] align-design (LoginPage.vue built + /login route; matches mockup 01-login.html; Inter font + .form-input class; password toggle deferred to Scenario 2.1)
- [x] green-playwright (login-page.spec.ts passes — skip marker removed; frontend auto-started via Playwright webServer; display-only, no backend needed)
- [x] demo (ran login-page.spec.ts headed + slowMo 1200ms; passed; config reverted)

### Scenario 2.1: Password visibility toggle shows and hides password
- [x] red-playwright (login-page.spec.ts §2.1 — @skip; toggle type-switch + value-preservation asserted via password-toggle testid)
- [S] red-frontend (trivial-logic gate: password visibility toggle is presentational reactive state — showPassword ref + `:type="showPassword ? 'text' : 'password'"` ternary, built in align-design. No input-varying logic for a .logic.ts unit; observable behavior covered by red-playwright E2E §2.1)
- [S] green-frontend (no logic to implement — see red-frontend [S]; toggle built in align-design)
- [S] red-frontend-api (no API/HTTP interaction in §2.1 — pure client-side password type toggle, presentational; handled in align-design)
- [S] green-frontend-api (no API/HTTP interaction in §2.1 — see red-frontend-api [S]; toggle built in align-design)
- [x] align-design (toggle built in LoginPage.vue: showPassword ref + :type binding + password-toggle button with @lucide/vue Eye/EyeOff; matches mockup .toggle-password; @lucide/vue added as icon lib + technology.md/tailwind binding updated)
- [x] green-playwright (login-page.spec.ts §2.1 passes — skip marker removed; toggle switches type + preserves value; 2 passed)
- [~] demo

### Scenario 3.1: Wrong credentials show error banner
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 3.2: Inactive account shows error banner with activation message
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.1: Activation page shows password fields and complexity rules
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.1: Successful activation shows success message
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.2: Expired token shows error message
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 6.1: Clicking "Go to Sign In" navigates to login page
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
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
