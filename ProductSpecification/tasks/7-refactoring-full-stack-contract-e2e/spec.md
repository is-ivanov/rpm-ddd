# Task 7: Pilot Full-Stack E2E (real backend)

Type: refactoring
Issue: #120

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

Add a separate **full-stack E2E** tier: a small set of Playwright tests that run against the real
backend + Postgres with no `page.route` mocking. These tests verify the actual HTTP contract
between the frontend and backend — the top of the frontend test pyramid (real frontend + real
backend + real DB), above the mocked UI tests and the Vitest logic/api unit tests.

Naming note: this tier is "full-stack E2E", not "contract test" in the Pact / consumer-driven sense
— both sides run together in one process tree rather than verifying a shared contract artifact
independently. The taxonomy is recorded in the Step 2 ADR.

Key design decisions:
- **Isolated from the default suite.** Full-stack tests live in a dedicated Playwright project
  (`fullstack`) matching only `*.fullstack.spec.ts` files, kept flat under a dedicated `fullstack/`
  folder. `npm run test:e2e` (the default) does NOT include them. A separate script
  `npm run test:e2e:fullstack` runs them explicitly.
- **Separate CI job.** A new `frontend-e2e-fullstack` job in `build.yml` depends on the `build`
  job (backend jar artifact), provisions a Postgres service container, starts the backend jar,
  starts the Vite dev server, and runs only `test:e2e:fullstack`. It does NOT replace the existing
  `frontend-e2e` job (fast, mocked, runs on every PR).
- **Real setup, not page.route.** A `RealAuthBackendStatements` class seeds test users via the
  actual backend REST API / a seed path decided by the Step 2 ADR (admin bootstrap vs Postgres
  seed). No mock intercepts.
- **No mailbox for login.** The login happy path sends no email; the test seeds an already-active
  user, so neither GreenMail nor Mailpit is needed. GreenMail stays for in-JVM backend tests;
  Mailpit is the choice only if a future full-stack scenario must read a delivered email.
- **Scope: login happy path only.** One full-stack test: valid login succeeds and the session
  cookie is set. This is the minimum to validate the full request↔response cycle. Edge cases (wrong
  credentials, inactive account) remain in the fast mocked suite — they test frontend branching
  logic, not the HTTP contract shape.

## Affected Layers

- Frontend: new `*.fullstack.spec.ts` spec file, new `RealAuthBackendStatements`, `playwright.config.ts` changes, `package.json` script
- CI: `.github/workflows/build.yml` — new `frontend-e2e-fullstack` job

## Key Files

- `frontend/acceptance/tests/fullstack/login.fullstack.spec.ts` (new)
- `frontend/acceptance/tests/statements/backend/real-auth-backend.statements.ts` (new)
- `frontend/playwright.config.ts` — add `fullstack` Playwright project
- `frontend/package.json` — add `test:e2e:fullstack` script
- `.github/workflows/build.yml` — add `frontend-e2e-fullstack` job

## Notes

- The existing `frontend-e2e` job (mocked, fast) stays unchanged — it covers UI behavior.
- Full-stack tests are deliberately few. They answer "does the real backend speak the protocol the
  frontend expects?" — one happy-path login is enough for that.
- Future full-stack tests should be added only when a new endpoint's response shape, error type URI,
  or auth mechanism is not otherwise integration-tested by the backend's own Level 1 suite.
