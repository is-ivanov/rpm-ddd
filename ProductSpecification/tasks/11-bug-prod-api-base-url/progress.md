# Task 11: Prod API Base URL -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix 1: Client API base URL defaults to relative (same-origin)
Build-config change — no meaningful red test (the defect is a Vite build-time
`define` default, not runtime logic). Verified by existing unit tests staying green.
- [x] fix vite.config.ts (client base '' default; localhost:8080 only as dev proxy target)
- [x] verify frontend unit tests green (npm run test)

### Fix 2: LoginPage surfaces a generic error on unexpected failures
- [~] red-playwright (login endpoint failure -> error banner visible)
- [ ] red-frontend (logic: map unknown error -> generic login error view)
- [ ] green-frontend
- [S] red-frontend-api (login.api.ts unchanged; no API client logic change)
- [S] green-frontend-api
- [S] align-design (reuses existing LoginErrorBanner; no new styling)
- [ ] green-playwright
- [ ] demo
