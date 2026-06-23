# Story 3: Home page — Progress

## Spec
- [S] interview (scope confirmed directly with the user during `/story` — dashboard shell when authenticated, welcome screen with a Log in button when not, same layout for all roles, sidebar + dashboard body are placeholders; no `interview.md`)
- [x] story
- [x] mockups
- [S] api-spec (no new HTTP endpoints — Home page is frontend-only and consumes the existing GET /api/auth/me + POST /api/auth/logout, both already documented in Story 1's `endpoints.md` with auth/CSRF notes; per api-spec MVP "when in doubt, leave it out". Mirrors Story 2's `[S] api-spec`.)
- [x] test-spec

## Backend Scenarios
(none — n/a: frontend-only story, no new endpoints; consumes existing GET /api/auth/me + POST /api/auth/logout covered by Story 1. See `tests/01_API_Tests.md`.)

## Integration Scenarios
(none — n/a: no external services, scheduled jobs, or cross-context flows. See `tests/06_Integration_Tests.md`.)

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1.1: Unauthenticated home shows welcome with logo, tagline, and login button
- [x] red-playwright
- [S] red-frontend (welcome content is static/presentational — RPM logo, fixed tagline, "Войти" button; no branching/computation/transformation for `.logic.ts`. Authenticated-vs-welcome branching belongs to Scenario 2.1. Rendering verified by the RED Playwright spec + built during align-design.)
- [S] green-frontend (counterpart of skipped red-frontend; rendering handled in the component during align-design)
- [x] red-frontend-api
- [x] green-frontend-api
- [x] align-design
- [x] green-playwright
- [x] demo

### Scenario 2.1: Authenticated home shows the dashboard shell with the current user
- [x] red-playwright
- [x] red-frontend
- [x] green-frontend
- [x] red-frontend-api
- [x] green-frontend-api
- [x] align-design
- [x] green-playwright
- [x] demo

