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
- [~] design
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
