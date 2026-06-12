# Task 8 — Journey Summary

## red-acceptance (2026-06-12)

**Expected:** Guard test "missing asset `/assets/missing.js`" → 404.
**Actual:** 401 Unauthorized.
**Why:** A missing static resource under permitted `/assets/**` raises `NoResourceFoundException`, which triggers an ERROR dispatch to `/error` — and that dispatch is rejected by `anyRequest().denyAll()`, so the browser sees 401, not 404.
**Resolution:** Redesigned the guard to request a real fixture asset (`src/test/resources/static/assets/app.js`) and assert its exact content/Content-Type; raw-401-for-missing-asset is potential improvements-backlog material, out of this task's scope.

## red-playwright (2026-06-12)

**Mistake:** Used `test.fails` as the Playwright RED marker, as prescribed by the `technology.md` Conventions table and the universal RED-phase template — the run died with `TypeError: test.fails is not a function`.
**Why wrong:** `test.fails` is Vitest-only; the Playwright API is `test.fail(...)`. Doc drift: `.claude/tech/playwright/tdd.md` even still prescribes `test.skip()`, contradicting both.
**Correct location/approach:** Use `test.fail(...)` in Playwright specs; the three docs (`playwright/tdd.md`, Conventions table, universal template) need aligning.

## red-playwright (2026-06-12)

**Decision:** The 401-redirect E2E drives the activation page with a mocked 401 on its API call, because the SPA has no truly protected UI action yet (HomePage makes zero API calls).
**Why:** The activation page is the only API-calling non-login view, and the spec's contract is "every API response goes through the shared layer".
**Where applied:** `frontend/acceptance/tests/frontend/login/unauthorized-redirect.spec.ts`; consequence: `activation.api` must route through the shared 401 layer for this E2E to go green.

## green-playwright (2026-06-12)

**Surprise:** The `/continue` dispatch for green-playwright prescribes `/run-backend` → `/run-frontend` unconditionally, but both are unnecessary for the mocked E2E tier: every spec mocks the backend via `page.route` (Statements like `ActivationBackendStatements`), and Playwright's `webServer` config auto-starts Vite (`npm run dev`, `reuseExistingServer`). The local backend can't even start (`docker/services.yml` Postgres publishes no host 5432 → connection refused); only the nightly `*.fullstack.spec.ts` tier needs the real stack.
**Mistake worth not repeating:** `/run-frontend` skill references `infrastructure/scripts/setup-ports.sh` / `run-frontend.sh`, which do not exist in the repo (doc drift) — don't hunt for them, just let the Playwright webServer start Vite.
**Correct approach:** For green-playwright/demo on mocked specs, skip backend/frontend startup entirely and run `npm run test:e2e` (chromium project) from `frontend/`.