### Scenario 3.1: Opening the user menu shows the user's name, email, and logout action
- [x] red-playwright
- [S] red-frontend (trivial — no branching/computation/validation/transformation in `.logic.ts`. Menu name = already-tested `displayName` (Scenario 2.1); email = pure pass-through into the view model; "Выйти" = static label; menu open/close = presentational component state. The pass-through `email` field is added when the menu component is built during align-design. Mirrors Scenario 1.1's `[S]`.)
- [S] green-frontend (counterpart of skipped red-frontend; pass-through `email` view-model field added during align-design)
- [S] red-frontend-api (existence check — `GET /api/auth/me` is already fetched and validated by `current-user.api.ts` + `current-user.schema.ts` (includes `email`) with tests, from Scenarios 1.1/2.1. No new endpoint or field for the menu.)
- [S] green-frontend-api (counterpart; capability already exists)
- [x] align-design
- [x] green-playwright
- [x] demo

### Scenario 4.1: Clicking "Войти" on the welcome page opens the login page
- [x] red-playwright (spec `welcome-to-login.spec.ts` authored + reviewed + refactored; navigation already wired by Stories 1 & 3 — `RouterLink to="/login"` + `/login` route + login page — so the test passes from the start. Per the playwright tdd binding no `test.fail()` marker is added when a test cannot be made RED; green-playwright will simply verify it green.)
- [S] red-frontend (trivial — no branching/computation/validation/transformation in `.logic.ts`. The "Войти" control is a declarative `RouterLink to="/login"` in `WelcomeView.vue`; navigation is purely presentational with no logic function to test. Mirrors Scenario 1.1's `[S]`.)
- [S] green-frontend (counterpart of skipped red-frontend; navigation handled by the `RouterLink` in the component)
- [S] red-frontend-api (existence check — Scenario 4.1 is pure UI navigation (`RouterLink` welcome → `/login`) and makes no backend request of its own. The welcome page's `GET /api/auth/me` is already fetched + validated by `current-user.api.ts` + `current-user.schema.ts` with tests (Scenarios 1.1/2.1). No new endpoint, field, or API client. Mirrors Scenario 3.1's `[S]`.)
- [S] green-frontend-api (counterpart; capability already exists)
- [S] align-design (no new UI — Scenario 4.1 introduces no component or styling. The welcome "Войти" button (`RouterLink`, mockup `mockups/desktop/01-welcome.html`) was built + aligned in Scenario 1.1; the login page it navigates to was built + aligned in Story 1. Nothing to build or pixel-align here.)
- [x] green-playwright (verified green — `npx playwright test --project=chromium welcome-to-login.spec.ts` → 1 passed. No marker to remove (test passed from RED, navigation already wired); no production/Statements changes.)
- [x] demo (recorded `frontend/test-results/demo-welcome-to-login.webm`; spec passed 1/1 with slowMo; config reverted, tree clean)

### Scenario 4.2: Successful login redirects to the dashboard
- [x] red-playwright (spec `login-to-dashboard.spec.ts` authored + reviewed + refactored. Genuine RED: `LoginPage.submitLogin()` does not redirect on success → browser stays on `/login`; pinned by `assertNavigatedToHomeUrl()` (new method on `HomePageStatements`). Prediction matched exactly: `toHaveURL` expected `.../` got `.../login`. `test.fail()` marker added.)
- [S] red-frontend (trivial — no branching/computation/validation/transformation in `.logic.ts`, and ZERO logic-layer production files change. The redirect on successful login is an unconditional `router.push('/')` to a fixed path (no return-URL feature exists; spec asks only for "navigated to the home page") — purely presentational navigation in `LoginPage.vue`'s submit handler. A `redirectPathAfterLogin()` helper would return the constant `'/'`, which the trivial gate classifies as "a value that never varies by input". Mirrors Scenario 4.1's `[S]`. The `router.push('/')` is added to the component during align-design.)
- [S] green-frontend (counterpart of skipped red-frontend; the `router.push('/')` redirect is wired in `LoginPage.vue` during align-design)
- [S] red-frontend-api (existence check — Scenario 4.2 introduces no new endpoint, field, or API client, and ZERO API-layer production files change. The login request `POST /api/auth/login` is already implemented by `login.api.ts` (Story 1) and tested in `login.api.test.ts`; the dashboard's `GET /api/auth/me` is already fetched + validated by `current-user.api.ts` + `current-user.schema.ts` (incl. all fields) and tested in `current-user.api.test.ts` (Scenarios 1.1/2.1). The redirect itself is presentational navigation, not an API call. Mirrors Scenario 3.1's `[S]`.)
- [S] green-frontend-api (counterpart; both API clients already exist and are tested)
- [x] align-design (Build component: wired the redirect-on-success into `LoginPage.vue` — `useRouter()` + `await router.push('/')` after a successful `login()`. No markup/styling change (login page already pixel-aligned in Story 1), so `/align-design` + `/design-review` are no-ops here: no hardcoded placeholder data, rendered output unchanged. `/refactor` clean (file 106 lines; `submitLogin` is a cohesive try/catch/finally submit handler; `'/'` used once). Verified: lint green, 44 unit tests pass, playwright 4.2 now reports "Expected to fail, but passed" (redirect works — marker removed in green-playwright). `/test-coverage frontend --focus` not meaningful — only a presentational `.vue` component changed (covered by the playwright acceptance tier, not vitest); no logic/api files touched.)
- [x] green-playwright (removed the `test.fail()` marker + stale RED comment — the only allowed change. `npx playwright test --project=chromium login-to-dashboard.spec.ts` → 1 passed. No production/Statements changes. Prettier clean.)
- [x] demo (recorded `frontend/test-results/demo-login-to-dashboard.webm`; spec passed 1/1 with slowMo; config reverted, tree clean)

### Scenario 4.3: Logging out from the user menu returns to the welcome page
- [x] red-playwright (spec `logout-to-welcome.spec.ts` authored + reviewed + refactored. Genuine RED: the user-menu logout button (`user-menu-logout`) has no click handler → clicking "Выйти" does nothing, dashboard stays, welcome never appears; pinned by `assertLoginButtonIsVisible()`. Prediction matched exactly: `toBeVisible` on `welcome-login-button` timed out, element not found. Added `UserMenuStatements.clickLogout()` + `CurrentUserBackendStatements.givenAuthenticatedUserUntilLogout()` (stateful `/me` → 401 after logout, `/csrf`, `/logout`). `test.fail()` marker added. Refactor extracted shared `support/csrf-route.ts` (deduped CSRF fulfillment with `auth-backend.statements.ts`); 14 related specs green, no regression.)
- [S] red-frontend (trivial — no branching/computation/validation/transformation in `.logic.ts`, and ZERO logic-layer production files change. Logout splits into: the logout API call (`POST /api/auth/logout` with CSRF) which belongs to the API-client layer → `red-frontend-api`; and the post-logout transition back to the welcome page, which is unconditional + presentational (re-evaluate auth → welcome) in the component, not `.logic.ts` logic. No logout-related logic function exists or is needed. Mirrors Scenario 4.2's `[S]`.)
- [S] green-frontend (counterpart of skipped red-frontend; the session-end → welcome transition is wired in the component during align-design)
- [~] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

## Security Scenarios
(none in main suite — n/a: no new endpoints/input; authorization + CSRF enforced server-side and covered by Story 1, profile-name XSS is framework-auto-escaped and self-scoped. One defence-in-depth check in `tests/extended/05_Security_Tests_Extended.md`. See `tests/05_Security_Tests.md`.)

## Load Scenarios
(none — n/a: frontend-only, one GET /api/auth/me per page load (existing endpoint). See `tests/03_Load_Tests.md`.)

## Infrastructure Scenarios
(none — n/a: no new persistence or external dependencies. See `tests/04_Infrastructure_Tests.md`.)
