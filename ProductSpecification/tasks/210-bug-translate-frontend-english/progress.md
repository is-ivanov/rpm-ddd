# Task 210: Translate frontend UI text from Russian to English -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: translate Russian UI strings to English (text only, no logic)
- [x] red-frontend (flip existing Vitest expectations to English: home.smoke, dashboard-user.logic, auth.store, current-user.api, fetch.api)
- [x] red-acceptance-frontend (flip Playwright E2E + Statements expectations to English: home-page.statements, user-menu.statements, welcome-page, welcome-to-login, dashboard-page, user-menu, logout-to-welcome, login-to-dashboard; `test.fail` on the 5 that assert hardcoded UI text — login-to-dashboard stays green, fixture-only)
- [~] green-frontend (translate WelcomeView, DashboardShell, UserMenu, AppLoading; set `lang="en"`)
- [ ] green-playwright (run FE+BE; remove `test.fail` markers; `rg "\p{Cyrillic}" frontend/src frontend/acceptance` clean; `npm run lint` + Vitest + Playwright home suite green)
- [ ] translate-mockups (translate 8 Story 03 home-page mockup HTML files to English; `rg "\p{Cyrillic}" ProductSpecification/stories/03-home-page/mockups` clean) — docs edit, no TDD cycle
- [ ] demo

> Scoped per `workflow.md` (scoped steps): text-only change, no `.logic.ts`/API/align-design work, so the logic/api/design steps of the standard frontend sequence are omitted.
> Scope expanded mid-task (after `red-frontend`): `rg "\p{Cyrillic}"` at fix time surfaced Russian beyond the spec's original list — the Playwright acceptance suite (`frontend/acceptance/tests/frontend/home/*` + statements), which would break `green-playwright` once components are English, and the Story 03 mockups. User opted to translate the mockups within Task 210 (see spec.md "Key Files").
