# Task 258: FE test object-mother for AuthenticatedUser & /api/auth/me -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Create the shared FE test object-mother module
- [x] refactor (create `frontend/src/test/builders/` with `anAuthenticatedUser(overrides?: Partial<AuthenticatedUser>)`, `aCurrentUserResponse(overrides?)` for the /me wire body, and an unauthenticated problem-detail builder, using the `{...DEFAULTS, ...overrides}` pattern; defaults reproduce the exact JOHN_DOE / current /me bodies so consumers stay byte-equal. No consumers yet → full FE unit suite stays green. lint + suite green, file <200.)

### Step 2: Dedupe the 3 duplicated AuthenticatedUser (JOHN_DOE) constants
- [x] refactor (replace the inline `JOHN_DOE: AuthenticatedUser` literal in auth.store.test.ts, fetch.api.test.ts, dashboard-user.logic.test.ts with `anAuthenticatedUser()`; assertions/expected values unchanged. Full FE unit suite green, lint green.)

### Step 3: Dedupe the duplicated /api/auth/me stub bodies
- [x] refactor (replace the inline `stubMeAuthenticated()` / `stubMeUnauthenticated()` bodies in current-user.api.test.ts, auth.store.test.ts, home.smoke.test.ts with `aCurrentUserResponse()` + the problem-detail builder; the it.fails RED markers and assertions are untouched. Full FE unit suite green, lint green.)

### Step 4: Final sweep + verify
- [x] refactor (cleanup) (verify no remaining inline AuthenticatedUser / `/me` duplication across the unit suite, all touched files <200 lines, full FE unit suite green, `npm run lint` green, IDE inspections clean on changed files.)

### Step 5: Document the FE test-data convention
- [x] refactor (doc) (record the FE object-mother/builder convention in `.claude/tech/vue-ts/tdd.md` — the binding is currently silent on test-data builders: `{...DEFAULTS, ...overrides}` mothers under `frontend/src/test/builders/`, express only fields relevant to the assertion, explicit given variable for a direct SUT input vs shared module fixture for a whole-object given+expected comparison, blank-line AAA separation without `// given/when/then` labels. Last commit of the task.)
