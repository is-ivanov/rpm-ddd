# Task 14: Login Form Validation and Forgot-Password Cleanup -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix 1: Required-field validation disables Sign In until both fields filled (issue #131)
Logic-driven (Humble Object): `isLoginFormValid(login, password)` in `.logic.ts`; the component
reflects it on the Sign In button. No API-client change. Step list may be refined at start.
- [x] red-frontend (logic: isLoginFormValid(login, password) — false unless both non-empty; tag #131)
- [x] green-frontend (disable Sign In until valid)
- [S] red-frontend-api (no API client change)
- [S] green-frontend-api
- [x] red-playwright (Sign In disabled with empty/partial fields; tag #131)
- [x] align-design (wire :disabled to isLoginFormValid; disabled-button state matches mockup)
- [x] green-playwright
- [x] demo

### Fix 2: Remove out-of-scope "Forgot password" placeholder (issue #131)
Presentational removal from `LoginPage.vue` (out of scope per Story 1).
- [x] red-playwright ("Forgot password" element absent on the login page; tag #131)
- [x] align-design (remove the element from LoginPage.vue)
- [x] green-playwright
- [x] demo

### Fix 3: Map backend fieldErrors to per-control messages (issue #131, folded in 2026-06-08)
Frontend-only, defense-in-depth. Backend already returns RFC-9457 with `fieldErrors[]`; surface
per-field messages under login/password, banner for global errors only (never raw `detail` on 422).
- [x] red-frontend (logic: map ProblemDetail fieldErrors -> {login?, password?} + global message; property -> control; tag #131)
- [x] green-frontend
- [~] red-frontend-api (login.api.ts parses 422 ProblemDetail fieldErrors[] into structured LoginError; extend types.ts; tag #131)
- [ ] green-frontend-api
- [ ] align-design (render per-field messages under inputs; LoginErrorBanner shows global-only)
- [ ] red-playwright (stub 422 with fieldErrors -> per-field messages under login/password; tag #131)
- [ ] green-playwright
- [ ] demo
