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

Add a separate **full-stack E2E** tier: Playwright running against the real backend + Postgres +
Mailpit with no `page.route` mocking — the top of the frontend test pyramid (real frontend + real
backend + real DB + real mailbox), above the mocked UI tests and the Vitest logic/api unit tests.

Naming note: this tier is "full-stack E2E", not "contract test" in the Pact / consumer-driven sense
— both sides run together in one process tree rather than verifying a shared contract artifact
independently. The full taxonomy, seeding decision, and rationale live in the Step 2 ADR
(`decisions/fullstack-e2e-tier-decision.md`).

Key design decisions:
- **One growing critical journey, run nightly.** The tier is a single account-lifecycle journey,
  not a per-scenario suite: admin logs in (UI) → admin creates a user via API `POST /api/admin/users`
  (no admin UI yet — migrates to UI later) → the new user reads the activation link from Mailpit →
  activates (UI) → logs in (UI). It runs on a nightly schedule, NOT on every PR (slow, real I/O).
  Edge cases (wrong credentials, inactive account, validation) stay in the fast mocked UI suite.
- **Isolated from the default suite.** Full-stack tests live in a dedicated `fullstack/` folder AND
  use the `*.fullstack.spec.ts` suffix; a `fullstack` Playwright project (`retries: 2`) matches only
  that suffix, the default `chromium` project excludes it. `npm run test:e2e` (`--project=chromium`)
  does NOT include them; `npm run test:e2e:fullstack` runs them explicitly.
- **Dedicated nightly workflow.** A new `.github/workflows/nightly-fullstack-e2e.yml`
  (`on: schedule` + `workflow_dispatch`), mirroring `checkstyle-updates.yml` — NOT a job in
  `build.yml`. It starts the same `docker/infra-fullstack-tests.yml` compose, launches the backend
  with `SPRING_PROFILES_ACTIVE=fullstack`, seeds, starts Vite, and runs `test:e2e:fullstack`. The
  existing fast mocked `frontend-e2e` job is untouched.
- **Real setup, not page.route.** `RealAuthBackendStatements` drives real REST (admin login,
  create-user + CSRF) and `MailpitStatements` polls the Mailpit HTTP API for the activation link.
  The created user gets a unique-per-run identity so `retries: 2` is collision-free.
- **Mailbox via Mailpit; prod jar stays clean.** The journey reads the activation email, so Mailpit
  is required (GreenMail stays for in-JVM backend tests). The backend boots under a `fullstack`
  Spring profile running ONLY the production master changelog — NO test fixtures in the jar; the
  journey's entry admin is seeded out-of-band by `scripts/seed-fullstack.sh` after startup.

## Affected Layers

- Frontend: `*.fullstack.spec.ts` journey, `RealAuthBackendStatements` + `MailpitStatements`, `playwright.config.ts`, `package.json` scripts
- Backend config: `application-fullstack.yml` (profile), out-of-band SQL seed fixture (test resources)
- Infra/CI: `docker/infra-fullstack-tests.yml` (Postgres + Mailpit), `Infra-FullStack-Tests-Up` + `App-FullStack` run configs, `scripts/seed-fullstack.sh`, `.github/workflows/nightly-fullstack-e2e.yml`
- Docs: `frontend/acceptance/tests/fullstack/README.md` (local-run recipe)

## Key Files

- `frontend/acceptance/tests/fullstack/account-lifecycle.fullstack.spec.ts`
- `frontend/acceptance/tests/statements/backend/real-auth-backend.statements.ts`, `mailpit.statements.ts`
- `frontend/playwright.config.ts` — `fullstack` project (`retries: 2`); `chromium` excludes the suffix
- `frontend/package.json` — `test:e2e:fullstack` (+ `test:e2e` scoped to `--project=chromium`)
- `src/main/resources/application-fullstack.yml`; `src/test/resources/db/fixtures/fullstack-seed.sql`
- `docker/infra-fullstack-tests.yml`; `scripts/seed-fullstack.sh`; `.run/Infra-FullStack-Tests-Up.run.xml`, `.run/App-FullStack.run.xml`
- `.github/workflows/nightly-fullstack-e2e.yml`
- `frontend/acceptance/tests/fullstack/README.md`

## Notes

- The existing `frontend-e2e` job (mocked, fast) stays unchanged — it covers UI behavior on every PR.
- The full-stack tier is deliberately ONE journey. It grows by extending the same journey as new
  critical-path features land; it is not a place for edge cases (those stay in cheaper tiers).
- Future full-stack coverage is added only when a new endpoint's response shape, problem+json `type`
  URI, or auth mechanism on the critical path is not otherwise covered by the backend's Level 1 suite.
