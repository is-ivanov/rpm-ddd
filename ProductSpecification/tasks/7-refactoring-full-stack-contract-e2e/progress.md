# Task 7: Pilot Full-Stack Contract E2E — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Contract login spec with real backend
Write `login.contract.spec.ts` — happy-path login via real backend + Postgres, no `page.route`
mocking. `RealAuthBackendStatements` seeds a test user via actual REST API calls (no mocks).
Test starts disabled with the skip marker.
- [x] red-playwright

### Step 2: ADR — Two-tier Playwright strategy
Document the decision: mocked tests (fast, default, every PR) vs contract tests (real stack,
opt-in, separate job). Resolve the loginable-user seeding path the RED test exposed
(admin bootstrap vs Postgres seed) — this drives Steps 3, 4, and 5. Record the layout decision:
contract tests use BOTH a dedicated `contract/` folder AND the `*.contract.spec.ts` filename
suffix; kept flat for now (revisit grouping by feature if the set grows). Clarify when future
scenarios should get contract tests (new endpoint/HTTP-contract shape, not frontend branching
logic).
- [ ] refactor (ADR)

### Step 3: Playwright project config + npm script
Add a `contract` Playwright project to `playwright.config.ts` matching only `*.contract.spec.ts`
files. Add `test:e2e:contract` script to `package.json`. Verify default `test:e2e` does NOT pick
up contract specs.
- [ ] refactor (playwright.config.ts + package.json)

### Step 4: Green contract login test (local, real backend)
Remove the skip marker from `login.contract.spec.ts` and run it locally against the real backend
+ Postgres, seeding the loginable user per the Step 2 ADR decision. Verify it passes (full
request↔response cycle, session cookie set).
- [ ] green-playwright

### Step 5: CI job `frontend-e2e-contract`
Add `frontend-e2e-contract` job to `.github/workflows/build.yml`. Depends on `build` artifact
(jar), provisions Postgres service container, starts backend jar, starts Vite frontend, runs
`npm run test:e2e:contract`. Does NOT replace existing `frontend-e2e` job.
- [ ] refactor (build.yml)
