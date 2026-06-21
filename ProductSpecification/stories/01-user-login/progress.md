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
- [x] demo (ran login-page.spec.ts §2.1 headed + slowMo; passed; config reverted)

### Scenario 3.1: Wrong credentials show error banner
- [x] red-playwright (login-page.spec.ts §3.1 — @skip; backend mock Statements return 401 problem+json; asserts error-banner exact text + fields cleared)
- [S] red-frontend (trivial-logic gate: no input-varying logic for a .logic.ts unit. Request build {login,password}→{login,password} is pass-through; banner message = server `detail` from 401 problem+json (backend owns the invalid-creds vs activation branching — frontend forwards `detail` unchanged, identity mapping); clearing fields on error = setting refs to '' (presentational reactive state in submit .catch, built in align-design). Any test would assert output≈input → fails post-impl trivial-test gate. Observable behavior covered by red-playwright E2E §3.1)
- [S] green-frontend (no logic produced in red-frontend — see [S] above; submit handler + error state built in align-design)
- [x] red-frontend-api (login.api.test.ts — @skip; MSW stubs POST /api/auth/login → 401 problem+json; asserts LoginError with exact `detail` "Invalid username or password". Added msw dep + src/test MSW lifecycle; login.api.ts stub + types.ts LoginRequest/LoginError)
- [x] green-frontend-api (login.api.ts login(): POST /api/auth/login {login,password} + credentials:'include'; non-2xx → throw LoginError(problem.detail); 2xx → resolve; skip marker removed; 1/1 auth + 2/2 suite pass)
- [x] align-design (LoginPage.vue wired to login() API client: v-model on inputs, @submit.prevent submitLogin, error-banner testid with XCircle + {{errorMessage}} from server detail, clears fields on LoginError via showLoginError(); banner matches mockup 02-login-error-credentials.html; design-review PASS — no hardcoded placeholders; coverage clean)
- [x] green-playwright (login-page.spec.ts §3.1 passes — skip marker removed; backend mocked in-browser via page.route 401; 3/3 login spec green, no regressions)
- [x] demo (ran login-page.spec.ts §3.1 headed + slowMo 1200ms; passed; config reverted)

