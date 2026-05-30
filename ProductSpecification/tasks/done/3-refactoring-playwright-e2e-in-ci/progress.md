# Task 3: Run Playwright E2E in CI -- Progress

Type: refactoring

## Spec
- [x] spec

## Decision (Step 1)

- **Execution location:** dedicated `frontend-e2e` job (not Maven `test:e2e`).
- **Workflow file:** `build.yml`.
- **Parallelism:** `frontend-e2e` runs **parallel** to `build` (no `needs` — frontend-only scope needs no jar/Postgres).
- **Allure unification:** extract a new `allure-report` job with `needs:[build, frontend-e2e]`. Both jobs upload their `target/allure-results`; report job merges + runs `npm run report`; `deploy-report` consumes the report artifact.
- **Stack scope:** frontend-only for now (npm ci + `playwright install --with-deps chromium` + vite server). Postgres + backend jar deferred until a spec actually calls the backend (current single spec is pure-UI and skipped).

## Fix

### Step 1: Decide execution location
- [x] Choose dedicated `frontend-e2e` CI job vs. Maven `test:e2e` execution → separate `frontend-e2e` job in `build.yml`, parallel to `build`

### Step 2: Add the frontend-e2e job (frontend-only stack)
- [x] Add `frontend-e2e` job: checkout, node, `npm ci`, `npx playwright install --with-deps chromium`, `npm run test:e2e`
- [x] Upload e2e `target/allure-results` as an artifact (`allure-results-e2e`)

### Step 3: Unify Allure report across jobs
- [x] Have `build` upload its `target/allure-results` as an artifact (`allure-results-build`) instead of generating the report inline
- [x] Add `allure-report` job (`needs:[build, frontend-e2e]`): download both result sets into `target/allure-results`, `npm run report`, upload `allure-report`
- [x] Repoint `deploy-report` to `needs: allure-report`

### Step 4: Stabilize
- [x] Tune retries/timeouts, verify green and non-flaky
  - `reuseExistingServer: !process.env.CI` (fresh server + hard fail on busy port in CI). Retries (2 in CI) and webServer timeout (120s) already sound — left as-is.
  - Local `npm run test:e2e` green: vite booted via webServer, 1 skipped, exit 0. `npm run lint` clean.
  - Final CI-green confirmation requires a PR→main run (workflow does not trigger on task-branch push).
