# Story 3: Home page — Progress

> Terse entries (status + test-class/ADR ref + `see summaries/X` link). The "why" lives in
> `summaries/` + `carryover.md`; see `.claude/rules/workflow.md` → "Updating Progress".

## Spec
- [S] interview (scope confirmed directly with the user during `/story`; no interview.md)
- [x] story
- [x] mockups
- [S] api-spec (no new endpoints — consumes Story 1's GET /api/auth/me + POST /api/auth/logout)
- [x] test-spec

## Backend Scenarios
(none — n/a: frontend-only story, no new endpoints. See `tests/01_API_Tests.md`.)

## Integration Scenarios
(none — n/a: no external services, scheduled jobs, or cross-context flows. See `tests/06_Integration_Tests.md`.)

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1.1: Unauthenticated home shows welcome with logo, tagline, and login button
- [x] red-playwright
- [S] red-frontend (static/presentational welcome content — no logic to test)
- [S] green-frontend
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
- [S] red-frontend (trivial — name/email pass-through, static label, presentational menu state)
- [S] green-frontend
- [S] red-frontend-api (existence check — GET /api/auth/me already covers email)
- [S] green-frontend-api
- [x] align-design
- [x] green-playwright
- [x] demo

### Scenario 4.1: Clicking "Войти" on the welcome page opens the login page
- [x] red-playwright (passes from RED — navigation already wired by Stories 1 & 3; no fail marker)
- [S] red-frontend (trivial — declarative RouterLink, no logic)
- [S] green-frontend
- [S] red-frontend-api (existence check — pure UI navigation, no new API)
- [S] green-frontend-api
- [S] align-design (no new UI — built in Scenario 1.1 + Story 1)
- [x] green-playwright
- [x] demo

### Scenario 4.2: Successful login redirects to the dashboard
- [x] red-playwright (genuine RED — no redirect on success; prediction matched exactly)
- [S] red-frontend (trivial — fixed redirect path; see summaries/4-2-login-redirect.md)
- [S] green-frontend
- [S] red-frontend-api (existence check — login + /me API clients already exist)
- [S] green-frontend-api
- [x] align-design (see summaries/4-2-login-redirect.md)
- [x] green-playwright
- [x] demo

### Scenario 4.3: Logging out from the user menu returns to the welcome page
- [x] red-playwright (genuine RED — no logout handler; prediction matched exactly)
- [S] red-frontend (trivial — logout splits into API call + presentational transition)
- [S] green-frontend
- [x] red-frontend-api (new logout.api.ts — CSRF handshake; prediction matched exactly)
- [x] green-frontend-api
- [x] align-design (see summaries/4-3-logout-full-reload.md)
- [x] green-playwright
- [x] demo

## Security Scenarios
(none in main suite — authorization + CSRF covered by Story 1; one defence-in-depth check in `tests/extended/05_Security_Tests_Extended.md`. See `tests/05_Security_Tests.md`.)

## Load Scenarios
(none — n/a: frontend-only, one GET /api/auth/me per page load. See `tests/03_Load_Tests.md`.)

## Infrastructure Scenarios
(none — n/a: no new persistence or external dependencies. See `tests/04_Infrastructure_Tests.md`.)

## Extended (reviewed at Story Completion Gate — 2026-06-23)
All extended cases reviewed with the user at the gate; decision: **defer all** (none promoted). Logged to `improvements.md` as Open items:
- [S] UI 1.1 loading indicator while `/me` resolves → deferred (I1 — already implemented, untested)
- [S] UI 2.1 session expiry on dashboard → welcome → deferred (I2 — not built, needs design)
- [S] UI 3.1 click outside user menu closes it → deferred (I3 — UX polish, not built)
- [S] UI 4.1 single-word name → single initial → deferred (I4 — logic edge case)
- [S] SEC 1.1 XSS in profile name rendered inert → deferred (I5 — Vue auto-escapes; defence-in-depth only)

(Other extended files — 01 API, 03 Load, 04 Infrastructure, 06 Integration — contain "No extended scenarios".)
