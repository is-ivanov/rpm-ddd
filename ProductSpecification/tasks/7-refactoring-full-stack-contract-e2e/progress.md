# Task 7: Pilot Full-Stack E2E (real backend) — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

Tier scope (see `decisions/fullstack-e2e-tier-decision.md`): ONE growing critical user-lifecycle
journey (admin login → create user via API → activate via email → first login), run nightly, not
per-PR. Mailpit is required (the journey reads the activation link). The committed login RED
(Step 1) is the first slice; Step 4 expands it into the full journey.

### Step 1: Full-stack login spec with real backend (first journey slice)
Write `*.fullstack.spec.ts` — happy-path login via real backend + Postgres, no `page.route`
mocking. `RealAuthBackendStatements` performs real REST calls (no mocks). Test starts disabled
with the skip marker.
- [x] red-playwright

### Step 2: ADR — Frontend test taxonomy + full-stack E2E journey strategy
Recorded in `decisions/fullstack-e2e-tier-decision.md`: taxonomy (Vitest=unit, Playwright+mock=
integration/UI tests, Playwright+real backend+Mailpit=full-stack E2E); tier = ONE growing
account-lifecycle journey run nightly (deliberate exception to the Level-1 "one action" rule);
naming = "full-stack E2E" not Pact; layout = `fullstack/` folder + `*.fullstack.spec.ts` suffix;
`retries: 2` with unique-per-run created-user identity; backend boots under a `fullstack` Spring
profile (test Liquibase context → ACTIVE `admin`, mail → Mailpit); Mailpit required, GreenMail
stays in-JVM; local infra = new `Infra-FullStack-Tests-Up` (Postgres + Mailpit), WireMock later.
- [x] refactor (ADR)

### Step 3: Full-stack harness — infra + Spring profile + Playwright project
Add `docker/infra-fullstack-tests.yml` (Postgres + Mailpit) + an `Infra-FullStack-Tests-Up` run
config, mirroring `Infra-Tests-Up` (shared-first, idempotent). Add `application-fullstack.yml`
(datasource → run's Postgres, `spring.liquibase.contexts=test`, `spring.mail` → Mailpit
`localhost:1025`). Add a `fullstack` Playwright project to `playwright.config.ts` matching only
`*.fullstack.spec.ts` with `retries: 2`; default project excludes that suffix. Add
`test:e2e:fullstack` script to `package.json`. Verify default `test:e2e` does NOT pick up
full-stack specs.
- [~] refactor (infra-fullstack-tests.yml + application-fullstack.yml + playwright.config.ts + package.json)

### Step 4: red-playwright — expand to the account-lifecycle journey
Rename the spec to `account-lifecycle.fullstack.spec.ts`. Expand the journey: admin logs in (UI) →
admin creates a user via API `POST /api/admin/users` (no admin UI yet — comment marks the
future UI migration) → read the activation link from Mailpit (polling wait) → user activates (UI)
→ user logs in (UI). Created user gets a unique-per-run identity (retry-safe). Statements split by
concern to stay under 200 lines. Test stays disabled with the skip marker; predict + validate the
RED failure against the harness.
- [ ] red-playwright

### Step 5: green-playwright — account-lifecycle journey (local)
Start `Infra-FullStack-Tests-Up` (Postgres + Mailpit), launch the backend with the `fullstack`
profile, start Vite. Remove the skip marker and verify the full journey passes.
- [ ] green-playwright

### Step 6: Nightly CI job `frontend-e2e-fullstack`
Add a scheduled (nightly cron) `frontend-e2e-fullstack` job to `.github/workflows/build.yml`.
Provisions Postgres + Mailpit service containers, starts the backend jar with
`SPRING_PROFILES_ACTIVE=fullstack`, starts Vite frontend, runs `npm run test:e2e:fullstack`. Does
NOT run on every PR and does NOT replace the existing `frontend-e2e` job.
- [ ] refactor (build.yml)
