# Full-stack E2E tests

The **full-stack E2E** tier runs Playwright against the **real** stack — real frontend + real
backend + real Postgres + real Mailpit, with **no `page.route` mocking**. It is the top of the
frontend test pyramid (above the mocked UI tests and the Vitest logic/api unit tests) and verifies
the actual frontend↔backend HTTP contract along the critical user-lifecycle path.

- **Scope:** ONE growing critical journey — `account-lifecycle.fullstack.spec.ts`: admin logs in
  (UI) → admin creates a user via the admin API → the new user reads the activation link from
  Mailpit → activates (UI) → logs in (UI). Edge cases stay in the fast mocked UI suite.
- **Cadence:** runs **nightly** (CI: `.github/workflows/nightly-fullstack-e2e.yml`), **not** on
  every PR. The `fullstack` Playwright project sets `retries: 2`.
- **Isolation:** matched only by the `*.fullstack.spec.ts` suffix; the default `npm run test:e2e`
  (`--project=chromium`) excludes it.

Decision record: `ProductSpecification/tasks/7-refactoring-full-stack-contract-e2e/decisions/fullstack-e2e-tier-decision.md`.

## Run locally

### 1. Start the infra (Postgres + Mailpit)

Console:

```bash
docker compose -f docker/infra-fullstack-tests.yml up -d
```

IntelliJ: run the **`Infra-FullStack-Tests-Up`** compose run config.

Postgres → host `:54035`, Mailpit SMTP → `:1025`, Mailpit web UI/API → http://localhost:8025.
The start is idempotent; leave it running across runs (do not tear it down).

### 2. Start the backend on the `fullstack` profile

Console:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=fullstack
```

IntelliJ: run the **`App-FullStack`** Spring Boot run config (`ACTIVE_PROFILES=fullstack`).

> ⚠️ Do **not** launch a stale prebuilt jar — an old jar may not contain `application-fullstack.yml`
> and will fail to start ("Failed to determine a suitable driver class"). Use `spring-boot:run`
> (fresh classpath) or rebuild the jar (`./mvnw -DskipTests package`) first. The profile runs the
> production master changelog only — no test data is baked into the jar.

### 3. Seed the journey's entry admin (after the backend is healthy)

```bash
bash scripts/seed-fullstack.sh
```

Out-of-band seed (the prod jar stays clean): inserts the ACTIVE `admin`/`admin` row via
`docker exec … psql`. Idempotent (`ON CONFLICT DO NOTHING`).

### 4. Start the frontend (Vite)

```bash
cd frontend && npm run dev
```

Vite serves on `:5173` and proxies `/api` to the backend. (If a dev server is already running on
`:5173`, reuse it.)

### 5. Run the journey

Console:

```bash
cd frontend && npm run test:e2e:fullstack
```

IntelliJ: run the `fullstack` project via the Playwright plugin, or the `test:e2e:fullstack` npm
script.

## Notes

- Mailpit web UI (http://localhost:8025) is handy for inspecting the delivered activation email.
- The journey creates a unique-per-run user (`fsuser_<unique>`), so `retries: 2` never collides on
  "user already exists".
- CI runs the exact same `docker/infra-fullstack-tests.yml` compose, so `scripts/seed-fullstack.sh`
  works identically locally and in the nightly workflow (no local `psql` client needed).
