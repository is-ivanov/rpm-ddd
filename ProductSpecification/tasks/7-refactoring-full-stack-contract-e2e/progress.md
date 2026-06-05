# Task 7: Pilot Full-Stack Contract E2E — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Contract login spec with real backend
Write `login.contract.spec.ts` — happy-path login via real backend + Postgres, no `page.route`
mocking. `RealAuthBackendStatements` seeds a test user via actual REST API calls (no mocks).
Test starts disabled with the skip marker.
- [~] red-playwright
- [ ] green-playwright

### Step 2: Playwright project config + npm script
Add a `contract` Playwright project to `playwright.config.ts` matching only `*.contract.spec.ts`
files. Add `test:e2e:contract` script to `package.json`. Verify default `test:e2e` does NOT pick
up contract specs.
- [ ] refactor (playwright.config.ts + package.json)

### Step 3: CI job `frontend-e2e-contract`
Add `frontend-e2e-contract` job to `.github/workflows/build.yml`. Depends on `build` artifact
(jar), provisions Postgres service container, starts backend jar, starts Vite frontend, runs
`npm run test:e2e:contract`. Does NOT replace existing `frontend-e2e` job.
- [ ] refactor (build.yml)

### Step 4: ADR — Two-tier Playwright strategy
Document the decision: mocked tests (fast, default, every PR) vs contract tests (real stack,
opt-in, separate job). Clarify when to add contract tests for future scenarios (new endpoint
contract, not frontend branching logic).
- [ ] refactor (ADR)
