# Task 7: Pilot Full-Stack E2E (real backend) — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Full-stack login spec with real backend
Write `login.fullstack.spec.ts` — happy-path login via real backend + Postgres, no `page.route`
mocking. `RealAuthBackendStatements` seeds a test user via actual REST API calls (no mocks).
Test starts disabled with the skip marker.
- [x] red-playwright

### Step 2: ADR — Frontend test taxonomy + full-stack E2E strategy
Document the decision: mocked UI tests (fast, default, every PR) vs full-stack E2E tests (real
stack, opt-in, separate job). Record the frontend test taxonomy mapped to the backend pyramid:
Vitest logic/api = unit; Playwright + mock = integration (UI tests); Playwright + real backend =
full-stack E2E. Resolve the loginable-user seeding path the RED test exposed (admin bootstrap vs
Postgres seed) — this drives Steps 3, 4, and 5. Record naming: tier is "full-stack E2E" (NOT Pact
contract); layout uses BOTH a dedicated `fullstack/` folder AND the `*.fullstack.spec.ts` suffix,
kept flat for now. Record the mail decision (login needs no mailbox; GreenMail in-JVM only; Mailpit
only if a future scenario reads email). Clarify when future scenarios get a full-stack test.
Decision recorded in `decisions/fullstack-e2e-tier-decision.md`: chosen seeding path = dedicated
`fullstack` Spring profile reusing the `test` Liquibase context (`user.csv` → ACTIVE `admin`) +
dummy mail host; test logs in as the pre-seeded ACTIVE user, no REST seeding, no mailbox.
- [x] refactor (ADR)

### Step 3: Full-stack tier wiring (Playwright project + npm script + Spring profile)
Add a `fullstack` Playwright project to `playwright.config.ts` matching only `*.fullstack.spec.ts`
files; default project excludes that suffix. Add `test:e2e:fullstack` script to `package.json`.
Verify default `test:e2e` does NOT pick up full-stack specs. Add `application-fullstack.yml`
(datasource → run's Postgres, `spring.liquibase.contexts=test`, dummy `spring.mail.host`) so the
backend boots mail-safe with the pre-seeded ACTIVE `admin` — per the Step 2 ADR.
- [~] refactor (playwright.config.ts + package.json + application-fullstack.yml)

### Step 4: Green full-stack login test (local, real backend)
Launch the backend locally with the `fullstack` profile against the shared test Postgres (seed
loaded via the `test` context). Update `login.fullstack.spec.ts` to log in as the pre-seeded
ACTIVE `admin` (per ADR — admin-created users are PENDING and can't log in), remove the skip
marker, and verify it passes (full request↔response cycle, JSESSIONID session cookie set).
- [ ] green-playwright

### Step 5: CI job `frontend-e2e-fullstack`
Add `frontend-e2e-fullstack` job to `.github/workflows/build.yml`. Depends on `build` artifact
(jar), provisions Postgres service container, starts the backend jar with
`SPRING_PROFILES_ACTIVE=fullstack` (loads the `test`-context seed + dummy mail), starts Vite
frontend, runs `npm run test:e2e:fullstack`. Does NOT replace existing `frontend-e2e` job.
- [ ] refactor (build.yml)
