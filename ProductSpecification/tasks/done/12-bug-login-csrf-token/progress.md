# Task 12: Login CSRF Token -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: Login performs the CSRF handshake (issue #129)
Frontend-only API-client fix. `login.api.ts` must `GET /api/auth/csrf` then `POST /api/auth/login`
with `X-XSRF-TOKEN`, mirroring `activation.api.ts`. No component/view-model change (`LoginPage.vue`
and `*.logic.ts` unchanged), so red-frontend / green-frontend / align-design are `[S]`.
- [x] red-frontend-api (login.api.ts: GET /api/auth/csrf then POST with X-XSRF-TOKEN; mirror activate-account.api.test.ts; tag #129)
- [x] green-frontend-api
- [S] red-frontend (no component/view-model logic change)
- [S] green-frontend
- [S] align-design (no styling change)
- [x] refactor (extract shared CSRF helper logic/csrf.ts; reuse in login + activation, dedupe readCookie)
- [x] green-playwright (stub /api/auth/csrf in auth-backend Statements; rerun login spec green)
- [~] demo
