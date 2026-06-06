# Task 7: Pilot Full-Stack Contract E2E

Type: refactoring

## Problem

All Playwright acceptance tests mock the backend in-browser via `page.route`. No automated test
verifies the real frontend↔backend HTTP contract. If the backend silently changes problem+json
`type` URIs, error shapes, cookie names, or endpoint paths, the mocked Playwright suite stays green
while production breaks. The mismatch is caught only by manual cross-checking during test-review
(fragile) or by prod incidents (costly).

Root cause: the `frontend-e2e` CI job is intentionally frontend-only (Task 3 decision). This was
the right call then — the single spec was pure-UI and skipped. Now that the login flow has backend-
dependent scenarios (§3.1 wrong credentials, §3.2 inactive account), the deferred work is due.

## Solution

Add a separate **contract test** tier: a small set of Playwright tests that run against the real
backend + Postgres with no `page.route` mocking. These tests verify the actual HTTP contract
between the frontend and backend.

Key design decisions:
- **Isolated from the default suite.** Contract tests live in a dedicated Playwright project
  (`contract`) matching only `*.contract.spec.ts` files. `npm run test:e2e` (the default) does
  NOT include them. A separate script `npm run test:e2e:contract` runs them explicitly.
- **Separate CI job.** A new `frontend-e2e-contract` job in `build.yml` depends on the `build`
  job (backend jar artifact), provisions a Postgres service container, starts the backend jar,
  starts the Vite dev server, and runs only `test:e2e:contract`. It does NOT replace the existing
  `frontend-e2e` job (fast, mocked, runs on every PR).
- **Real setup, not page.route.** A `RealAuthBackendStatements` class seeds test users via the
  actual backend REST API (e.g. POST `/api/admin/users` or through the registration flow). No mock
  intercepts.
- **Scope: login happy path only.** One contract test: valid login succeeds and the session cookie
  is set. This is the minimum to validate the full request↔response cycle. Edge cases (wrong
  credentials, inactive account) remain in the fast mocked suite — they test frontend branching
  logic, not the HTTP contract shape.

## Affected Layers

- Frontend: new `*.contract.spec.ts` spec file, new `RealAuthBackendStatements`, `playwright.config.ts` changes, `package.json` script
- CI: `.github/workflows/build.yml` — new `frontend-e2e-contract` job

## Key Files

- `frontend/acceptance/tests/contract/login.contract.spec.ts` (new)
- `frontend/acceptance/tests/statements/backend/real-auth-backend.statements.ts` (new)
- `frontend/playwright.config.ts` — add `contract` Playwright project
- `frontend/package.json` — add `test:e2e:contract` script
- `.github/workflows/build.yml` — add `frontend-e2e-contract` job

## Notes

- The existing `frontend-e2e` job (mocked, fast) stays unchanged — it covers UI behavior.
- Contract tests are deliberately few. They answer "does the real backend speak the protocol the
  frontend expects?" — one happy-path login is enough for that.
- Future contract tests should be added only when a new endpoint's response shape, error type URI,
  or auth mechanism is not otherwise integration-tested by the backend's own Level 1 suite.
