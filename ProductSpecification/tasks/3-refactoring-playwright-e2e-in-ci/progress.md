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
- [~] Have `build` upload its `target/allure-results` as an artifact (instead of generating the report inline)
- [ ] Add `allure-report` job (`needs:[build, frontend-e2e]`): download both result sets, `npm run report`, upload `allure-report`
- [ ] Repoint `deploy-report` to `needs: allure-report`

### Step 4: Stabilize
- [ ] Tune retries/timeouts, verify green and non-flaky
