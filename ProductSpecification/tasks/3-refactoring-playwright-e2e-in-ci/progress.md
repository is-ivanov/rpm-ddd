# Task 3: Run Playwright E2E in CI -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Decide execution location
- [ ] Choose dedicated `frontend-e2e` CI job vs. Maven `test:e2e` execution

### Step 2: Provision the stack in CI
- [ ] Start Postgres (service container / Testcontainers / reusable test DB)
- [ ] Run backend jar from the `build` job
- [ ] Start frontend server + install Playwright browsers

### Step 3: Run e2e and merge into Allure
- [ ] Run `npm run test:e2e` (allure-playwright already configured → ../target/allure-results)
- [ ] Upload/merge e2e results into the Allure report artifact

### Step 4: Stabilize
- [ ] Tune retries/timeouts, verify green and non-flaky
