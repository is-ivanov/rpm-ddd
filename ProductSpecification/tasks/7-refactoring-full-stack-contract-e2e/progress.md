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
profile (prod master changelog, mail → Mailpit) + out-of-band seed (SQL fixture + script, jar stays
clean); Mailpit required, GreenMail stays in-JVM; local infra = new `Infra-FullStack-Tests-Up`
(Postgres + Mailpit), WireMock later.
- [x] refactor (ADR)

### Step 3: Full-stack harness — infra + Spring profile + Playwright project
Add `docker/infra-fullstack-tests.yml` (Postgres `:54035` + Mailpit `:1025`/`:8025`) + an
`Infra-FullStack-Tests-Up` run config, mirroring `Infra-Tests-Up` (shared-first, idempotent). Add
`application-fullstack.yml` (datasource → `:54035`, prod master changelog — NO override, mail →
Mailpit, `rpm.frontend-base-url` → Vite). Seed is out-of-band (jar stays clean): SQL fixture
`src/test/resources/db/fixtures/fullstack-seed.sql` (one ACTIVE admin, `ON CONFLICT DO NOTHING`) +
`scripts/seed-fullstack.sh` (`docker exec … psql`, used by both local and CI).
Add a `fullstack` Playwright project (`testMatch` `*.fullstack.spec.ts`, `retries: 2`); the
`chromium` project excludes the suffix (`testIgnore`). Add `test:e2e:fullstack`
(`--project=fullstack`); scope `test:e2e` to `--project=chromium`.
- [x] refactor (infra-fullstack-tests.yml + application-fullstack.yml + fullstack-seed.sql + seed-fullstack.sh + playwright.config.ts + package.json)

### Step 4: red-playwright — expand to the account-lifecycle journey
Rename the spec to `account-lifecycle.fullstack.spec.ts`. Expand the journey: admin logs in (UI) →
admin creates a user via API `POST /api/admin/users` (no admin UI yet — comment marks the
future UI migration) → read the activation link from Mailpit (polling wait) → user activates (UI)
→ user logs in (UI). Created user gets a unique-per-run identity (retry-safe). Statements split by
concern to stay under 200 lines. Test stays disabled with the skip marker; predict + validate the
RED failure against the harness.
- [x] red-playwright

### Step 5: green-playwright — account-lifecycle journey (local)
Start `Infra-FullStack-Tests-Up` (Postgres + Mailpit), launch the backend with the `fullstack`
profile via `./mvnw spring-boot:run -Dspring-boot.run.profiles=fullstack` (NOT a stale jar — it may
lack `application-fullstack.yml`), run `scripts/seed-fullstack.sh` after health, start Vite. Remove
the skip marker and verify the full journey passes. (remove-marker-only — no production/Statements
changes)
- [x] green-playwright

Then capture the verified local-run recipe (separate commit; green-playwright stays
remove-marker-only):
- Add `.run/App-FullStack.run.xml` (Spring Boot, `ACTIVE_PROFILES=fullstack`) — one-click backend
  launch in IntelliJ, mirroring `App-Local`.
- Write `frontend/acceptance/tests/fullstack/README.md` documenting the exact, just-verified local
  run (console + IntelliJ): infra → backend (fullstack) → seed → Vite → `npm run test:e2e:fullstack`,
  plus the stale-jar caveat and the nightly-workflow pointer. Cross-link from `AGENTS.md`.
- [~] docs (App-FullStack run config + fullstack/README.md)

### Step 6: Nightly full-stack workflow
Add a dedicated scheduled workflow `.github/workflows/nightly-fullstack-e2e.yml` (`on: schedule`
nightly cron + `workflow_dispatch`), mirroring the `checkstyle-updates.yml` precedent — NOT a job
in `build.yml`. Starts `docker/infra-fullstack-tests.yml` (same compose as local → DRY seed
script), starts the backend jar with `SPRING_PROFILES_ACTIVE=fullstack` (+ standard Spring env
overrides), runs `scripts/seed-fullstack.sh` after health, starts Vite frontend, runs
`npm run test:e2e:fullstack`. Does NOT run on every PR and does NOT touch the existing
`frontend-e2e` job.
- [ ] refactor (nightly-fullstack-e2e.yml)
