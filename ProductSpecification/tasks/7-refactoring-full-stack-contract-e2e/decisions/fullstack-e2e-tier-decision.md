# Decision: Full-stack E2E test tier (real frontend + backend + Postgres)

**Date**: 2026-06-09 **Scenarios**: Task 7 / Issue #120

Why: login now has backend-dependent behavior, but every Playwright test mocks the backend via `page.route` — no test verifies the real frontend↔backend HTTP contract.

| Rejected | Why |
|----------|-----|
| Call the tier "contract test" (Pact / consumer-driven sense) | Both sides run together in one process tree; there is no independently-verified contract artifact, so the Pact term misleads. Use "full-stack E2E". |
| Treat mocked Playwright tests as "E2E" | They stub the network (`page.route`); reserve "E2E" for the real stack. The mocked tier is "UI tests". |
| Seed the loginable user via REST (`POST /api/admin/users`) | Admin-created users are `PENDING` (`User.create` → `PENDING`) and need email activation — can't log in without the mail flow. |
| Boot the full-stack backend under the `prod` profile / real SMTP | `application-prod.yml` requires `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` + `starttls.required=true`; the backend won't start without real mail config. |
| Real-API admin+activate seeding (bootstrap admin → create → activate) | Needs new production admin-bootstrap code + Mailpit token retrieval; biggest blast radius, reintroduces the mailbox we removed. |

**Chosen**: A dedicated full-stack E2E tier driven by Playwright against the real backend + Postgres. Isolated by BOTH a `frontend/acceptance/tests/fullstack/` folder AND the `*.fullstack.spec.ts` suffix; a `fullstack` Playwright project matches only that suffix, the default project excludes it; `npm run test:e2e:fullstack`; CI job `frontend-e2e-fullstack`. The backend boots under a new `fullstack` Spring profile that loads the existing `test` Liquibase context (`user.csv` → pre-seeded ACTIVE `admin`) and a dummy mail host (never dialed — login sends no mail). The test logs in as the pre-seeded ACTIVE user; no REST seeding, no mailbox.

## Model

- **Frontend test taxonomy** (↔ backend pyramid):
  - Vitest logic/api tests = **unit** (Level 3/4)
  - Playwright + `page.route` mock = **integration / "UI tests"** (broad UI integration, faked network) — NOT E2E
  - Playwright + real backend + Postgres = **full-stack E2E** (top of the pyramid)
- **Layout**: `fullstack/` folder + `*.fullstack.spec.ts` suffix, kept flat (revisit grouping by feature if the set grows). Default Playwright project matches everything except `*.fullstack.spec.ts`.
- **Backend launch**: new `application-fullstack.yml` — datasource → the run's Postgres; `spring.liquibase.contexts=test`; dummy `spring.mail.host` (Spring's `JavaMailSender` connects lazily on send, so an unreachable host is fine when no mail is sent).
- **Seed**: reuse `db/data/user.csv` (ACTIVE `admin`/`admin`). The test logs in as `admin`; `RealAuthBackendStatements.givenActiveUser` asserts the pre-seeded ACTIVE user exists (no REST create). No change to `user.csv` → no risk to existing backend tests' shared seed.
- **Mail**: GreenMail stays for in-JVM backend tests (in-process SMTP fake started by a JUnit `TestExecutionListener` — cannot be injected into a separate-process backend driven by Node/Playwright). Mailpit (`docker/services.yml`, HTTP API on `:8025`) is the choice ONLY if a future full-stack scenario must read a delivered email.

## Edge Cases

| Case | Behavior |
|------|----------|
| New endpoint's response shape / problem+json `type` URI / auth mechanism not covered by the backend's own Level 1 suite | Add a full-stack E2E test |
| Frontend branching logic (error banners, field validation, conditional UI) | Stays in the mocked UI tier — NOT full-stack |
