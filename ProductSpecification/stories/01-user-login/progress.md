# Story 1: User login — Progress

> Terse entries (status + test-class/ADR ref + `see summaries/X` link). The "why" lives in
> `summaries/` + `carryover.md`; see `.claude/rules/workflow.md` → "Updating Progress".

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
- [S] red-usecase (happy path → Level 1)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (existing adapters sufficient)
- [x] green-acceptance

### Scenario 1.2: Login with non-ACTIVE user returns 401
- [x] red-acceptance (LoginStatusValidationIntegrationTest — PENDING/LOCKED/INACTIVE)
- [x] design (existing implementation)
- [x] red-usecase (AuthenticationServiceTest — PENDING, LOCKED)
- [x] green-usecase
- [S] red-domain (UserStatus message branches covered at L1)
- [S] green-domain
- [x] adapters-discovery (SecurityConfig maps UserAuthenticationException)
- [x] green-acceptance

### Scenario 2.1: Valid activation token returns user info
- [x] red-acceptance (ActivationTokenValidationIntegrationTest)
- [x] design
- [x] red-usecase (ActivationServiceTest.ValidateTokenTest)
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
- [x] red-adapter rest (AuthResourceTest.ValidateActivationTokenTest)
- [x] green-adapter rest
- [x] green-acceptance

### Scenario 2.2: Invalid or expired activation token returns error
- [S] red-acceptance (error case → Level 2 web slice)
- [x] design
- [S] red-usecase (usecase throws JWT exceptions — adapter maps to 422)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (rest: JWT exceptions unmapped → 422)
- [x] red-adapter rest (AuthResourceTest.ValidateActivationTokenTest — invalid + expired)
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
- [S] green-acceptance (validation tested at adapter level)

### Scenario 3.2: Activate with expired token returns error
- [S] red-acceptance (error case → Level 2 web slice)
- [S] design (existing — activate() → findUserByToken() throws ExpiredJwtException, mapped 422)
- [S] red-usecase (same findUserByToken() path covered by ValidateTokenErrorTest)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (global ExpiredJwtException→422 already tested via GET)
- [S] green-acceptance (no acceptance test to enable)

### Scenario 4.1: Activate with valid token and password succeeds
- [x] red-acceptance (ActivateAccountIntegrationTest — already passes)
- [S] design (existing implementation from Scenario 3.1)
- [S] red-usecase (happy path → Level 1)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (existing adapters from Scenario 3.1)
- [x] green-acceptance

### Scenario 5.1: Authenticated user retrieves own info
- [x] red-acceptance (CurrentUserInfoIntegrationTest)
- [x] design
- [x] red-usecase
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (rest: simple delegation, code in green-acceptance; db: simple findById → [S])
- [S] red-adapter rest (simple delegation — acceptance covers happy path)
- [S] green-adapter rest
- [x] green-acceptance

### Scenario 6.1: Logout invalidates session
- [x] red-acceptance (LogoutIntegrationTest)
- [x] design (ADR: logout-decision.md — web/security adapter method, no usecase/domain)
- [S] red-usecase (logout is pure infrastructure/security)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (ports: none; simple delegation)
- [S] red-adapter rest (simple delegation — acceptance covers 200+401)
- [S] green-adapter rest (logout endpoint created in green-acceptance)
- [x] green-acceptance (logout endpoint + SecurityConfig auth on /me & /logout — see ADR)

## Integration Scenarios
(none — no external service dependencies in this story)

## Frontend Scenarios

**Setup: Initialize frontend application** — Vue 3 + TS + Vite + Vitest + Tailwind + Vue Router scaffold in `frontend/` with a simple main page (`/` → HomePage).
- [x] scaffold-frontend

### Scenario 1.1: Login page shows login and password fields and submit button
- [x] red-playwright (login-page.spec.ts — bootstrapped Playwright harness)
- [S] red-frontend (trivial-logic gate — presentational, built in align-design)
- [S] green-frontend
- [S] red-frontend-api (page-display only, no API)
- [S] green-frontend-api
- [x] align-design (LoginPage.vue + /login route; matches mockup 01-login.html)
- [x] green-playwright
- [x] demo

