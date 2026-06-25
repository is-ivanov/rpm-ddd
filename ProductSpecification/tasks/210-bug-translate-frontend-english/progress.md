# Task 210: Translate frontend UI text from Russian to English -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: translate Russian UI strings to English (text only, no logic)
- [x] red-frontend (flip existing test expectations to English: home.smoke, dashboard-user.logic, auth.store, current-user.api, fetch.api)
- [~] green-frontend (translate WelcomeView, DashboardShell, UserMenu, AppLoading; set `lang="en"`)
- [ ] green-playwright (run FE; `rg "\p{Cyrillic}" frontend/src` clean; `npm run lint` + frontend tests green)
- [ ] demo

> Scoped per `workflow.md` (scoped steps): text-only change, no `.logic.ts`/API/align-design work, so the logic/api/design steps of the standard frontend sequence are omitted.
