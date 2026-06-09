# Decision: Full-stack E2E test tier (real frontend + backend + Postgres + Mailpit)

**Date**: 2026-06-09 **Scenarios**: Task 7 / Issue #120

Why: login now has backend-dependent behavior, but every Playwright test mocks the backend via `page.route` — no test verifies the real frontend↔backend HTTP contract along the critical user-lifecycle path.

| Rejected | Why |
|----------|-----|
| Call the tier "contract test" (Pact / consumer-driven sense) | Both sides run together in one process tree; there is no independently-verified contract artifact, so the Pact term misleads. Use "full-stack E2E". |
| Treat mocked Playwright tests as "E2E" | They stub the network (`page.route`); reserve "E2E" for the real stack. The mocked tier is "UI tests". |
| Boot the full-stack backend under the `prod` profile / real SMTP | `application-prod.yml` requires `SPRING_MAIL_*` + `starttls.required=true`; the backend won't start without real mail config. |
| Run the full-stack tier on every PR | It is slow and occasionally flaky (long journey, real I/O). Reserve it for a nightly schedule; PRs keep the fast mocked suite. |
| Many small full-stack tests / login-happy-path only | Loses the integrated-path value and multiplies the slowest tier. Keep ONE growing critical journey; edge cases stay in cheaper tiers. |

**Chosen**: A dedicated full-stack E2E tier driven by Playwright against the real backend + Postgres + Mailpit. It is **one growing critical user-lifecycle journey** (admin login → create user → activate via email → first login), run **nightly on a schedule**, not per-PR. Isolated by BOTH a `frontend/acceptance/tests/fullstack/` folder AND the `*.fullstack.spec.ts` suffix; a `fullstack` Playwright project matches only that suffix (with `retries: 2`), the default project excludes it; `npm run test:e2e:fullstack`; nightly CI job `frontend-e2e-fullstack`. The backend boots under a new `fullstack` Spring profile reusing the `test` Liquibase context (`user.csv` → pre-seeded ACTIVE `admin`, the journey's entry actor) with mail pointed at Mailpit. Local infra mirrors `Infra-Tests-Up`: a new `Infra-FullStack-Tests-Up` (Postgres + Mailpit).

## Model

- **Frontend test taxonomy** (↔ backend pyramid):
  - Vitest logic/api tests = **unit** (Level 3/4)
  - Playwright + `page.route` mock = **integration / "UI tests"** (broad UI integration, faked network) — NOT E2E
  - Playwright + real backend + Postgres + Mailpit = **full-stack E2E** (top of the pyramid)
- **Scope = ONE growing journey** (`account-lifecycle.fullstack.spec.ts`): admin logs in (UI) → admin creates a user via **API** `POST /api/admin/users` (no admin UI yet — comment to migrate to UI when it exists) → new user (PENDING) receives the activation email, the link is read from **Mailpit** → user activates (UI) → user logs in (UI). Edge cases (wrong credentials, inactive account, validation) stay in the mocked UI tier. This multi-step journey is a **deliberate exception** to the backend Level-1 "one action, assert all consequences" rule — it validates the integrated path, not a single action; justified because it is the nightly broad-stack smoke and kept to the single critical journey.
- **Layout**: `fullstack/` folder + `*.fullstack.spec.ts` suffix, flat (revisit grouping if a second journey appears). Default Playwright project matches everything except the suffix.
- **Retries / idempotency**: `fullstack` project sets `retries: 2` (a mid-journey failure masks later steps, so retry the whole journey). Because the journey **creates state**, the created user gets a **unique-per-run identity** (login/email with a unique suffix) so a retry can't collide on "user already exists". Assertions on the created user use the captured value, not a literal; the pre-seeded `admin` stays fixed. Waiting for the activation email uses a **polling wait** against the Mailpit API — never sleep.
- **Backend launch**: new `application-fullstack.yml` — datasource → the run's Postgres; `spring.liquibase.contexts=test`; `spring.mail.host`/`port` → Mailpit (`localhost:1025`). CI launches the jar with `SPRING_PROFILES_ACTIVE=fullstack`.
- **Seed**: reuse `db/data/user.csv` (ACTIVE `admin`/`admin`) as the journey's entry actor — no change to `user.csv`, no risk to existing backend tests. `User.create → PENDING`, so the journey's *other* user is created PENDING and activated via the real email flow (this is the point of the journey, not a problem to avoid).
- **Mail**: Mailpit (`docker/services.yml`, HTTP API on `:8025`) — the journey reads the activation link from it. GreenMail stays for in-JVM backend tests (in-process SMTP fake started by a JUnit `TestExecutionListener` — cannot be injected into a separate-process backend driven by Node/Playwright).
- **Local infra**: new `docker/infra-fullstack-tests.yml` (Postgres + Mailpit) + an `Infra-FullStack-Tests-Up` run config, mirroring `Infra-Tests-Up` (shared-first, idempotent start, never torn down). External-system integrations later add WireMock (stubs) to the same compose.

## Edge Cases

| Case | Behavior |
|------|----------|
| New endpoint's response shape / problem+json `type` URI / auth mechanism on the critical lifecycle path | Extend the journey (or add a second journey if a distinct critical path appears) |
| Frontend branching logic (error banners, field validation, conditional UI) | Stays in the mocked UI tier — NOT full-stack |
| Admin-create-user gains a UI | Migrate the journey's create-user step from the API call to UI actions (comment in the spec marks the spot) |
| Journey retried after a partial failure | Unique-per-run created-user identity makes the re-run collision-free |