### Scenario 2.1: Password visibility toggle shows and hides password
- [x] red-playwright (login-page.spec.ts §2.1 — password-toggle testid)
- [S] red-frontend (trivial-logic gate — presentational toggle, built in align-design)
- [S] green-frontend
- [S] red-frontend-api (no API — client-side toggle)
- [S] green-frontend-api
- [x] align-design (showPassword ref + :type binding + @lucide/vue Eye/EyeOff)
- [x] green-playwright
- [x] demo

### Scenario 3.1: Wrong credentials show error banner
- [x] red-playwright (login-page.spec.ts §3.1 — 401 mock, banner text + fields cleared)
- [S] red-frontend (trivial-logic gate — pass-through + presentational, built in align-design)
- [S] green-frontend
- [x] red-frontend-api (login.api.test.ts — MSW 401 problem+json → LoginError detail)
- [x] green-frontend-api (login.api.ts login())
- [x] align-design (LoginPage.vue wired to login(); error-banner; matches mockup 02)
- [x] green-playwright
- [x] demo

### Scenario 3.2: Inactive account shows error banner with activation message
- [x] red-playwright (login-page.spec.ts §3.2 — 401 "Account not activated" + activation-link)
- [S] red-frontend (trivial-logic gate — response-mapping is API-client concern)
- [S] green-frontend
- [x] red-frontend-api (login.api.test.ts §3.2 — type=authentication-failed → requiresActivation)
- [x] green-frontend-api (login.api.ts maps AUTHENTICATION_FAILED_TYPE → requiresActivation)
- [x] align-design (LoginErrorBanner.vue extracted — conditional icon + activation-link; mockup 03)
- [x] green-playwright
- [x] demo

### Scenario 4.1: Activation page shows password fields and complexity rules
- [x] red-playwright (activation-page.spec.ts §4.1 — fields masked, 6 complexity rules, button)
- [S] red-frontend (trivial-logic gate — pure page-display)
- [S] green-frontend
- [x] red-frontend-api (activation.api.test.ts — GET /api/auth/activate → {login,email})
- [x] green-frontend-api (activation.api.ts validateActivationToken())
- [x] align-design (ActivationPage.vue + /activate; validate-on-load subtitle; PasswordField.vue; mockup 04)
- [x] green-playwright
- [x] demo

### Scenario 5.1: Successful activation shows success message
> Origin-gate resolved 2026-06-06: dev = Vite proxy (same-origin, relative `/api`, no CORS). See activation-flow.md.
- [x] red-playwright (activation-page.spec.ts §5.1 — csrf+activate mock, success screen testids)
- [S] red-frontend (trivial-logic gate — request build is identity pass-through; orchestration is API-client)
- [S] green-frontend
- [x] red-frontend-api (activate-account.api.test.ts — GET csrf + POST activate, X-XSRF-TOKEN header)
- [x] green-frontend-api (activation.api.ts activateAccount() — csrf handshake + POST)
- [x] align-design (ActivationSuccess.vue; mockup 05; .auth-card/.btn-primary extracted to style.css)
- [x] green-playwright (see summaries/5-1-activation-success.md — SVG-icon assertion fix)
- [x] demo

### Scenario 5.2: Expired token shows error message
- [x] red-playwright (activation-page.spec.ts §5.2 — 422 mock, error view testids; shared assertScreenIconIsVisible)
- [S] red-frontend (trivial-logic gate — expired/invalid is HTTP-status, API-client concern)
- [S] green-frontend
- [x] red-frontend-api (activation.api.test.ts — 422 problem+json → ActivationError "Token expired")
- [x] green-frontend-api (validateActivationToken: !response.ok → throw ActivationError; ProblemDetail type pulled up)
- [x] align-design (ActivationExpired.vue; tokenInvalid only on instanceof ActivationError; ActivationResultCard.vue extracted; mockup 06)
- [x] green-playwright
- [x] demo

