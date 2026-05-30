# Task 3: Run Playwright E2E in CI

Type: refactoring

## Problem

Frontend Playwright acceptance tests (`frontend/acceptance/**/*.spec.ts`) are **not executed in CI**. The `frontend` Maven profile now runs vitest unit tests (Task: connect vitest to CI/Allure), but Playwright e2e is only runnable locally.

The Allure reporter for Playwright is already wired (`allure-playwright` → `../target/allure-results`), so any e2e run produces report-compatible results — but nothing triggers that run in GitHub Actions.

Running e2e in CI is a larger lift than vitest because it needs the full stack booted inside the workflow:
- the backend application (jar) running,
- a Postgres instance (or the project's reusable test DB / Testcontainers),
- the frontend server (`vite dev`/`preview`),
- Playwright browsers installed (`npx playwright install --with-deps chromium`).

## Solution

Add a CI path that boots the stack and runs Playwright e2e, with results merged into Allure:

1. Decide execution location: a dedicated `frontend-e2e` job in `build.yml` (depends on `build`) vs. a new `test:e2e` execution in the Maven `frontend` profile. A separate job is likely cleaner (browser install, service orchestration, longer timeout, conditional on non-dependabot).
2. Provision dependencies: start Postgres (service container or Testcontainers), run the backend jar produced by `build`, start the frontend, install Playwright browsers.
3. Run `npm run test:e2e` (Playwright already emits Allure results via `allure-playwright`).
4. Ensure e2e Allure results are uploaded/merged into the same Allure report artifact as backend + vitest.
5. Tune retries/timeouts and mark the job appropriately (`if: always()` for report completeness, but failing the overall status on real failures).

## Key Files

- `.github/workflows/build.yml` — add `frontend-e2e` job (or e2e step)
- `pom.xml` — `frontend` profile, if e2e is bound to Maven instead of a CI job
- `frontend/playwright.config.ts` — already emits Allure results; may need CI-specific `webServer`/baseURL handling
- `frontend/acceptance/**` — existing specs

## Notes

- Deferred deliberately from the vitest CI/Allure integration to keep that change small and reliable.
- Watch for flakiness — e2e in CI is the most fragile tier; prefer the project's reusable test DB pattern over per-run cold-start where possible.
