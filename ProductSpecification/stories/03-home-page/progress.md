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
- [~] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 3.1: Opening the user menu shows the user's name, email, and logout action
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.1: Clicking "Войти" on the welcome page opens the login page
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.2: Successful login redirects to the dashboard
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.3: Logging out from the user menu returns to the welcome page
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
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
