# Task 266: Make viewer name optional in the Playwright auth fixture

Type: refactoring
Issue: #266  <- task number IS the issue number; refactoring records it for traceability, does not tag tests

## Problem

`CurrentUserBackendStatements.givenAuthenticatedUser({ firstName, lastName })` requires the viewer's
first/last name, so every call site passes `{ firstName: 'John', lastName: 'Doe' }` — including ~15 of
18 tests that never assert the viewer's name. This is noise: it implies the viewer's name matters to the
scenario when it does not. The same file already treats `email` as optional (defaulting to a
login-derived address) with an explanatory comment; `firstName`/`lastName` were simply never brought to
the same shape.

Only 3 tests actually assert the viewer's name:
- `frontend/acceptance/tests/frontend/home/dashboard-page.spec.ts` → `assertUserNameIsVisible('John Doe')`
- `frontend/acceptance/tests/frontend/home/login-to-dashboard.spec.ts` → `assertUserNameIsVisible('John Doe')`
- `frontend/acceptance/tests/frontend/home/user-menu.spec.ts` → `assertMenuShowsName('John Doe')` (already passes name + email meaningfully)

## Solution

Mirror the existing optional-`email` pattern in `current-user-backend.statements.ts`:

1. Make `firstName`/`lastName` optional in the `AuthenticatedUser` interface.
2. Add `DEFAULT_FIRST_NAME` / `DEFAULT_LAST_NAME` constants, with a comment noting they apply only when
   a caller omits the name because the scenario does not assert it.
3. Default the `givenAuthenticatedUser(user = {})` parameter and resolve
   `user.firstName ?? DEFAULT_FIRST_NAME` / `user.lastName ?? DEFAULT_LAST_NAME` in the fulfill body.
   Apply the same default handling to `givenAuthenticatedUserUntilLogout`.
4. Drop the explicit `{ firstName: 'John', lastName: 'Doe' }` from the call sites that do NOT assert the
   viewer name → they become `givenAuthenticatedUser()`.
5. KEEP the explicit names in the 3 name-asserting tests so the setup↔assertion link stays visible and
   `'John Doe'` never relies on a hidden default.

Pure test-infrastructure refactor — no `src/` production changes, no behavior change.

## Full-Stack Journey Verdict

no-impact — test-infrastructure-only refactor; no rendered critical-path or lifecycle surface changes.

## Key Files

- `frontend/acceptance/tests/statements/backend/current-user-backend.statements.ts` (the fixture)
- `frontend/acceptance/tests/frontend/**` — call sites of `givenAuthenticatedUser` / `givenAuthenticatedUserUntilLogout`
