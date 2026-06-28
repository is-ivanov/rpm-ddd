# Infrastructure Details

Detailed reference for local development infrastructure (java-spring profile).
For rules and quick reference, see `.claude/rules/infrastructure.md`.
For the tech binding (health check, run, test-DB procedure), see `.claude/tech/java-spring/infrastructure.md`.

## Topology

Single Maven module (no `backend/` directory, no sub-modules). Modularity is enforced by
Spring Modulith + ArchUnit, not by Maven modules. Ports are **fixed** — there is no
per-repo-instance port isolation and no `infrastructure/scripts/` or `infrastructure/.env`.

| Service | Port | Where |
|---------|------|-------|
| Backend (Spring Boot, profile `local`) | 8080 (fixed) | `by.iivanov.rpm.RpmDddApplication` |
| Dev Postgres (persistent, named volume) — `local` run | 54036 | `docker/infra-local.yml` |
| Fullstack Postgres (shared, tmpfs) — fullstack E2E | 54035 | `docker/infra-fullstack-tests.yml` |
| Test Postgres (shared, tmpfs) — db-tagged tests | 54034 | `docker/infra-tests.yml` |

## Run the Backend (local)

Profile `local` connects to `jdbc:postgresql://localhost:54036/rpm_ddd` (user/pass `postgres`/`postgres`),
the persistent dev stack. Start that stack first (idempotent — Postgres on `54036`, Mailpit
on `1025`/`8025`), then run the app.

```bash
docker compose -f docker/infra-local.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Alternatively, run the IntelliJ `App-Local` run configuration via MCP
(`mcp__idea__execute_run_configuration`, `configurationName: "App-Local"`) — requires the IDE
to be open. `App-Local` runs `Infra-Local-Up` as a before-launch task, so it starts the dev
stack (`docker/infra-local.yml`) automatically — no separate compose step is needed.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Docker Composes

| File | Purpose | Postgres | Notes |
|------|---------|----------|-------|
| `docker/infra-local.yml` | Dev stack for the `local` profile | port 54036, persistent named volume | bundles Mailpit (`1025`/`8025`); stock tuning (durable, autovacuum on); self-contained image default (no `--env-file`); started via the `Infra-Local-Up` IDEA run config (auto before-launch of `App-Local`) |
| `docker/infra-fullstack-tests.yml` | Fullstack E2E real-stack infra | port 54035, tmpfs (ephemeral) | bundles Mailpit (`1025`/`8025`); tuning vars in `docker/.env`; shared-first, never torn down |
| `docker/infra-tests.yml` | Shared TEST Postgres for DB-tagged tests | port 54034, tmpfs (ephemeral) | tuning vars in `docker/.env`; started via the `Infra-Tests-Up` IDEA run config |

Start the shared test DB (idempotent — leave it running across runs):

```bash
docker compose --env-file docker/.env -f docker/infra-tests.yml up -d --wait
```

## Tests

```bash
./mvnw test                          # full unit suite (parallel)
./mvnw test -Dtest='*ClassName*'     # single test class
./mvnw test -Dgroups=db              # DB-tagged tests only (needs shared test DB up)
./mvnw verify -B                     # acceptance / integration (full CI build)
```

DB-tagged tests (`@Tag("db")`: acceptance, integration, `db` adapter tests) resolve the shared
test Postgres at `localhost:54034` first and fall back to a Testcontainer only if it is unreachable.
For the full datasource-resolution and start procedure, see the "Test Database" section in
`.claude/tech/java-spring/infrastructure.md` — do not duplicate it here.

## Mail (dev)

The dev mail server is **Mailpit**, bundled in `docker/infra-local.yml` (SMTP on `1025`, web
UI on `8025`). The `local` profile sends to `localhost:1025`, so starting the dev stack above
also provisions dev mail. The fullstack test stack bundles its own Mailpit on the same ports
— do not run both stacks at once.
