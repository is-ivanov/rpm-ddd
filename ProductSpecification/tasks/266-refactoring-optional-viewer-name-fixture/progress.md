# Task 266: Make viewer name optional in the Playwright auth fixture -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Make firstName/lastName optional with defaults in the auth fixture (mirror email)
- [x] refactor (current-user-backend.statements.ts — optional fields + DEFAULT_FIRST_NAME/DEFAULT_LAST_NAME + user={} default)

### Step 2: Drop explicit viewer-name from non-asserting call sites; keep the 3 name-asserting tests
- [x] refactor (call sites → givenAuthenticatedUser(); keep dashboard-page / login-to-dashboard / user-menu explicit)

### Step 3: Verify green — frontend lint + affected Playwright specs
- [~] verify (npm run lint + affected Playwright specs pass)
