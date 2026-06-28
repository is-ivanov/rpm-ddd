# ADR — Local real-stack run: align `/run-backend`, the `local` profile, and the compose files

Task: 166 · Issue: #166 · Status: Accepted · Date: 2026-06-28

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

## Decision

Adopt **Option 2**. The local real-stack run uses the fullstack infra:

- `application-local.yml`: datasource url `localhost:5432` → `localhost:54035` (mail stays
  `localhost:1025`, served by the fullstack Mailpit).
- `/run-backend` + `infrastructure.md` + `infrastructure-details.md`: the dev-Postgres
  prerequisite becomes
  `docker compose --env-file docker/.env -f docker/infra-fullstack-tests.yml up -d --wait`;
  every "5432 / `services.yml`" mention becomes "54035 / `infra-fullstack-tests.yml`".
- **Delete `docker/services.yml` and `docker/postgres.yml`.** Their Postgres is now dead,
  and their Mailpit is fully covered by the fullstack infra's Mailpit (1025/8025). No
  script or CI references them (only docs/skills, all updated here).

## Consequences

- One shared real-stack infra for both local dev and fullstack E2E — fewer moving parts,
  the documented run path actually works.
- Local dev data is ephemeral (the fullstack Postgres is tmpfs) and may be reseeded by a
  fullstack E2E run sharing `:54035`. Acceptable: dev data here is throwaway.
- Running the backend locally now requires `docker/.env` (already committed) and the
  fullstack compose — a heavier prerequisite than the old `services.yml`, but it is the
  same stack the project already maintains and runs.
- Resolves the #164 dependency: the local real-stack wording now has a single source of
  truth (the fullstack infra).
- Memory `project_mailpit-docker-intentional` and `project_dev-postgres-no-host-port`
  become stale once Step 2 lands — update them then.

## Implementation touch-list (Step 2)

- `src/main/resources/application-local.yml` — url 5432 → 54035.
- `.claude/skills/run-backend/SKILL.md` — prerequisite compose + wording.
- `.claude/tech/java-spring/infrastructure.md` — "Run (local)" prerequisite + port.
- `.claude/tech/java-spring/templates/infrastructure/infrastructure-details.md` — port
  table (5432), run command, file table, dev-mail note.
- Delete `docker/services.yml`, `docker/postgres.yml`.
