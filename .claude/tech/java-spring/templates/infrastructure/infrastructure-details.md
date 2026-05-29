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
| Dev Postgres | 5432 | `docker/services.yml` (extends `docker/postgres.yml`) |
| Test Postgres (shared, tmpfs) | 54034 | `docker/infra-tests.yml` |

## Run the Backend (local)

Profile `local` connects to `jdbc:postgresql://localhost:5432/rpm_ddd` (user/pass `postgres`/`postgres`).
Start the dev Postgres first (idempotent), then run the app.

```bash
docker compose -f docker/services.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Alternatively, run the IntelliJ `App-Local` run configuration via MCP
(`mcp__idea__execute_run_configuration`, `configurationName: "App-Local"`) — requires the IDE to be open.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Docker Composes

| File | Purpose | Postgres | Notes |
|------|---------|----------|-------|
| `docker/services.yml` | Dev Postgres for the `local` profile | port 5432, DB `rpm_ddd`, `postgres`/`postgres` | extends `docker/postgres.yml`; persistent |
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

The chosen dev mail server is **Mailpit**, provisioned via Testcontainers (per
`ProductSpecification/technology.md`). There is currently no mail service in `docker/services.yml`.
