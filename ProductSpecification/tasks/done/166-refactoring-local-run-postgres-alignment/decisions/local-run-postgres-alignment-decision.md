# ADR — Local real-stack run: align `/run-backend`, the `local` profile, and the compose files

Task: 166 · Issue: #166 · Status: **Superseded by the Revision below** · Date: 2026-06-28

> **Revision (2026-06-28, during Step 3 verification): the decision changed from Option 2
> to Option 1.** The original choice (Option 2 — re-point `local` at the fullstack infra)
> was implemented in Step 2, then reconsidered before verification. The fullstack infra is a
> *test* stack: its Postgres runs on **tmpfs** (all data in RAM — lost on any container
> stop/restart, only a backend-process restart survives) and is tuned for test throughput at
> the cost of durability and dev-friendliness (`fsync=off`, `synchronous_commit=off`,
> `autovacuum=off`, `wal_level=minimal`, `max_connections=30`). Those are wrong defaults for
> hands-on local development, where data should persist across sessions and autovacuum should
> run. Borrowing the test stack coupled the local-run experience to test-tuning that exists
> for a different purpose. **New decision: Option 1 — a dedicated dev compose with a
> persistent named volume, stock Postgres tuning, a published host port, and its own Mailpit.**
> The "two near-identical stacks" argument against Option 1 was overweighted: the stacks are
> *not* identical — persistent-durable-dev vs ephemeral-fast-test is a deliberate split — and
> the 5432-collision risk is removed by using a non-default host port (`54036`, in the
> project's `5403x` container-Postgres range). See "Revised Decision" below.

## Context

The documented local-run path is a guaranteed BUILD FAILURE (reproduced in Task 8 / #162):

- `/run-backend` (skill + `infrastructure.md` binding) tells you to start dev Postgres via
  `docker compose -f docker/services.yml up -d`, then run the backend on the `local`
  profile, which connects to `jdbc:postgresql://localhost:5432/rpm_ddd`.
- The dev Postgres (`docker/services.yml` → `docker/postgres.yml`) **publishes no host
  port**. The container is healthy but `localhost:5432` is unreachable → HikariCP/Liquibase
  get `Connection refused` → `spring-boot:run` fails after ~40s.

Existing Postgres infrastructures and host ports:

| Compose | Host port | Purpose | Mailpit |
|---|---|---|---|
| `docker/services.yml` (extends `postgres.yml`) | none | `local` profile (broken) | 1025/8025 |
| `docker/infra-tests.yml` | 54034 | `@Tag("db")` tests — schema recreated every db-test run | — |
| `docker/infra-fullstack-tests.yml` | 54035 | fullstack E2E (`fullstack` profile), tmpfs | 1025/8025 |

Facts that shaped the decision:

- The `local` profile does **not** load test fixtures — `application.yml` defaults to
  `ddl-auto: validate` + the production master changelog. So `local` and `fullstack` boot
  the same schema; there is no dev-fixture vs fullstack-seed data collision.
- `:54034` recreates its schema on every db-test run (`DbContainerTestExecutionListener`),
  so it would wipe a dev session mid-run — a poor shared dev target.
- `:54035` is reseeded only by a (rare, nightly) fullstack E2E run, and already ships a
  Mailpit on 1025/8025 — exactly what the `local` profile's `mail.host=localhost:1025`
  expects. It is the dev-stable, internally consistent shared target.
- `docker/.env` (committed) already provides every `POSTGRES_*` var the fullstack compose
  needs, so `--env-file docker/.env -f docker/infra-fullstack-tests.yml up -d --wait` works.

## Options considered

1. **Publish a host port on the dev compose** (`services.yml`) — restores the documented
   path but keeps two near-identical dev/test Postgres stacks and risks 5432 collisions
   with parallel sessions / other local Postgres instances.
2. **Re-point `local` at the fullstack infra (`:54035`)** — one shared infra serves both
   local runs and fullstack E2E. **(chosen)**
3. **Drop the `local` `spring-boot:run` path entirely** — document only the fullstack
   recipe. Rejected: removing the ability to run the backend on its own dev profile is a
   capability loss, not just a docs fix.

## Decision (original — superseded; kept for history)

Adopt **Option 2**. The local real-stack run uses the fullstack infra:

- `application-local.yml`: datasource url `localhost:5432` → `localhost:54035` (mail stays
  `localhost:1025`, served by the fullstack Mailpit).
- `/run-backend` + `infrastructure.md` + `infrastructure-details.md`: the dev-Postgres
  prerequisite becomes
  `docker compose --env-file docker/.env -f docker/infra-fullstack-tests.yml up -d --wait`;
  every "5432 / `services.yml`" mention becomes "54035 / `infra-fullstack-tests.yml`".
- **Delete `docker/services.yml` and `docker/postgres.yml`.** Their Postgres is now dead,
  and their Mailpit is fully covered by the fullstack infra's Mailpit (1025/8025).

> Superseded — see the Revision in the header and "Revised Decision" below. The Step 2
> commit (`1e16b90`) that implemented this is reverted by the redesign.

## Revised Decision (Option 1 — a dedicated, persistent dev stack)

Re-create a **dev-only** compose, separate from the test composes, with dev-appropriate
semantics:

- **`docker/infra-local.yml`** (renamed from the old `services.yml` for naming consistency
  with the other `infra-*.yml` composes; the old `extends docker/postgres.yml` indirection is
  dropped since nothing else referenced `postgres.yml`). Compose project name `rpm-ddd-local`,
  containers `rpm-ddd-local-{postgres,mailpit}`, volume `rpm-ddd-local-pgdata`:
  - Postgres on host port **`54036`** (non-default — avoids colliding with a locally
    installed Postgres on 5432; stays in the project's `5403x` container-Postgres range).
  - **Persistent named volume** (`rpm-ddd-local-pgdata`) — data survives `stop`/`start`/`down`
    and across dev sessions (unlike the tmpfs test stacks).
  - **Stock Postgres tuning** — no `fsync=off`/`autovacuum=off`/`wal_level=minimal`; a dev DB
    should be durable and self-maintaining.
  - **Mailpit** (SMTP `1025`, web UI `8025`) — same as before, so the `local` profile's
    `mail.host=localhost:1025` works. (Do not run the dev stack and the fullstack test stack
    at the same time — both bind 1025/8025; they serve different activities.)
  - Self-contained `image:` default (`postgres:18.3-alpine`) so the dev command needs no
    `--env-file`.
- **`.run/Infra-Local-Up.run.xml`** (new) — a `docker-deploy` run config pointing at
  `docker/infra-local.yml`, mirroring `Infra-Tests-Up` / `Infra-FullStack-Tests-Up`.
- **`.run/App-Local.run.xml`** — add `Infra-Local-Up` as a `RunConfigurationTask`
  before-launch step (before `Make`), so launching `App-Local` in IntelliJ brings up the dev
  stack automatically (idempotent). The CLI path still starts it manually.
- `application-local.yml`: datasource url → `localhost:54036`; mail stays `localhost:1025`.
- `/run-backend` + `infrastructure.md` + `infrastructure-details.md`: dev-Postgres
  prerequisite is `docker compose -f docker/infra-local.yml up -d`; port `54036`; note the
  `App-Local` auto before-launch; describe the dev stack as persistent + stock-tuned,
  distinct from the ephemeral test stacks.

## Consequences (revised)

- The documented local-run path works **and** has dev-correct semantics: data persists,
  autovacuum runs, durability is on.
- Two real-stack composes coexist — `docker/infra-local.yml` (persistent dev) and
  `docker/infra-fullstack-tests.yml` (ephemeral E2E). This is a deliberate split by purpose,
  not duplication; they never need to run simultaneously.
- No `--env-file` needed for the dev run (the compose carries its own defaults).
- IntelliJ `App-Local` users get the dev stack started for them (before-launch task) — the
  documented prerequisite is enforced by the run config, not just prose.
- Resolves the #164 dependency: the local real-stack wording has a single source of truth
  (the dev `infra-local.yml`).
- Memory `project_dev-postgres-no-host-port` becomes stale once the redesign lands (dev
  Postgres now publishes `54036`); `project_mailpit-docker-intentional` stays valid (dev
  Mailpit is still intentional). Update the first after verification.

## Implementation touch-list (revised — redesign of Step 2)

- `docker/infra-local.yml` (renamed from `docker/services.yml`) — single-file dev compose:
  Postgres `54036` + persistent named volume + stock tuning + Mailpit; self-contained image
  default; project/container/volume names `rpm-ddd-local*`. (`docker/postgres.yml` stays deleted.)
- `.run/Infra-Local-Up.run.xml` — new `docker-deploy` run config for `docker/infra-local.yml`.
- `.run/App-Local.run.xml` — add `Infra-Local-Up` before-launch `RunConfigurationTask`.
- `src/main/resources/application-local.yml` — url → `54036`.
- `.claude/skills/run-backend/SKILL.md` — prerequisite `docker compose -f docker/infra-local.yml up -d`, port `54036`, App-Local auto before-launch note.
- `.claude/tech/java-spring/infrastructure.md` — "Run (local)" prerequisite + port `54036` + before-launch note.
- `.claude/tech/java-spring/templates/infrastructure/infrastructure-details.md` — port
  table, run command, file table, dev-mail note: dev `infra-local.yml` on `54036`, persistent.
