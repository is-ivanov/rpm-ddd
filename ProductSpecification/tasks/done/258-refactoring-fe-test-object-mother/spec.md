# Task 258: FE test object-mother for AuthenticatedUser & /api/auth/me

Type: refactoring
Issue: #258  <- the task number IS the issue number; refactoring records it for traceability but does NOT tag tests

## Problem

Frontend test data is duplicated as full inline literals across the Vitest unit suite:

- The `JOHN_DOE: AuthenticatedUser` constant is redefined **identically in 3 files**:
  - `frontend/src/app/stores/__tests__/auth.store.test.ts`
  - `frontend/src/app/__tests__/fetch.api.test.ts`
  - `frontend/src/app/__tests__/dashboard-user.logic.test.ts`
- The full `stubMeAuthenticated()` / `stubMeUnauthenticated()` `/api/auth/me` response bodies are duplicated across:
  - `frontend/src/app/__tests__/current-user.api.test.ts`
  - `frontend/src/app/stores/__tests__/auth.store.test.ts`
  - `frontend/src/features/home/__tests__/home.smoke.test.ts`

Every test that needs an `AuthenticatedUser` or a `/me` mock rebuilds the whole object, so a contract change
(e.g. adding `timeZone` for Story 4 Scn 3.3) has to be applied at every site, and tests cannot express
"only the fields relevant to this test" — they restate the full object each time.

## Solution

Add a shared FE test **object-mother / builder** (under `frontend/src/test/builders/`) using the
`{ ...DEFAULTS, ...overrides }` pattern:

- `anAuthenticatedUser(overrides?: Partial<AuthenticatedUser>): AuthenticatedUser`
- `aCurrentUserResponse(overrides?): {...}` for the `/me` wire body (`userId`, `login`, `email`,
  `firstName`, `lastName`, `status`, `roles`, `timeZone`)
- an unauthenticated problem-detail builder, and optionally `stubMe*` MSW helpers

Tests then write `anAuthenticatedUser({ timeZone: 'Asia/Tokyo' })` and default the rest to valid values.
This is the FE analog of the backend `UserBuilder` (`aUser().withLogin(...).build()`) and the 3-tier
`Scope` builder. The pattern already exists locally — `userWith()` in
`frontend/src/features/users/__tests__/users-grid.logic.test.ts` and the Playwright
`CurrentUserBackendStatements` defaults — but was never ported to the app-layer unit tests.

## Scope

- **Test-infrastructure only** — no production code changes, no test behavior changes.
- The full FE unit suite must stay green throughout; assertions and expected values are unchanged
  (the builder must reproduce the exact same data each test currently inlines).
- Out of scope (possible follow-up): documenting the FE object-mother convention in
  `.claude/tech/vue-ts/tdd.md` — the binding is currently silent on test-data builders.

## Key Files

- `frontend/src/test/builders/` (new — the shared object-mother module)
- `frontend/src/app/stores/__tests__/auth.store.test.ts`
- `frontend/src/app/__tests__/fetch.api.test.ts`
- `frontend/src/app/__tests__/dashboard-user.logic.test.ts`
- `frontend/src/app/__tests__/current-user.api.test.ts`
- `frontend/src/features/home/__tests__/home.smoke.test.ts`
- Reference patterns: `frontend/src/features/users/__tests__/users-grid.logic.test.ts` (`userWith`),
  `frontend/acceptance/tests/statements/backend/current-user-backend.statements.ts` (defaults)