### Scenario 3.2: Inactive account shows error banner with activation message
- [x] red-playwright (login-page.spec.ts §3.2 — @skip; backend mock givenInactiveUser returns 401 "Account not activated" problem+json; asserts error-banner contains activation message + activation-link anchor present)
- [S] red-frontend (trivial-logic gate: no .logic.ts logic. The activation-required signal is response-mapping of the problem+json `type` (.../authentication-failed vs .../bad-credentials) into LoginError — the API client's job per Humble Object, tested in red-frontend-api; no login.logic.ts exists and creating one would duplicate the API mapping. Conditional activation-link render is presentational `v-if`, built in align-design. Zero logic-layer production files change. Mirrors §3.1)
- [S] green-frontend (no logic produced in red-frontend — see [S] above; conditional link built in align-design)
- [x] red-frontend-api (login.api.test.ts §3.2 — it.skip; MSW stubs not-activated 401 type=authentication-failed → asserts LoginError.requiresActivation===true & message "Account not activated"; invalid-creds test updated to real bad-credentials type + asserts requiresActivation===false. LoginError gained requiresActivation field; login.api.ts stub passes false)
- [x] green-frontend-api (login.api.ts maps problem.type===.../authentication-failed → requiresActivation:true via AUTHENTICATION_FAILED_TYPE const; not-activated test un-skipped; 2/2 pass)
- [x] align-design (LoginPage.vue wired requiresActivation from LoginError; error banner extracted to LoginErrorBanner.vue — conditional AlertTriangle (activation) vs XCircle icon + activation-link anchor "Request a new activation email"; banner matches mockup 03-login-error-inactive.html (items-start, leading-[1.4], alert-triangle); design-review PASS — no placeholders; coverage clean — components E2E-covered)
- [x] green-playwright (login-page.spec.ts §3.2 passes — skip marker removed; backend mocked in-browser via page.route 401 "Account not activated"; activation-link anchor + alert-triangle banner rendered; 4/4 login spec green, no regressions)
- [x] demo (ran login-page.spec.ts §3.2 headed + slowMo 1200ms; passed; config reverted)

### Scenario 4.1: Activation page shows password fields and complexity rules
- [x] red-playwright (activation-page.spec.ts §4.1 — @skip; navigate /activate?token=...; asserts password+confirm fields visible & masked, exactly 6 complexity rules with exact texts, "Activate Account" button text)
- [S] red-frontend (trivial-logic gate: pure page-display scenario — Gherkin has no When, no input varies output. Field visibility, password masking via type=password, the fixed 6-item complexity-rules list, and the fixed "Activate Account" label are presentational constants rendered in the activation component during align-design. No .logic.ts logic exists or is needed: password validation against the rules is Scenario 5.1, token validation is a separate API concern. Existence check: no activation logic file exists; zero logic-layer production files change. Display visibility covered by red-playwright E2E §4.1. Mirrors scenarios 1.1/2.1/3.1/3.2 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; static complexity rules + fields + button built in the activation component during align-design)
- [x] red-frontend-api (activation.api.test.ts — it.skip; MSW stubs GET /api/auth/activate?token=valid → 200 {login,email}; asserts validateActivationToken resolves toEqual {login:'iivanov',email:'ivan@example.com'} + request path + token query param. activation.api.ts stub (not-impl marker); types.ts ActivationTokenResponse {login,email})
- [x] green-frontend-api (activation.api.ts validateActivationToken(): GET /api/auth/activate?token= + credentials:'include', returns {login,email}; skip marker removed; 1/1 target + 3/3 auth suite pass)
- [x] align-design (ActivationPage.vue + /activate route built, matches mockup 04-activation-form.html; validate-on-load: onMounted calls validateActivationToken(token from query) to populate dynamic "For account {login} ({email})" subtitle, form always renders regardless of result so 4.1 display passes without backend stub — expired-token error view deferred to Scenario 5.2; PasswordField.vue extracted (input+visibility toggle, reused for both password fields); 6 static complexity rules; design-review PASS — no hardcoded placeholders; coverage clean — components E2E-covered, activation.api.ts happy path 100%)
- [x] green-playwright (activation-page.spec.ts §4.1 passes — skip marker removed; frontend-only via Playwright webServer, no backend needed (validate-on-load fetch rejects → account null → form renders); 1/1 activation + 5/5 full login spec dir green, no regressions)
- [x] demo (ran activation-page.spec.ts §4.1 headed + slowMo 1200ms maximized; 1 passed; config reverted)

### Scenario 5.1: Successful activation shows success message
> ✅ Origin-gate РЕШЁН (2026-06-06): dev = Vite-proxy (same-origin), relative `/api` URLs, без CORS. См. activation-flow.md → "РЕШЕНО (Сценарий 5.1)". При реализации POST: BASE_URL → '' + GET /api/auth/csrf → POST с X-XSRF-TOKEN + credentials:'include'.
- [x] red-playwright (activation-page.spec.ts §5.1 — test.skip; backend mock Statements via page.route: GET /api/auth/csrf sets XSRF-TOKEN, GET /api/auth/activate→200 {login,email}, POST /api/auth/activate→200; asserts success screen — green check icon visible+non-empty SVG, exact "Account Activated!" text, "Go to Sign In" button; new testids activation-success/-icon/-title + go-to-sign-in-button for align-design)
- [S] red-frontend (trivial-logic gate: no input-varying .logic.ts logic in §5.1's happy path. Building the activate request {token, password} from the route token + password ref is an identity pass-through (no rename/filter/default/computation → trivial); the GET /api/auth/csrf → POST /api/auth/activate orchestration with X-XSRF-TOKEN + credentials:'include' is API-client concern, tested in red-frontend-api; the success screen swap — green check icon + "Account Activated!" + "Go to Sign In" — is presentational reactive state set in the submit .then(), built in align-design; password-match/complexity validation is not exercised by §5.1 (a valid password entered identically → no branching). Any test would assert output≈input → fails the post-impl trivial-test gate. Observable behavior covered by red-playwright E2E §5.1. Mirrors scenarios 1.1/2.1/3.1/3.2/4.1 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; submit handler + CSRF/POST call + success-screen state built in align-design wiring the red-frontend-api client)
- [x] red-frontend-api (activate-account.api.test.ts — it.skip; stubs GET /api/auth/csrf (sets XSRF-TOKEN cookie via document.cookie) + POST /api/auth/activate capturing request; asserts POST path /api/auth/activate, X-XSRF-TOKEN header == cookie value, body toEqual {token,password}. activation.api.ts activateAccount(token,password) not-impl stub. PREDICT Error "Not implemented" matched actual — type+message+status all YES)
- [x] green-frontend-api (activation.api.ts activateAccount(): GET /api/auth/csrf credentials:'include' → readCookie('XSRF-TOKEN') helper → POST /api/auth/activate with Content-Type:application/json + X-XSRF-TOKEN header + credentials:'include' + body JSON {token,password}; void placeholders removed; skip marker removed; 1/1 target + 4/4 auth suite pass; tsc clean)
- [x] align-design (ActivationSuccess.vue built + rendered by ActivationPage when activated=true; @submit.prevent submitActivation calls activateAccount(token,password) then sets activated; success screen matches mockup 05-activation-success.html — CheckCircle2 green icon (#40c057) + "Account Activated!" title + description + "Go to Sign In" button (router.push('/login')); testids activation-success/-icon/-title + go-to-sign-in-button. Refactor extracted .auth-card + .btn-primary to style.css (deduped across LoginPage/ActivationPage/ActivationSuccess). design-review PASS — no placeholders; coverage clean — components E2E-covered, activateAccount happy path covered by unit test)
- [x] green-playwright (skip marker removed; success screen E2E-verified — backend mocked in-browser via page.route (csrf cookie + GET/POST activate). Prereq fix (committed separately): §5.1 Statements `assertSuccessIconIsVisible` line 76 misused Playwright `not.toBeEmpty()` on the SVG-only icon wrapper (treats no-text element as empty → can never pass); corrected to `expect(successIcon().locator('svg > *').first()).toBeAttached()`, test-review CLEAN. activation-page.spec.ts 2/2 pass; full login dir 6/6 pass, no regressions)
- [x] demo (ran activation-page.spec.ts §5.1 headed + slowMo 1200ms maximized; 1 passed; config reverted)

### Scenario 5.2: Expired token shows error message
- [x] red-playwright (activation-page.spec.ts §5.2 — test.skip; backend mock givenExpiredToken returns GET /api/auth/activate 422 problem+json; asserts error view — red X icon visible+non-empty SVG, exact "Link Expired" text, "Request New Link" button; new testids activation-error/-icon/-title + request-new-link-button for align-design. RED confirmed: error view absent in ActivationPage.vue. Refactor extracted shared assertScreenIconIsVisible helper deduping success/error icon checks; test-review CLEAN)
- [S] red-frontend (trivial-logic gate: no input-varying .logic.ts logic. The expired/invalid-token determination is an HTTP-status concern — validateActivationToken must reject on non-2xx, an API-client job tested in red-frontend-api; no activation.logic.ts exists and creating one would duplicate the API mapping. The form→error-view swap is presentational reactive state set in the component's loadAccount catch (built in align-design). Zero logic-layer production files change. Mirrors scenarios 1.1/2.1/3.1/3.2/4.1/5.1 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; error-view state + conditional render built in align-design wiring the red-frontend-api client)
- [x] red-frontend-api (activation.api.test.ts — it.skip; MSW stubActivateExpired returns GET /api/auth/activate 422 application/problem+json {status,title,detail:"Token expired",instance}; asserts validateActivationToken('expired-token') rejects with ActivationError and exact message "Token expired". types.ts gained minimal ActivationError extends Error (plumbing). PREDICT: promise resolves instead of rejecting (no response.ok check) — matched: type+message+status all YES. test-review tightened to assert exact message; refactor extracted captureActivationRejection helper mirroring login.api.test. 1 passed | 1 skipped)
- [x] green-frontend-api (activation.api.ts validateActivationToken: added `if (!response.ok)` guard parsing problem+json and throwing new ActivationError(problem.detail); 2xx path unchanged. skip marker removed. Refactor pulled up shared ProblemDetail type into types.ts (deduped login.api.ts/activation.api.ts). activation.api.test.ts 2/2; full auth suite 5/5; vue-tsc clean)
- [x] align-design (ActivationExpired.vue built + rendered by ActivationPage when tokenInvalid=true; loadAccount catch sets tokenInvalid only on `instanceof ActivationError` (preserves 4.1: network/parse errors keep the form). Matches mockup 06-activation-error.html — XCircle red icon (#fa5252) + "Link Expired" title + static description + "Request New Link" button (router.push('/login')); testids activation-error/-icon/-title + request-new-link-button. Refactor extracted shared ActivationResultCard.vue (deduped success/error result views — both now thin config; testids preserved). design-review PASS — no placeholders; coverage CLEAN — §5.2 logic 100% (new throw branch covered), components E2E-covered. vue-tsc + lint clean; auth suite 5/5)
- [x] green-playwright (activation-page.spec.ts §5.2 passes — skip marker removed; error view E2E-verified, backend mocked in-browser via page.route 422; 3/3 activation spec green, no regressions)
- [x] demo (ran activation-page.spec.ts §5.2 headed + slowMo 1200ms maximized; 1 passed; config reverted)

### Scenario 6.1: Clicking "Go to Sign In" navigates to login page
- [x] red-playwright (activation-page.spec.ts §6.1 — test.skip; reaches success screen via §5.1 backend-mock flow then clicks go-to-sign-in-button, asserts exact /login URL + login page elements visible/masked. PREDICT: test passes — router.push('/login') already wired in §5.1; matched. Refactor extracted completeActivationAndReachSuccessScreen() precondition; test-review tightened URL to exact + added password-masking assertion)
- [S] red-frontend (trivial-logic gate + existence check: clicking "Go to Sign In" is an unconditional router navigation `router.push('/login')` — no branching, computation, validation, or data transformation for a .logic.ts unit. Handler already exists in ActivationSuccess.vue (goToSignIn() → router.push('/login'), wired §5.1 align-design); zero .logic.ts files exist and a logic function here would be a constant pass-through. A test would assert router.push called with hardcoded '/login' → identity/constant assertion forbidden by the post-impl trivial-test gate. Observable behavior covered by red-playwright §6.1. Zero logic-layer production files change. Mirrors scenarios 1.1/2.1/3.1/3.2/4.1/5.1/5.2 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; navigation handler already built in ActivationSuccess.vue during §5.1 align-design)
- [S] red-frontend-api (no API/HTTP interaction in §6.1 — the action is a pure client-side router navigation router.push('/login') with zero network requests; nothing for an MSW .api.ts test to stub/assert. Existence check: all auth HTTP already built/tested in prior scenarios — login.api.ts login() (§3.1/3.2), activation.api.ts validateActivationToken() (§4.1), activateAccount() (§5.1) — §6.1 triggers none. goToSignIn()→router.push('/login') handler already in ActivationSuccess.vue (§5.1 align-design). Zero .api.ts production files change. Observable behavior covered by red-playwright §6.1. Mirrors scenarios 1.1/2.1/5.2 red-frontend-api [S] and §6.1 red-frontend [S])
- [S] green-frontend-api (no API client produced in red-frontend-api — see [S] above; navigation already built in ActivationSuccess.vue during §5.1 align-design)
- [x] align-design (verification-only — §6.1 is navigation between two already-aligned screens: success screen button lives in ActivationSuccess.vue→ActivationResultCard.vue (btn-primary matches mockup 05-activation-success.html .btn, aligned §5.1), navigation target LoginPage.vue (aligned §1.1/§3.x). Zero component changes; working tree clean. vue-tsc clean; vitest 6/6 green. design-review PASS — no placeholders, no regressions. refactor CLEAN — no smells, all files ≤87L. test-coverage --focus: no changed production files to analyze; navigation E2E-covered by red-playwright §6.1)
- [x] green-playwright (skip marker removed — navigation E2E-verified; backend mocked in-browser via page.route, frontend auto-started via Playwright webServer. activation-page.spec.ts 4/4 pass (§4.1/§5.1/§5.2/§6.1), no regressions. Remove-marker-only: no production/Statements changes)
- [x] demo (ran activation-page.spec.ts §6.1 headed + slowMo 1200ms maximized; 1 passed; config reverted. Note: demo config needs deviceScaleFactor:undefined alongside viewport:null in the chromium project — Desktop Chrome device sets deviceScaleFactor which conflicts with null viewport)

> **Promoted 2026-06-20 (issue #189) from `tests/extended/02_UI_Tests_Extended.md`.** These four
> extended UI cases were deferred-by-design and never tracked (FE audit `audits/2026-06-20-frontend-audit.md`,
> improvements.md I4). Now first-class core scenarios 2.2/3.3/4.2/4.3 (see `tests/02_UI_Tests.md`).
> Dependency note: #191 (client validation library — zod) is MERGED, so the real-time strength (4.2)
> and password-mismatch (4.3) scenarios are unblocked. The dead "Request a new activation email"
> link (href="#") is NOT in scope here — it needs a resend-activation feature that does not exist;
> tracked separately in improvements.md I5.

### Scenario 2.2: Login page shows loading state during submission
- [x] red-playwright (login-loading.spec.ts §2.2 — test.fail() RED marker; new spec split from login-page.spec.ts to stay ≤200L. Backend mock givenSlowLoginRequest holds POST /api/auth/login via a deferred promise so the in-flight loading state is observable — no sleeps; whileSlowLoginIsHeld(action) owns the held-route lifecycle + guaranteed release. LoginLoadingStatements asserts all 3 DSL consequences: submit-loading indicator visible + login/password inputs disabled. RED genuine: LoginPage.vue has no `submitting` state, no submit-loading testid, no :disabled bindings. PREDICT toBeVisible timeout on submit-loading — matched (Type/Message/Status all YES). New testid for align-design: submit-loading. test-review: wrapped release in try/finally (clean otherwise). refactor: Extract Method assertLoadingStateIsActive() + encapsulate held-route in whileSlowLoginIsHeld(). CLI lint clean (eslint/oxlint/prettier/vue-tsc EXIT=0); IDE clean except SonarLint S2068 FP on the password-input testid — already suppressed via // noinspection HardcodedPasswordInspection mirroring login-page.statements.ts, IDE-only not a CI gate)
- [S] red-frontend (trivial-logic gate: no input-varying .logic.ts logic. The loading state is a presentational reactive `submitting` ref in LoginPage.vue — set true before `await login(...)`, false in a `finally`; the submit button shows a spinner and the login/password inputs bind `:disabled="submitting"` while it is true. A hypothetical loading-state logic function would just return the ref (constant/identity pass-through) → forbidden by the post-impl trivial-test gate. No request build/validation/transformation is added (request {login,password} is pass-through, §3.1 [S]; validation `isLoginFormValid` already exists/tested). Existence check: login-form.logic.ts + login.api.ts already exist; no loading-state logic file exists and creating one would be trivial. Zero logic-layer production files change. Observable behavior — button indicator visible + login/password fields disabled during the held in-flight request — covered by red-playwright E2E §2.2; submitting ref + :disabled bindings + spinner built in align-design. Mirrors scenarios 1.1/2.1/3.1/3.2/5.1 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; submitting ref + button spinner + :disabled bindings built in align-design)
- [S] red-frontend-api (no new .api.ts HTTP surface in §2.2 — the loading state wraps the EXISTING login() API client (login.api.ts: CSRF handshake → POST /api/auth/login → problem+json→LoginError mapping, built/tested in scenarios 3.1/3.2). §2.2 adds a presentational `submitting` ref set true before `await login(...)` and false in a finally, a button spinner, and `:disabled="submitting"` on the login/password inputs — no new endpoint, no new request shape, no new response/error mapping. Existence check: login() already exists and is comprehensively tested by login.api.test.ts (4 tests). Zero .api.ts production files change. Observable loading behavior covered by red-playwright E2E §2.2 (login-loading.spec.ts: submit-loading indicator + disabled inputs while a deferred-promise route holds POST /api/auth/login); submitting ref + spinner + :disabled bindings built in align-design. Mirrors scenarios 1.1/2.1/5.2/6.1 red-frontend-api [S].)
- [S] green-frontend-api (no API client produced in red-frontend-api — see [S] above; the loading state reuses the existing login() client, the submitting ref + button spinner + :disabled bindings are built in align-design)
- [x] align-design (LoginPage.vue: added `submitting` ref toggled true→finally-false around `await login(...)`; submit button shows a LoaderCircle spinner (data-testid submit-loading, animate-spin, aria-hidden) + "Signing In…" label while submitting, disabled via `!isFormValid || submitting` + aria-busy; login input + PasswordField bound `:disabled="submitting"`. PasswordField.vue gained an optional `disabled` prop wired to the input + toggle button. style.css `.form-input` gained `disabled:` utilities (cursor-not-allowed + bg-surface + opacity-60), consistent with the existing `.btn-primary` disabled tokens. No loading-state mockup exists — idle login state is visually unchanged (spinner v-if-hidden, flex centering layout-neutral for the single text node) and still matches mockup 01-login.html (aligned in §1.1/§3.x). design-review PASS — only new literals are static UI labels + submit-loading testid, no placeholder/user data. refactor CLEAN — files ≤98L, idiomatic, LoaderCircle via @lucide/vue (no inline SVG); noted a future shared LoadingButton if ActivationPage gets a loading state (out of scope). test-coverage frontend --focus: components E2E-covered by login-loading.spec.ts §2.2, no logic/api gaps, no new steps (27 vitest passed). CLI lint EXIT=0 (oxlint/eslint/prettier/vue-tsc); IDE inspections clean on all 3 files.)
- [x] green-playwright (login-loading.spec.ts §2.2 passes — test.fail() marker + stale RED comment removed; feature in place from align-design. Frontend auto-started via Playwright webServer, login page.route-mocked (no backend). 1/1 spec; full login dir 15/15, no regressions. Remove-marker-only: no production/Statements changes. CLI lint + IDE clean.)
- [x] demo (ran login-loading.spec.ts §2.2 headless + slowMo 2000ms + video; 1 passed; recording test-results/demo-login-loading-2-2.webm; config reverted, tree clean)

### Scenario 3.3: Error banner dismiss button closes the banner
- [x] red-playwright (login-error-dismiss.spec.ts §3.3 — test.fail() RED marker; new spec (login-page.spec.ts already 177L). Given: givenErrorBannerIsVisible reuses authBackend.givenRegisteredUser + submits wrong-pass → 401 → error-banner visible (precondition asserted). When: clickDismissButton clicks new error-banner-dismiss testid. Then: error-banner toHaveCount(0). RED genuine: LoginErrorBanner.vue has no dismiss button. PREDICT toBeVisible on missing error-banner-dismiss → matched (Type/Message/Status all YES). First run used bare .click() → 30s action timeout (hard fail under test.fail); fixed to pin RED via toBeVisible() expect (5s), mirroring §2.2. New testid for align-design: error-banner-dismiss (button emits dismiss → parent clears errorMessage). test-review CLEAN. refactor: Extract givenErrorBannerIsVisible precondition (matches Gherkin abstraction, removes impl constants from test class); added noinspection HardcodedPasswordInspection for new password-input testid. CLI lint EXIT=0; IDE clean. spec 32L / statements 54L.)
- [S] red-frontend (trivial-logic gate: no input-varying .logic.ts logic. Dismissing the error banner is presentational reactive state-clearing — the banner shows via `v-if="errorMessage"` in LoginPage.vue (line 47); the dismiss handler just resets errorMessage/requiresActivation/fieldErrors refs to ''/false/{} so the v-if hides it. No branching, computation, validation, or transformation. A hypothetical "clear error" logic fn would return constants {errorMessage:'',requiresActivation:false,fieldErrors:{}} → forbidden by the post-impl trivial-test gate. Existence check: no clear/dismiss logic file exists in auth/logic/; the error→view mapping (mapLoginErrorToView) already exists/tested in login-error-view.logic.test.ts and transforms nothing here. Mechanism: LoginErrorBanner.vue gains a dismiss button (error-banner-dismiss testid) emitting `dismiss`; LoginPage.vue @dismiss clears the refs — built in align-design. Zero logic-layer production files change. Observable behavior covered by red-playwright E2E §3.3 (login-error-dismiss.spec.ts). Mirrors scenarios 1.1/2.1/3.1/3.2/5.1/2.2 red-frontend [S])
- [S] green-frontend (no logic produced in red-frontend — see [S] above; dismiss button emit + parent ref-clearing built in align-design on LoginErrorBanner.vue + LoginPage.vue)
- [S] red-frontend-api (no new .api.ts HTTP surface in §3.3 — dismissing the error banner is a pure client-side state reset with ZERO network requests: the banner shows via `v-if="errorMessage"` in LoginPage.vue (line 47); the dismiss button emits `dismiss`, the parent clears the errorMessage/requiresActivation/fieldErrors refs to ''/false/{} so the v-if hides it. No endpoint is hit, no request is built, no response/error mapping occurs — nothing for an MSW .api.ts test to stub or assert. Existence check: all auth HTTP is already built/tested in prior scenarios — login.api.ts login() (§3.1/3.2), activation.api.ts validateActivationToken() (§4.1) + activateAccount() (§5.1); §3.3 triggers none of them, and grep over frontend/src for "dismiss" finds no API code. Zero .api.ts production files change (login.api.ts/activation.api.ts/csrf.ts untouched). Mechanism — dismiss button (error-banner-dismiss testid) on LoginErrorBanner.vue + @dismiss ref-clearing on LoginPage.vue — built in align-design. Observable behavior covered by red-playwright E2E §3.3 (login-error-dismiss.spec.ts). Mirrors scenarios 1.1/2.1/5.2/6.1/2.2 red-frontend-api [S] and §3.3 red-frontend [S].)
- [S] green-frontend-api (no API client produced in red-frontend-api — see [S] above; dismissing reuses no HTTP, the dismiss button emit + parent ref-clearing are built in align-design on LoginErrorBanner.vue + LoginPage.vue)
- [x] align-design (LoginErrorBanner.vue: added a dismiss button — Lucide X icon, data-testid error-banner-dismiss, aria-label "Dismiss error", ml-auto on the right of the banner — emitting `dismiss` via defineEmits. LoginPage.vue: `@dismiss="dismissError"` on LoginErrorBanner + dismissError() clearing errorMessage/requiresActivation/fieldErrors so the v-if=errorMessage banner hides. No dismiss button in the error mockups (02/03 show only the leading x-circle icon) — net-new presentational element using existing danger tokens; the rest of the banner (icon/message/activation link) unchanged vs mockups. design-review PASS — only new literals are the static aria-label + testid, message stays a dynamic prop. refactor extracted a shared `.icon-button` semantic class in style.css (deduped the bare-icon-button reset shared with PasswordField.vue's toggle). test-coverage frontend --focus: components E2E-covered by login-error-dismiss.spec.ts §3.3, logic/ 100% lines, no logic/api gaps, no new steps (27 vitest passed). CLI lint EXIT=0; IDE inspections clean on all 4 files.)
- [x] green-playwright (login-error-dismiss.spec.ts §3.3 passes — test.fail() marker + stale RED comment removed; dismiss button built in align-design. Frontend auto-started via Playwright webServer, backend mocked in-browser via page.route (no real backend). 1/1 spec; full login dir 16/16, no regressions. Remove-marker-only: no production/Statements changes. CLI lint EXIT=0; IDE clean.)
- [x] demo (ran login-error-dismiss.spec.ts §3.3 headless + slowMo 2000ms + video; 1 passed; recording test-results/demo-login-error-dismiss-3-3.webm; config reverted, tree clean)

> **§4.2 REDESIGNED 2026-06-21 (user direction during align-design):** the original contract — an
> AGGREGATE indicator `data-testid="password-strength"` with `data-strength="weak"/"medium"/"strong"`
> (computed by `computePasswordStrength`) — did NOT match mockup `04-activation-form.html`, which shows
> PER-RULE highlighting: each satisfied complexity rule turns green (`.rule.met`), unsatisfied rules stay
> grey; there is no aggregate weak/medium/strong widget. The mockup is the design source of truth, so the
> frontend cycle is reopened under a PER-RULE contract: logic `evaluateComplexityRules(password): ComplexityRule[]`
> ({key,label,met} per rule); each rule rendered as `data-testid="complexity-rule-{key}"` + `data-met="true|false"`
> (attribute, not colour/class → refactor-stable); met rule = `text-success`, unmet = `text-muted` via CSS on
> `[data-met]`. Gherkin §4.2 + the UI-matrix row updated to the per-rule model. The prior data-strength
> contract (commits a660043 red-playwright / 01dbda7 red-frontend / 3f19741 green-frontend) is SUPERSEDED;
> `computePasswordStrength` + its test are replaced in the reopened red-frontend/green-frontend steps.

### Scenario 4.2: Activation page shows password strength indicator updating in real-time
- [x] red-playwright (PER-RULE redesign — rewrote activation-strength.spec.ts + activation-strength.statements.ts: each of the 6 complexity rules renders as data-testid="complexity-rule-{key}" (length/uppercase/lowercase/digit/special/no-spaces) with data-met="true|false" (attribute, refactor-stable). Types "weak" → asserts lowercase+no-spaces data-met=true, length/uppercase/digit/special data-met=false; updates to "Str0ng-P@ssw0rd!" → all 6 data-met=true in real-time (exact toHaveAttribute, no sleeps). Backend mock reused (ActivationBackendStatements.givenPendingAccountForToken validate-on-load GET). test.fail() RED marker; PREDICT toHaveAttribute timeout on missing complexity-rule-lowercase → matched (Type/Message/Status all YES). Supersedes the prior data-strength version (a660043). test-review CLEAN (strict per-rule asserts, 2-tier DSL pure, testid-only, all 6 rules covered). refactor: extracted DSL methods (typePartiallySatisfyingPassword/assertOnlySatisfiedRulesAreMet/...) + RULE_KEY/PARTIAL_MET_KEYS constants; spec 38L / statements 89L. lint EXIT=0; IDE clean; 1 expected-fail passed.)
- [x] red-frontend (PER-RULE redesign — replaced aggregate computePasswordStrength/PasswordStrength with the per-rule ComplexityRule interface {key,label,met} + evaluateComplexityRules(password): ComplexityRule[] stub (throws not-implemented) in password-strength.logic.ts; rewrote password-strength.logic.test.ts: 3 it.fails() whole-array toEqual cases for 'weak' (lowercase+no-spaces met only), 'Str0ng-P@ssw0rd!' (all 6 met), '' (no-spaces met only). Rules in order length/uppercase/lowercase/digit/special/no-spaces with labels = the former ActivationPage PASSWORD_RULES (logic now the single source of truth for labels → align-design consumes them). No issue('189') tag (story scenario). PREDICT Error 'not implemented' → matched (Type/Message/Status all YES). test-review CLEAN (strict toEqual, literal expected arrays, met-flags hand-verified). refactor: hoisted rule() builder to module scope + renamed unused stub param→_password (lint); CLEAN otherwise. lint EXIT=0; IDE clean; 3 expected-fail passed. COVERAGE FOLLOW-UP (user-requested): converted the 3 cases to a parametrized it.fails.each table (7 cases) closing predicate-boundary gaps a single representative password missed — both met/unmet states of all 6 predicates, the length 11/12 boundary, and critically a no-spaces=false case (password with a space, never exercised before). expected(metFlags) helper zips fixed RULES {key,label} with literal per-row flags (no production mirroring); whole-array toEqual. test-review CLEAN again; lint EXIT=0; IDE clean; 7 expected-fail.)
- [x] green-frontend (implemented evaluateComplexityRules in password-strength.logic.ts — COMPLEXITY_RULES const array of {key,label,predicate} mapped to {key,label,met:predicate(password)}; 6 rules in order length(≥12)/uppercase(/[A-Z]/)/lowercase(/[a-z]/)/digit(/\d/)/special(/[^A-Za-z0-9\s]/)/no-spaces(!/\s/); labels match the test contract exactly. it.fails.each→it.each (only allowed test change). No aggregate level. refactor CLEAN (data+behavior co-located, 28L). lint EXIT=0; IDE clean; target 7/7 passed; full auth suite 30 passed, 0 fail, 0 skip.)
- [S] red-frontend-api (no new .api.ts HTTP surface in §4.2 — per-rule complexity evaluation is still computed purely client-side from the typed password value with ZERO network requests; the feature is the pure synchronous evaluateComplexityRules() logic fn, no fetch/HTTP. No endpoint, request build, or response/error mapping for an MSW .api.ts test to stub or assert. Existence check: all auth HTTP already built/tested in prior scenarios — login.api.ts login() (§3.1/3.2), activation.api.ts validateActivationToken() (§4.1) + activateAccount()+CSRF (§5.1); §4.2 triggers none. Zero .api.ts production files change. Observable behavior covered by red-playwright E2E §4.2 + the pure logic by its Vitest test. Per-rule rendering built in align-design. Mirrors scenarios 1.1/2.1/5.2/6.1/2.2/3.3 red-frontend-api [S]. Unchanged by the redesign — per-rule is as network-free as the aggregate version was.)
- [S] green-frontend-api (no API client produced in red-frontend-api — see [S] above; per-rule rendering reuses no HTTP, the data-met binding to evaluateComplexityRules is built in align-design)
- [x] align-design (ActivationPage.vue wired the complexity-rules block to a `complexityRules` computed = `evaluateComplexityRules(password.value)` (real-time per keystroke); each rule renders as `:data-testid="complexity-rule-{key}"` + `:data-met="rule.met"`, label from the logic source of truth. Styling matches mockup 04-activation-form.html `.rule`/`.rule.met`: new `.complexity-rule` class in style.css (mb-1 flex items-center gap-2 text-[13px]) with attribute-driven color — `[data-met='false']`→text-muted (#6c757d), `[data-met='true']`→text-success (#40c057); Check icon `:size="16"` inherits currentColor (grey unmet / green met). DECISION — testid unification: the redesign removed the legacy aggregate `data-testid="password-complexity-rule"`; §4.1 (already green) located rules by that id for count+exact-text. One element can't carry two data-testids, so rather than nest a redundant wrapper div purely for the legacy id, §4.1's activation-page.statements.ts was migrated onto the same per-key model (a `rule(key)` locator + loop asserting each of the 6 rules' exact text) — single source of truth, matching green-frontend's "logic owns the labels". design-review PASS (no placeholder leaks — account subtitle stays dynamic from API, rule met-states computed from typed password, mockup's "MyStr0ng"/pre-highlighted rules not copied). refactor CLEAN (all files ≤200L: component 104L, style.css 72L, statements 197L; attribute-driven styling contract preserved, not collapsed to a ternary class). test-coverage frontend --focus CLEAN — evaluateComplexityRules 100% (7 parametrized cases, unchanged), component E2E-covered by §4.2 activation-strength.spec.ts, CSS not tracked; no new logic branches, no steps added. CLI lint EXIT=0 (oxlint/eslint/prettier/vue-tsc); IDE inspections clean on all 3 files; vitest 34/34. §4.1 green-playwright re-verification (the migrated statements) lands in the next §4.2 green-playwright unit, which runs the full activation spec dir.)
- [~] green-playwright
- [ ] demo

### Scenario 4.3: Activation page shows error when passwords do not match
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

> **Scenario 4.4 promoted 2026-06-21 from improvements.md I6 (user request).** The activation activate
> button is the only backend-calling auth control still without a loading state — the activation half
> of the #189 double-submit finding (login half closed by §2.2). NOT in the extended UI spec (under-
> specified by design); promoted as a new core scenario so it is tracked, scheduled LAST after the
> other promoted scenarios. align-design should **extract a shared LoadingButton** (per the new
> `.claude/rules/frontend-rules.md` "Async Action Buttons" rule) and migrate BOTH the §2.2 login button
> and the activate button onto it (§2.2 login tests must stay green through that refactor).

### Scenario 4.4: Activation page shows loading state during submission
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
- [S] red-acceptance (acceptance test REMOVED after review — Level 1 can't distinguish protected vs vulnerable: login=`x' OR '1'='1` simply has no matching user → 401, identical to any nonexistent login. Worse, in this architecture login-field injection can NEVER bypass auth regardless of parameterization, because the password is verified separately by BCrypt after findByLogin (RpmUserDetailsService). The original LoginSqlInjectionIntegrationTest (401 + no JSESSIONID) proved nothing. The literal-treatment property is now proven directly at the db-adapter level — see red-adapter db below.)
- [x] design (existing implementation — SQL injection prevented structurally by Spring Data derived query findByLogin(Login): no @Query/native SQL/concatenation, value bound as parameterized criteria; Login value-object boundary. Password never touches SQL (in-memory BCrypt). No new code. Approved "confirm existing", no ADR — single viable approach, no trade-off.)
- [S] red-usecase (no new usecase logic — login lookup corner cases already tested by AuthenticationServiceTest)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (db adapter gets a security-regression test for SQLi literal-treatment — see red-adapter db; no new ports/exceptions/response shapes; rest adapter unchanged)
- [x] red-adapter db (UserRepositorySqlInjectionTest — @DataJpaTest + @DbTest + replace=NONE; project's first repository test. Asserts findByLogin(new Login("admin' OR '1'='1")) → empty (tautology bound as literal parameter, no row), with a control assert that findByLogin("admin") finds the seeded user — proving the empty result is literal-matching, not an empty DB. Deliberate exception to the "skip derived-query db tests" template rule: this is a security-intent regression guard, not a query-correctness test. Passes immediately — derived query is already parameterized; if someone replaced it with a concatenated native query the tautology would match admin and this test would fail. 2/2 GREEN, PostgreSQL via shared test DB. checkstyle/pmd clean.)
- [S] green-adapter db (no production change — the existing derived query already satisfies the test; red+green collapse for an already-correct implementation, like the prior "already passes" scenarios)
- [S] green-acceptance (no acceptance test — security property covered at db-adapter level)

### Scenario 5.2: Login rate limiting blocks after N failed attempts
- [x] red-acceptance (LoginRateLimitIntegrationTest + LoginRateLimitStatements — genuine RED, feature unimplemented. 5 wrong-password logins for seeded admin → asserts 5th is 429 + application/problem+json (too-many-login-attempts), then correct password also 429 (locked within window). No clock manipulation (window doesn't elapse). PREDICT 429-expected-but-401-actual matched exactly (Type/Status/Message all YES); @ExpectedToFail(withExceptions=AssertionError.class) added. 5-attempt loop + LOCKOUT_THRESHOLD=5 + status/cred literals in Statements; test class pure DSL. test-review added .assertProblemJson() content-type check (RFC 9457) at both sites + reusable AssertionResponse.assertContentType/assertProblemJson. refactor extracted assertRateLimited(). Zero production files. 1 skipped, BUILD SUCCESS.)
- [x] design (ADR: login-rate-limit-decision.md — Option A: embed LoginThrottle VO on User aggregate + columns on iam_user, reuse UserRepository + Clock; threshold/window kept as domain constants (NOT @ConfigurationProperties for now — per user); TooManyLoginAttemptsException→429 via config-driven error mapping. 5th wrong attempt trips lock AND responds 429; correct pw while locked = 429. Rejected B: dedicated LoginAttempt table/2nd port; C: web-layer filter)
- [x] red-usecase (AuthenticationServiceTest.AuthenticateTest — 3 RED corner-case tests @ExpectedToFail(AssertionError): (1) 5th consecutive wrong pw trips lock → TooManyLoginAttemptsException not BadCredentials; (2) correct pw while locked → still rate-limited (throttle check short-circuits before pw verify; corrected prediction: actual = authentication succeeds/no exception); (3) successful login below threshold resets counter → fresh full run needed to relock. Test Clock wired in setUp. Created TooManyLoginAttemptsException (domain, msg "Too many failed attempts"); AuthenticationService gained Clock ctor param (plumbing only — RED, body unchanged, IDE 'unused field' warning expected, consumed by green-usecase). UserStatements throttle Statements added (LOCKOUT_THRESHOLD=5 + creds internal). Below-threshold→BadCredentials & window-expiry NOT tested (trivially pass at RED baseline — flagged as coverage notes for green-usecase). test-review CLEAN (strict assertions, 3-tier ok, no middlemen). refactor: Extract Method wrongPasswordCommand(). Post-review per user feedback: reset test restructured so WHEN = the successful login (the reset action) and THEN = thenFreshThresholdRunIsRequiredToRelock (proves reset: next threshold-1 wrong stay BadCredentials/assertNotRateLimited, only threshold-th relocks). Throttle Statements extracted to LoginThrottleStatements (UserStatements hit 211L >200 limit) sharing the SUT's userRepository; @SuppressWarnings("java:S2699") on reset test (assertions in THEN Statements). 7 run, 0 fail, 3 skipped. checkstyle/pmd/IDE clean.)
- [x] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (Check 1 ports: db [S] — UserRepository.findByLogin simple derived query + full-aggregate save on active-persistence User; throttle columns auto-persist via JPA + migration, proven by integration suite. Check 2 exceptions: TooManyLoginAttemptsException → 429 is config-only (error-handling starter in application.yml, no Java adapter logic) AND already asserted end-to-end by the Level 1 LoginRateLimitIntegrationTest (429 + application/problem+json + type) → a @WebTest slice would duplicate Level 1 (pyramid: level N skips what N-1 covers), so rest adapter is [S]; the minimal config mapping is applied in green-acceptance — it must be atomic with removing the @ExpectedToFail marker because adding the mapping makes the marked Level 1 test pass (junit-pioneer fails the build on an @ExpectedToFail test that passes). Mirrors Scenario 5.1 red/green-adapter rest [S] + production code in green-acceptance. Check 3 response shape: login success has no body, throttle path is an error response handled by Check 2 → [S].)
- [S] red-adapter rest (429 + application/problem+json already asserted by Level 1 LoginRateLimitIntegrationTest; mapping is config-only, no Java adapter logic — a web slice duplicates Level 1)
- [S] green-adapter rest (no separate green — the config mapping is applied in green-acceptance, atomically with the @ExpectedToFail marker removal)
- [x] green-acceptance

### Scenario 5.3: Passwords are stored hashed
- [S] red-acceptance (Level 1 acceptance is black-box HTTP only and NEVER touches the DB — see TESTING.md "Level 1" and tdd-rules.md. The scenario's only observable is the stored password value in the iam_user row; there is no HTTP-observable behavior, so a true Level-1 test cannot assert the "$2a$" BCrypt prefix or plaintext-absence — it would have to query the DB directly, which the rules forbid. Mirrors Scenario 5.1 red-acceptance [S]: Level 1 cannot distinguish a hashed column from a plaintext one over HTTP, so the property is proven at a lower, cheaper level. The real security property — "the application turns a plaintext password into a "$2a$" BCrypt hash and never stores the plaintext" — lives in PasswordPolicy.hashPlain() and is proven by a focused Level-4 domain test using the REAL BCryptPasswordEncoder (see design/red-domain recommendation below). The existing PasswordPolicyTest uses NoOpPasswordEncoder and only asserts encoder.matches(plain, hash) — under NoOp that is plain.equals(hash), so it does NOT cover the "$2a$" format or plaintext-absence; a new domain test is a justified security-regression guard, not a duplicate.)
- [x] design (feature already implemented — PasswordPolicy.hashPlain() validates complexity then BCrypt-encodes via the wired DelegatingPasswordEncoder, ActivationService.activate() saves it; no production code needed. Test strategy: Level-4 domain test on hashPlain() — Option A "Faithful": inject the production-equivalent PasswordEncoderFactories.createDelegatingPasswordEncoder(), assert the returned hash does NOT contain the plaintext AND startsWith("{bcrypt}$2a$"). ⚠ Production stores "{bcrypt}$2a$…" (DelegatingPasswordEncoder prepends the {bcrypt} id), NOT bare "$2a$…" — test spec §5.3 "starts with $2a$" is inaccurate (didn't account for the delegating prefix); the test documents this. Rejected Option B (plain BCryptPasswordEncoder + startsWith "$2a$"): not the wired encoder → weaker guard. No ADR — test-strategy detail, no structural trade-off. Activates red-domain/green-domain; red-usecase/green-usecase/adapters-discovery/green-acceptance [S] below.)
- [S] red-usecase (no new usecase logic — hashPlain() already exists and is exercised end-to-end by activation scenarios 3.1/4.1; the "stored hashed" property is a domain-service concern proven at red-domain. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [x] red-domain (PasswordPolicyTest.StorageFormatTest added — own SUT built with production-equivalent createDelegatingPasswordEncoder(); asserts hash doesNotContain plaintext AND startsWith "{bcrypt}$2a$". test-review added a 2nd guard: same plaintext hashed twice → different hashes (BCrypt salting — catches a swap to an unsalted digest the prefix check can't). refactor extracted VALID_PLAINTEXT constant. Predicted PASS (feature already implemented) → actual PASS; PasswordPolicyTest 11 tests, 0 fail; checkstyle/pmd clean. No @ExpectedToFail (red+green collapse, regression guard mirroring 5.1). No production files changed.)
- [S] green-domain (no production change — existing PasswordPolicy.hashPlain() already produces the "{bcrypt}$2a$…" salted hash the red-domain test asserts; red+green collapse, mirrors Scenario 5.1 green-adapter db [S]. PasswordPolicyTest green: 11 tests, 0 fail. No coverage step — no new production code.)
- [S] adapters-discovery (feature already implemented — no new ports, exceptions, or response shapes; existing REST/security/error mapping unchanged by this scenario)
- [S] green-acceptance (no acceptance test to enable — red-acceptance [S]; security property proven at Level-4 domain)

### Scenario 5.4: Tampered JWT activation token is rejected
- [S] red-acceptance (JWT-signature-rejection HTTP error on POST /api/auth/activate → Level 2 web slice + Level 4 domain, NOT Level 1. Pyramid rule (TESTING.md L32, tdd-rules.md): Level 1 = happy path only; error/corner cases belong to lower levels — "one acceptance scenario per endpoint behavior category". Mirrors in-story precedents 2.2 + 3.2 (both JWT-rejection→422 on this endpoint family, both red-acceptance [S] "error cases tested at Level 2 web slice"). A tampered/wrong-secret token IS fully observable over HTTP (422 response, no DB needed), but observability does not promote an error case to Level 1 — the pyramid bars error cases from L1 regardless. This is a genuine RED needing NEW production code: parseActivationClaim() throws io.jsonwebtoken.security.SignatureException on a re-signed/wrong-secret token, but SignatureException is UNMAPPED in application.yml (only ExpiredJwtException + MalformedJwtException → 422) so it currently falls through to 500; AND the required detail "Invalid activation token" exists nowhere in production. Recommended lower-level decomposition (design step decides, do not add here): (1) red-domain — REAL-crypto Level-4 test on JwtActivationTokenGenerator.parseActivationClaim() proving a token signed with a different/random secret AND a tampered-then-re-signed token throws SignatureException (the actual security property — a mocked web slice cannot prove real signature verification; mirrors 5.1/5.3 proving the property at the cheapest real level); (2) red-adapter rest — Level-2 web slice on POST /api/auth/activate asserting that when the activate path throws SignatureException the response is 422 + Problem Detail detail="Invalid activation token" (where the new SignatureException→422 mapping + custom detail get tested, extending AuthResourceTest.ActivateAccountTest). OPEN DESIGN QUESTION for the design step: HOW to produce the custom detail "Invalid activation token" — config-map SignatureException in application.yml (does the error-handling starter allow a custom per-exception message/detail?) vs. catch the JWT signature failure in domain/adapter and rethrow a domain exception (e.g. InvalidActivationTokenException) carrying that message, then map THAT → 422. Do not decide now.)
- [x] design (Option A "Config-only" approved — map io.jsonwebtoken.security.SignatureException → 422 in application.yml error.handling.http-statuses AND override its detail via error.handling.messages: "Invalid activation token". Consistent with the existing pattern that maps ExpiredJwtException/MalformedJwtException raw in config (2.2/3.2); the messages override is Context7-confirmed supported by the wimdeblauwe error-handling starter for any exception type. Zero Java production code — parseActivationClaim() already throws SignatureException natively on a wrong-secret/tampered-resigned token. Rejected Option B (wrap in a domain InvalidActivationTokenException): more code, changes parseActivationClaim's contract, and would special-case one jjwt exception while leaving its siblings raw — inconsistent, no payoff. No ADR — extends an existing config pattern, no novel structure. Decomposition: red-domain activated (real-crypto regression guard); red-usecase/green-usecase [S]; the NEW config mapping is the only production change and lands in green-adapter rest via adapters-discovery; green-acceptance [S].)
- [S] red-usecase (no new usecase logic — ActivationService.activate() already calls parseActivationClaim() which throws SignatureException; the usecase just propagates it. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [x] red-domain (JwtActivationTokenGeneratorTest — real-crypto Level-4 regression guard. @ParameterizedTest over {wrong-secret token, tampered-then-re-signed token}, both built by a second JwtActivationTokenGenerator(WRONG_SECRET); asserts parseActivationClaim() throws io.jsonwebtoken.security.SignatureException. Predicted PASS, ran PASS — 2 tests, 0 fail, real signature verification already rejects tampering; red+green collapse, NO @ExpectedToFail, NO production change. First run errored on non-static @MethodSource [test-setup bug, not feature behavior]; fixed by making factory static + flattening nested class, re-ran green. 56 lines.)
- [S] green-domain (no production change — parseActivationClaim() already verifies the HMAC signature; red-domain passed immediately. red+green collapse, mirrors 5.1/5.3.)
- [x] adapters-discovery (Check 1 ports: db/UserRepository [S] — a wrong-secret/tampered token throws SignatureException at parseActivationClaim() BEFORE userRepository.findById() is reached; no storage behavior exercised, no other ports change. Check 2 exceptions: rest [add] — io.jsonwebtoken.security.SignatureException is UNMAPPED in application.yml (its hierarchy SignatureException→SecurityException→JwtException is not a subtype of the mapped MalformedJwtException, and search-super-class-hierarchy is off) → currently falls to default 500; need 422 + detail "Invalid activation token". Verified at Level-2 web slice extending AuthResourceTest.ActivateAccountTest. Check 3 response shape: rest [S] — 5.4 is an error response handled by Check 2; the POST /api/auth/activate happy-path shape (3.1/4.1) is unchanged. → insert red-adapter rest / green-adapter rest.)
- [x] red-adapter rest (AuthResourceTest.ActivateAccountTest.should_return422_when_tamperedActivationToken: mocks activationService.activate(token,password) to throw io.jsonwebtoken.security.SignatureException; POSTs valid-shape body; asserts 422 + Problem Detail detail="Invalid activation token", instance="/api/auth/activate". RED via @ExpectedToFail(withExceptions=AssertionError.class). Predicted AssertionError "422 expected but 500 actual" — matched exactly (actual body: detail="JWT signature does not match", status 500, type .../signature → SignatureException unmapped, falls to starter default). Serialized AuthResourceTest with @Execution(SAME_THREAD) so the new mock stubbing doesn't race ValidateActivationTokenTest's shared activationService mock. Zero production files changed. green-adapter rest adds the application.yml http-statuses + detail mapping.)
- [x] green-adapter rest (SignatureException → unprocessable-content added to application.yml error.handling.http-statuses + new error.handling.messages override "Invalid activation token"; @ExpectedToFail marker + unused import removed; AuthResourceTest 6/6 green, 0 skipped; ExpiredJwt "Token expired" / MalformedJwt "Invalid token" raw-message details unaffected — messages block scoped to SignatureException only.)
- [S] green-acceptance (no acceptance test to enable — red-acceptance [S]; security property proven at Level-4 domain + the mapping at Level-2 web slice)

### Scenario 5.5: Expired JWT activation token is rejected
- [S] red-acceptance (Expired-JWT-token HTTP error on POST /api/auth/activate → Level 2 web slice, NOT Level 1. Pyramid rule (TESTING.md L32, tdd-rules.md): Level 1 = happy path only; error/corner cases belong to lower levels — "one acceptance scenario per endpoint behavior category". Mirrors in-story precedents 2.2 + 3.2 + 5.4 (all JWT-rejection→422 on this endpoint family, all red-acceptance [S] "error cases tested at Level 2 web slice"). An expired token IS fully observable over HTTP (422 response, no DB needed), but observability does not promote an error case to Level 1 — the pyramid bars error cases from L1 regardless. The expired-token path ALREADY returns 422 end-to-end: io.jsonwebtoken.ExpiredJwtException is mapped → unprocessable-content in application.yml error.handling.http-statuses (added in 2.2/3.2). The ONLY gap is the detail message: §5.5 requires the exact detail "Activation token has expired", but ExpiredJwtException has NO error.handling.messages override (the messages block currently scopes only SignatureException → "Invalid activation token", added in 5.4), so the current detail is the raw jjwt exception message ("Token expired" in the mocked web slice / "JWT expired N ms ago…" with a real token) — NOT the required text. The detail override + 422 confirmation belong at green-adapter rest, verified at a Level-2 web slice (AuthResourceTest.ActivateAccountTest), mirroring 5.4's SignatureException messages override exactly. DESIGN STEP MUST DECIDE: how to produce the custom detail — config-map an error.handling.messages override for io.jsonwebtoken.ExpiredJwtException: "Activation token has expired" (Option A, consistent with 5.4's SignatureException override — Context7-confirmed the wimdeblauwe starter supports per-exception message overrides) vs. wrapping in a domain exception (Option B, rejected by precedent: more code, special-cases one jjwt sibling while leaving others raw). DECOMPOSITION ACTIVATED: red-domain [S] (unlike 5.4, NO real-crypto guard is warranted here — token expiry is verified by the jjwt parser's own .clock() check inside parseActivationClaim(), a library behavior, not our security logic; a real-crypto test would only re-test jjwt; the existing JwtActivationTokenGeneratorTest already exercises parseActivationClaim signature verification). red-usecase/green-usecase [S] (no new usecase logic — ActivationService.activate()/findUserByToken() already propagate ExpiredJwtException). adapters-discovery activates rest [add]: red-adapter rest (Level-2 web slice on POST /api/auth/activate asserting 422 + Problem Detail detail="Activation token has expired" when activate() throws ExpiredJwtException) + green-adapter rest (the application.yml messages override is the only production change). green-acceptance [S].)
- [x] design (Option A "Config-only" approved, no ADR — add an error.handling.messages override mapping io.jsonwebtoken.ExpiredJwtException → "Activation token has expired" in application.yml. ExpiredJwtException is already mapped → unprocessable-content (422) in http-statuses (2.2/3.2); the ONLY gap is the §5.5 detail text, currently the raw jjwt message ("Token expired"). Mirrors 5.4's SignatureException messages override exactly; the wimdeblauwe error-handling starter supports per-exception messages overrides (Context7-confirmed in 5.4). Zero Java production code. Rejected Option B (wrap in a domain InvalidActivationTokenException): more code, changes parseActivationClaim's contract, special-cases one jjwt sibling while leaving MalformedJwtException raw — inconsistent, no payoff. Decomposition: red-domain/red-usecase/green-usecase [S]; the application.yml messages line is the only production change and lands in green-adapter rest via adapters-discovery; green-acceptance [S].)
- [S] red-usecase (no new usecase logic — ActivationService.activate() already calls findUserByToken()/parseActivationClaim() which throws ExpiredJwtException; the usecase just propagates it. Same path already exercised by ValidateTokenErrorTest / 3.2. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain (no real-crypto guard warranted — token-expiry rejection is jjwt's own .clock() check inside parseActivationClaim(), a library behavior not our security logic; a domain test would only re-test jjwt. Unlike 5.4 there is no tampering/signature property unique to us. parseActivationClaim signature verification already covered by JwtActivationTokenGeneratorTest.)
- [S] green-domain
- [x] adapters-discovery (Check 1 ports: db/UserRepository [S] — an expired token throws ExpiredJwtException at parseActivationClaim() BEFORE userRepository.findById() is reached; no storage behavior exercised, no other ports change (and findById/findByLogin are simple Spring Data derived queries regardless). Check 2 exceptions: rest [add] — io.jsonwebtoken.ExpiredJwtException is already mapped → unprocessable-content (422) in application.yml http-statuses (2.2/3.2), so the STATUS is correct, but the §5.5 contract asserts the exact Problem Detail detail="Activation token has expired" and there is NO error.handling.messages override for ExpiredJwtException (the messages block scopes only SignatureException → "Invalid activation token", added in 5.4) → current detail is the raw jjwt message ("Token expired"), wrong text. The detail override is an error-mapping concern verified at a Level-2 web slice extending AuthResourceTest.ActivateAccountTest. Check 3 response shape: rest [S] — 5.5 is an error response handled by Check 2; the POST /api/auth/activate happy-path shape (3.1/4.1) is unchanged. → insert red-adapter rest / green-adapter rest.)
- [x] red-adapter rest (AuthResourceTest.ActivateAccountTest.should_return422_when_expiredActivationToken: mocks activationService.activate(token,password) to throw io.jsonwebtoken.ExpiredJwtException; POSTs valid-shape body; asserts 422 + Problem Detail detail="Activation token has expired", instance="/api/auth/activate" via the whole-response assertUnprocessable() helper. RED via @ExpectedToFail(withExceptions=AssertionError.class). Unlike 5.4 the STATUS assertion PASSES at RED (ExpiredJwtException already mapped → unprocessable-content in application.yml http-statuses); predicted AssertionError on the detail field — matched exactly (actual detail="JWT expired", the raw jjwt message, because no error.handling.messages override exists for ExpiredJwtException). givenActivationFailsExpired() helper mirrors 5.4's givenActivationFailsSignature() (kept separate, not parameterized — different expected detail + only 5.5 carries @ExpectedToFail). Zero production files changed; file 184L. test-review CLEAN (strict whole-response assertions, RED reason pinned by exact detail). green-adapter rest adds the application.yml messages override.)
- [x] green-adapter rest (added io.jsonwebtoken.ExpiredJwtException: "Activation token has expired" to error.handling.messages in application.yml; removed @ExpectedToFail marker + unused import. SURPRISE during green: the approved config-only design was INSUFFICIENT as first scoped — error.handling.messages is keyed GLOBALLY by exception type, not per-endpoint, so a single override cannot give ExpiredJwtException two different details. The existing GET /api/auth/validate test (Scenario 2.2, ValidateActivationTokenTest.should_return422_when_expiredActivationToken) asserted detail="Token expired" (a raw-jjwt-message artifact of its mock — the 01_API_Tests.md §2.2 spec only requires "an error indicating the token issue", NOT the literal "Token expired"). green-agent correctly STOPPED rather than break it. RESOLUTION (user-approved, design revisit): UNIFY the message — both the validate (GET) and activate (POST) endpoints surface the same clean expiry detail "Activation token has expired" for the same activation-token expiry condition. Updated the 2.2 GET-path test assertion (and its mock message to the realistic "JWT expired") to the unified detail; kept config-only (zero Java production code). Regression-verified: MalformedJwt "Invalid token" + Signature "Invalid activation token" details unaffected (messages block scoped per-type). AuthResourceTest 7/7 GREEN, 0 skipped. No coverage step — config-only change, no new Java production lines/branches (mirrors prior no-production-code green steps). checkstyle/pmd/IDE clean.)
- [S] green-acceptance (no acceptance test to enable — red-acceptance [S]; expired-token 422 + detail mapping proven at Level-2 web slice)

### Scenario 5.6: POST /api/auth/activate without CSRF token returns 403
- [x] red-acceptance (ActivateAccountCsrfIntegrationTest — Level 1 regression guard, red+green collapse. POST /api/auth/activate without a CSRF token → 403. SecurityConfig enables `.csrf(CsrfConfigurer::spa)`, so the activate POST is CSRF-protected and the no-CSRF AuthApi.activate(body) path is rejected by Spring Security's CSRF filter before the handler. Reuses ActivationTokenFixture (PENDING user + valid token), AuthApi no-CSRF POST, AssertionResponse. Predicted PASS → actual PASS (Tests run: 1, 0 fail, 0 skip); NO @ExpectedToFail (mirrors AuthCsrfIntegrationTest #130 + scenarios 5.1/5.3/5.4 collapse). test-review tightened status-only → full RFC 9457 ProblemDetail body match (detail/title/type/instance/status via ProblemDetailAccessDeniedHandler). refactor CLEAN — 50 lines, no smells. checkstyle/pmd/IDE clean.)
- [x] design (Confirm existing implementation — no new code; no ADR. Single viable approach, no trade-off. CSRF is enforced globally by SecurityConfig `.csrf(CsrfConfigurer::spa)`; POST /api/auth/activate is permitAll for authentication but still CSRF-protected, so a request without the XSRF-TOKEN cookie / X-XSRF-TOKEN header is rejected by Spring Security's CSRF filter before the handler → AccessDeniedException → 403 + RFC 9457 Problem Detail via the wired ProblemDetailAccessDeniedHandler. Mirrors AuthCsrfIntegrationTest #130 (login CSRF) + scenarios 5.1/5.3/5.4 red+green collapse. Downstream steps [S] — zero production files change.)
- [S] red-usecase (no usecase logic — CSRF rejection is a framework/security-filter concern enforced before any usecase runs; ActivationService is never reached. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports, exceptions, or response shapes — CSRF protection + 403 Problem Detail handled by existing SecurityConfig `.csrf(CsrfConfigurer::spa)` + ProblemDetailAccessDeniedHandler, both unchanged by this scenario)
- [S] green-acceptance (no acceptance test to enable — red-acceptance is a red+green collapse that already passes with no @ExpectedToFail marker; the security property is proven at Level 1 by ActivateAccountCsrfIntegrationTest)

### Scenario 5.7: Mass assignment on activate endpoint — extra fields ignored
- [x] red-acceptance (ActivateAccountMassAssignmentIntegrationTest — Level 1 regression guard, red+green collapse, predicted PASS → actual PASS, NO @ExpectedToFail. POST /api/auth/activate with token + injected extra JSON `role:"ADMIN"` + `status:"LOCKED"` → 200; then login as the activated user + GET /api/auth/me asserts the FULL body exact (all 7 fields): status="ACTIVE" (proves activation came from the flow, not the injected field), roles=[] (no elevation). Jackson silently drops unknown props on the closed `ActivateAccountRequest(token, password)` record. test-review HARDENED the guard: injected status="LOCKED" (a value the flow never produces — if mass-assigned, login would fail "Account locked"), dropped IGNORING_EXTRA_FIELDS for full-body exact match, added firstName/lastName to ActivatedUserRegistration + FIRST_NAME/LAST_NAME constants; AuthSessionFactory.loginAs(login,password) extracted (loginAsAdmin delegates). refactor CLEAN — inline JSON payload mirrors sibling ActivateAccount*IntegrationTest convention (malicious payload visible = test subject). CONSTRAINT: literal "role remains USER" is NOT HTTP-observable — no role concept in domain (CurrentUserResponse.roles hardcoded List.of()); roles=[] is the strongest proxy. 1 passed, 0 failed, 0 skipped. checkstyle/pmd/IDE clean.)
- [x] design (Confirm existing implementation — no new code; no ADR. Single viable approach, no trade-off. Mass assignment is structurally impossible: (1) `ActivateAccountRequest` is a CLOSED Java record exposing only `{token, password}` — no binder surface for `role`/`status`; (2) Spring Boot's default Jackson config has `FAIL_ON_UNKNOWN_PROPERTIES=false`, so unknown JSON props are silently dropped, never bound; (3) the use case signature is `activationService.activate(token, password)` — extra fields cannot flow through. The domain has NO role/arbitrary-status concept settable from outside (`CurrentUserResponse.roles` hardcoded `List.of()`). Proven end-to-end by the red+green collapse at Level 1 (ActivateAccountMassAssignmentIntegrationTest). Mirrors 5.6 confirm-existing. Downstream steps [S] — zero production files change.)
- [S] red-usecase (no usecase logic — mass-assignment protection is structural at the DTO/Jackson boundary; the closed record + activate(token,password) signature give no surface for extra fields. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports, exceptions, or response shapes — the closed `ActivateAccountRequest(token, password)` record + Spring default Jackson unknown-property handling already enforce the behavior; existing REST adapter unchanged by this scenario)
- [S] green-acceptance (no acceptance test to enable — red-acceptance is a red+green collapse that already passes with no @ExpectedToFail; the security property is proven at Level 1 by ActivateAccountMassAssignmentIntegrationTest)

### Scenario 5.8: Oversized password input rejected
- [S] red-acceptance (validation error case — belongs at Level 2 web slice, not Level 1 acceptance; Level 1 = happy path only. Oversized input is HTTP-observable, but observability does not promote an error case to Level 1. The @Size(min=12,max=128) on ActivateAccountRequest.password already rejects a 200-char password → MethodArgumentNotValidException → 422 + FieldError.size() "size must be between 12 and 128"; zero production change. The existing web slice AuthResourceTest.ActivateAccountTest.should_return422WithSizeError_when_requestInvalid() covers only the MIN boundary — real coverage for the MAX boundary lives in red-adapter rest below. Mirrors 2.2/3.2/5.4/5.5.)
- [x] design (Confirm existing implementation — no new code; no ADR. Single viable approach, no trade-off. Oversized-password rejection is declarative bean validation at the web boundary: `@Size(min=12, max=128)` on `ActivateAccountRequest.password`. A 200-char password violates `max=128` → `MethodArgumentNotValidException` → 422 + `FieldError.size()` "size must be between 12 and 128". This is adapter-layer input validation, NOT domain or usecase logic — so red-usecase/green-usecase [S]. The only test gap is the MAX boundary of @Size (the existing web slice `should_return422WithSizeError_when_requestInvalid` covers only MIN). adapters-discovery activates rest [add]: a Level-2 web slice MAX-boundary regression guard; green-adapter rest is a likely red+green collapse (@Size already rejects). Mirrors 3.1 (password-policy validation at adapter level) + the 5.7/5.6 confirm-existing pattern.)
- [S] red-usecase (no usecase logic — oversized-password rejection is declarative @Size bean validation at the web/DTO boundary, rejected before ActivationService runs. Zero usecase production files modified.)
- [S] green-usecase (no new usecase code needed)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (existence check — MAX boundary ALREADY covered, no new adapter steps. Check 1 ports: db [S] — @Size rejects at the controller boundary before ActivationService/storage is reached; no new ports. Check 2 exceptions: rest [S] — the MAX boundary is already tested by `ActivateAccountRequestTest.invalidFields()` "Invalid password: too long" (`"a".repeat(MAX_LENGTH+1)` → Size violation "size must be between 12 and 128"), a focused fast bean-validation test on the DTO constraints; the MethodArgumentNotValidException→422+fieldErrors mapping is the framework's generic handler already exercised by 3.1's web slice. No new exception mapping, no new web-slice test needed — supersedes the design note's "rest [add]" expectation (the coverage already exists). Check 3 response shape: rest [S] — error response (RFC 9457 + fieldErrors) unchanged. Verified green: ActivateAccountRequestTest 5/5, 0 failed.)
- [S] green-acceptance (no acceptance test to enable — red-acceptance [S]; the oversized-password 422 + length-error property is proven at the DTO bean-validation level by ActivateAccountRequestTest "Invalid password: too long")

## Load Scenarios

> ⏸ DEFERRED (2026-06-15, user scope review): all Load scenarios deferred out of MVP — premature
> optimization (hardware-coupled flaky thresholds; 4.1 needs a load-test harness the project lacks).
> Tracked in `improvements.md` → I2. Revisit in a "Login hardening / performance" story.

### Scenario 3.1: Login response time under 200ms
- [S] red-acceptance (deferred — see improvements.md I2)
- [S] design (deferred — see improvements.md I2)
- [S] red-usecase (deferred — see improvements.md I2)
- [S] green-usecase (deferred — see improvements.md I2)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (deferred — see improvements.md I2)
- [S] green-acceptance (deferred — see improvements.md I2)

### Scenario 4.1: Concurrent login requests complete under 500ms
- [S] red-acceptance (deferred — see improvements.md I2)
- [S] design (deferred — see improvements.md I2)
- [S] red-usecase (deferred — see improvements.md I2)
- [S] green-usecase (deferred — see improvements.md I2)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (deferred — see improvements.md I2)
- [S] green-acceptance (deferred — see improvements.md I2)

### Scenario 5.1: Activation token validation response time under 200ms
- [S] red-acceptance (deferred — see improvements.md I2)
- [S] design (deferred — see improvements.md I2)
- [S] red-usecase (deferred — see improvements.md I2)
- [S] green-usecase (deferred — see improvements.md I2)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (deferred — see improvements.md I2)
- [S] green-acceptance (deferred — see improvements.md I2)

## Infrastructure Scenarios

> ⏸ DEFERRED (2026-06-15, user scope review): all Infrastructure scenarios deferred out of MVP —
> require a stateful DB-outage harness (expensive, low value pre-production-traffic).
> Tracked in `improvements.md` → I3. Revisit in a "Login hardening / resilience" story.

### Scenario 4.1: Database unavailable during login returns 500
- [S] red-acceptance (deferred — see improvements.md I3)
- [S] design (deferred — see improvements.md I3)
- [S] red-usecase (deferred — see improvements.md I3)
- [S] green-usecase (deferred — see improvements.md I3)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (deferred — see improvements.md I3)
- [S] green-acceptance (deferred — see improvements.md I3)

### Scenario 5.1: Database recovery allows login after outage
- [S] red-acceptance (deferred — see improvements.md I3)
- [S] design (deferred — see improvements.md I3)
- [S] red-usecase (deferred — see improvements.md I3)
- [S] green-usecase (deferred — see improvements.md I3)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (deferred — see improvements.md I3)
- [S] green-acceptance (deferred — see improvements.md I3)
