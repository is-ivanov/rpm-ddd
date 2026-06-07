# Task 14: Login Form Validation and Forgot-Password Cleanup -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix 1: Required-field validation disables Sign In until both fields filled (issue #131)
Logic-driven (Humble Object): `isLoginFormValid(login, password)` in `.logic.ts`; the component
reflects it on the Sign In button. No API-client change. Step list may be refined at start.
- [~] red-frontend (logic: isLoginFormValid(login, password) — false unless both non-empty; tag #131)
- [ ] green-frontend (disable Sign In until valid)
- [S] red-frontend-api (no API client change)
- [S] green-frontend-api
- [ ] red-playwright (Sign In disabled with empty/partial fields; tag #131)
- [ ] green-playwright
- [ ] align-design (disabled-button state matches mockup)
- [ ] demo

### Fix 2: Remove out-of-scope "Forgot password" placeholder (issue #131)
Presentational removal from `LoginPage.vue` (out of scope per Story 1).
- [ ] red-playwright ("Forgot password" element absent on the login page; tag #131)
- [ ] align-design (remove the element from LoginPage.vue)
- [ ] green-playwright
- [ ] demo