### Scenario 6.1: Clicking "Go to Sign In" navigates to login page
- [x] red-playwright (activation-page.spec.ts §6.1 — reaches success via §5.1, clicks button, asserts /login)
- [S] red-frontend (trivial-logic gate — unconditional router.push('/login'), handler from §5.1)
- [S] green-frontend
- [S] red-frontend-api (no API — pure client-side navigation)
- [S] green-frontend-api
- [x] align-design (verification-only — navigation between already-aligned §5.1/§1.1 screens)
- [x] green-playwright
- [x] demo (NB: demo config needs deviceScaleFactor:undefined alongside viewport:null in chromium project)

> **Promoted 2026-06-20 (issue #189) from `tests/extended/02_UI_Tests_Extended.md`** (FE audit; improvements.md I4):
> four deferred-by-design UI cases became core scenarios 2.2/3.3/4.2/4.3. Dependency: #191 (zod) MERGED →
> 4.2/4.3 unblocked. The dead "Request a new activation email" link is out of scope → improvements.md I5.

### Scenario 2.2: Login page shows loading state during submission
- [x] red-playwright (login-loading.spec.ts §2.2 — held-promise POST; submit-loading + inputs disabled)
- [S] red-frontend (trivial-logic gate — presentational submitting ref, built in align-design)
- [S] green-frontend
- [S] red-frontend-api (wraps existing login() — no new HTTP surface)
- [S] green-frontend-api
- [x] align-design (LoginPage.vue submitting ref + LoaderCircle spinner + :disabled; PasswordField disabled prop)
- [x] green-playwright
- [x] demo (recording demo-login-loading-2-2.webm)

### Scenario 3.3: Error banner dismiss button closes the banner
- [x] red-playwright (login-error-dismiss.spec.ts §3.3 — givenErrorBannerIsVisible; error-banner-dismiss testid)
- [S] red-frontend (trivial-logic gate — presentational ref-clearing, built in align-design)
- [S] green-frontend
- [S] red-frontend-api (no HTTP — pure client-side state reset)
- [S] green-frontend-api
- [x] align-design (LoginErrorBanner.vue dismiss button → emit; LoginPage @dismiss clears refs; .icon-button extracted)
- [x] green-playwright
- [x] demo (recording demo-login-error-dismiss-3-3.webm)

> **§4.2 REDESIGNED 2026-06-21 (user direction during align-design):** aggregate weak/medium/strong indicator
> superseded by a PER-RULE contract (`evaluateComplexityRules`, `complexity-rule-{key}` + `data-met`) to match
> mockup 04. Prior data-strength commits (a660043/01dbda7/3f19741) superseded. See summaries/4-2-password-strength.md.

### Scenario 4.2: Activation page shows password strength indicator updating in real-time
- [x] red-playwright (activation-strength.spec.ts — per-rule complexity-rule-{key} + data-met; supersedes a660043)
- [x] red-frontend (password-strength.logic.test.ts — per-rule ComplexityRule[]; parametrized 7-case table)
- [x] green-frontend (evaluateComplexityRules — 6 rules length/uppercase/lowercase/digit/special/no-spaces)
- [S] red-frontend-api (no API — pure client-side evaluation)
- [S] green-frontend-api
- [x] align-design (complexityRules computed + data-met styling; §4.1 statements migrated to per-key locators — see summaries/4-2-password-strength.md)
- [x] green-playwright
- [x] demo (recording demo-activation-strength-4-2.webm)

### Scenario 4.3: Activation page shows error when passwords do not match
- [x] red-playwright (activation-mismatch.spec.ts §4.3 — password-mismatch-error testid + exact text)
- [x] red-frontend (password-match.logic.test.ts — evaluatePasswordMatch, 5 boundary cases)
- [x] green-frontend (evaluatePasswordMatch — equality → matched / "Passwords do not match")
- [S] red-frontend-api (no API — pure client-side comparison)
- [S] green-frontend-api
- [x] align-design (ActivationPage showMismatchError gating + .field-error; mockup 04 has no mismatch state)
- [x] green-playwright
- [x] demo (recording demo-activation-mismatch-4-3.webm)

> **Scenario 4.4 promoted 2026-06-21 from improvements.md I6 (user request):** the activate button is the last
> backend-calling auth control without a loading state (activation half of #189). align-design must extract a
> shared LoadingButton and migrate BOTH the §2.2 login and activate buttons onto it (§2.2 must stay green).

### Scenario 4.4: Activation page shows loading state during submission
- [x] red-playwright (activation-loading.spec.ts §4.4 — held-promise POST; activate-loading + fields disabled)
- [S] red-frontend (trivial-logic gate — presentational submitting ref, built in align-design)
- [S] green-frontend
- [S] red-frontend-api (wraps existing activateAccount() — no new HTTP surface)
- [S] green-frontend-api
- [x] align-design (extracted shared LoadingButton.vue; migrated §2.2 login + §4.4 activate buttons onto it)
- [x] green-playwright (regression gate — full login/activation dir 19/19, §2.2 stays green)
- [x] demo (recording demo-activation-loading-4-4.webm)

## Security Scenarios

### Scenario 5.1: SQL injection in login field does not bypass authentication
- [S] red-acceptance (L1 can't distinguish protected vs vulnerable; test removed — see summaries/5-1-sqli-login.md)
- [x] design (existing — Spring Data derived findByLogin, parameterized; no new code; no ADR)
- [S] red-usecase (lookup corner cases covered by AuthenticationServiceTest)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (db gets a SQLi literal-treatment regression test; no new ports)
- [x] red-adapter db (UserRepositorySqlInjectionTest — findByLogin tautology → empty + control; see summaries/5-1-sqli-login.md)
- [S] green-adapter db (existing derived query already satisfies — red+green collapse)
- [S] green-acceptance (property covered at db-adapter level)

### Scenario 5.2: Login rate limiting blocks after N failed attempts
- [x] red-acceptance (LoginRateLimitIntegrationTest — 5 wrong → 429 problem+json; @ExpectedToFail)
- [x] design (ADR: login-rate-limit-decision.md — LoginThrottle VO on User aggregate; TooManyLoginAttemptsException→429 config)
- [x] red-usecase (AuthenticationServiceTest.AuthenticateTest — 3 throttle corner cases; TooManyLoginAttemptsException + Clock)
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (db [S] active-persistence; rest [S] 429 config-only dup of L1; mapping in green-acceptance)
- [S] red-adapter rest (429 config-only — web slice duplicates L1)
- [S] green-adapter rest (config mapping applied in green-acceptance, atomic with @ExpectedToFail removal)
- [x] green-acceptance

### Scenario 5.3: Passwords are stored hashed
- [S] red-acceptance (L1 is HTTP black-box — can't observe stored hash; proven at Level-4 domain)
- [x] design (existing PasswordPolicy.hashPlain(); Level-4 strategy — see summaries/5-3-passwords-hashed.md)
- [S] red-usecase (hashPlain() exercised by activation 3.1/4.1)
- [S] green-usecase
- [x] red-domain (PasswordPolicyTest.StorageFormatTest — doesNotContain plaintext + startsWith "{bcrypt}$2a$" + salting guard; red+green collapse)
- [S] green-domain (existing hashPlain() already produces the salted hash)
- [S] adapters-discovery (feature already implemented — no new ports)
- [S] green-acceptance (proven at Level-4 domain)

### Scenario 5.4: Tampered JWT activation token is rejected
- [S] red-acceptance (JWT-signature error → L2 web slice + L4 domain; mirrors 2.2/3.2)
- [x] design (Option A config-only — SignatureException → 422 + detail "Invalid activation token"; no ADR)
- [S] red-usecase (activate() propagates SignatureException)
- [S] green-usecase
- [x] red-domain (JwtActivationTokenGeneratorTest — real-crypto: wrong-secret + tampered-resigned → SignatureException; red+green collapse)
- [S] green-domain (parseActivationClaim() already verifies signature)
- [x] adapters-discovery (rest [add] — SignatureException unmapped → 500; need 422 + detail)
- [x] red-adapter rest (AuthResourceTest.ActivateAccountTest tampered → 422; @ExpectedToFail; @Execution(SAME_THREAD) — see summaries/5-4-tampered-jwt.md)
- [x] green-adapter rest (application.yml SignatureException → 422 + "Invalid activation token" — see summaries/5-4-tampered-jwt.md)
- [S] green-acceptance (proven at L4 domain + L2 web slice)

### Scenario 5.5: Expired JWT activation token is rejected
- [S] red-acceptance (expired-token error → L2 web slice; mirrors 2.2/3.2/5.4)
- [x] design (Option A config-only — ExpiredJwtException detail "Activation token has expired"; no ADR)
- [S] red-usecase (activate() propagates ExpiredJwtException)
- [S] green-usecase
- [S] red-domain (token-expiry is jjwt's own .clock() check, not our logic)
- [S] green-domain
- [x] adapters-discovery (rest [add] — ExpiredJwtException mapped 422 but detail override missing)
- [x] red-adapter rest (AuthResourceTest.ActivateAccountTest expired → 422 + detail; @ExpectedToFail on detail field)
- [x] green-adapter rest (application.yml messages override; unified expiry detail with §2.2 GET — see summaries/5-5-expired-jwt.md)
- [S] green-acceptance (proven at L2 web slice)

### Scenario 5.6: POST /api/auth/activate without CSRF token returns 403
- [x] red-acceptance (ActivateAccountCsrfIntegrationTest — no-CSRF POST → 403 RFC 9457; red+green collapse)
- [x] design (existing — SecurityConfig .csrf(CsrfConfigurer::spa) + ProblemDetailAccessDeniedHandler; no ADR)
- [S] red-usecase (CSRF rejection is a security-filter concern — ActivationService never reached)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports — existing SecurityConfig + handler)
- [S] green-acceptance (red+green collapse, no marker)

### Scenario 5.7: Mass assignment on activate endpoint — extra fields ignored
- [x] red-acceptance (ActivateAccountMassAssignmentIntegrationTest — injected role/status dropped; full /me body exact; see summaries/5-7-mass-assignment.md)
- [x] design (existing — closed ActivateAccountRequest record + Jackson drops unknowns; no ADR)
- [S] red-usecase (structural at DTO/Jackson boundary)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports — closed record + Jackson default)
- [S] green-acceptance (red+green collapse, no marker)

### Scenario 5.8: Oversized password input rejected
- [S] red-acceptance (validation error → L2 web slice; @Size(min=12,max=128) already rejects)
- [x] design (existing — @Size declarative bean validation at web boundary; no ADR)
- [S] red-usecase (declarative @Size at DTO boundary)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (MAX boundary already covered by ActivateAccountRequestTest "too long" → [S], no new steps)
- [S] green-acceptance (proven at DTO bean-validation level)

## Load Scenarios

> ⏸ DEFERRED 2026-06-15 (user scope review) — premature optimization (hardware-coupled thresholds; needs a
> load-test harness the project lacks). Tracked in improvements.md → I2. Revisit in a perf story.

### Scenario 3.1: Login response time under 200ms
- [S] red-acceptance (deferred — improvements.md I2)
- [S] design
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [S] green-acceptance

### Scenario 4.1: Concurrent login requests complete under 500ms
- [S] red-acceptance (deferred — improvements.md I2)
- [S] design
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [S] green-acceptance

### Scenario 5.1: Activation token validation response time under 200ms
- [S] red-acceptance (deferred — improvements.md I2)
- [S] design
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [S] green-acceptance

## Infrastructure Scenarios

> ⏸ DEFERRED 2026-06-15 (user scope review) — require a stateful DB-outage harness (expensive, low value
> pre-production-traffic). Tracked in improvements.md → I3. Revisit in a resilience story.

### Scenario 4.1: Database unavailable during login returns 500
- [S] red-acceptance (deferred — improvements.md I3)
- [S] design
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [S] green-acceptance

### Scenario 5.1: Database recovery allows login after outage
- [S] red-acceptance (deferred — improvements.md I3)
- [S] design
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [S] green-acceptance
