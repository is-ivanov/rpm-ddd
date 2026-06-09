# Task 7: Pilot Full-Stack E2E (real backend) — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Full-stack login spec with real backend
Write `login.fullstack.spec.ts` — happy-path login via real backend + Postgres, no `page.route`
mocking. `RealAuthBackendStatements` seeds a test user via actual REST API calls (no mocks).
Test starts disabled with the skip marker.
- [x] red-playwright

### Step 2: ADR — Frontend test taxonomy + full-stack E2E strategy
Document the decision: mocked UI tests (fast, default, every PR) vs full-stack E2E tests (real
stack, opt-in, separate job). Record the frontend test taxonomy mapped to the backend pyramid:
Vitest logic/api = unit; Playwright + mock = integration (UI tests); Playwright + real backend =
full-stack E2E. Resolve the loginable-user seeding path the RED test exposed (admin bootstrap vs
Postgres seed) — this drives Steps 3, 4, and 5. Record naming: tier is "full-stack E2E" (NOT Pact
contract); layout uses BOTH a dedicated `fullstack/` folder AND the `*.fullstack.spec.ts` suffix,
kept flat for now. Record the mail decision (login needs no mailbox; GreenMail in-JVM only; Mailpit
only if a future scenario reads email). Clarify when future scenarios get a full-stack test.
- [ ] refactor (ADR)

### Step 3: Playwright project config + npm script
Add a `fullstack` Playwright project to `playwright.config.ts` matching only `*.fullstack.spec.ts`
files. Add `test:e2e:fullstack` script to `package.json`. Verify default `test:e2e` does NOT pick
up full-stack specs.
- [ ] refactor (playwright.config.ts + package.json)

### Step 4: Green full-stack login test (local, real backend)
Remove the skip marker from `login.fullstack.spec.ts` and run it locally against the real backend
+ Postgres, seeding the loginable user per the Step 2 ADR decision. Verify it passes (full
request↔response cycle, session cookie set).
- [ ] green-playwright

### Step 5: CI job `frontend-e2e-fullstack`
Add `frontend-e2e-fullstack` job to `.github/workflows/build.yml`. Depends on `build` artifact
(jar), provisions Postgres service container, starts backend jar, starts Vite frontend, runs
`npm run test:e2e:fullstack`. Does NOT replace existing `frontend-e2e` job.
- [ ] refactor (build.yml)
