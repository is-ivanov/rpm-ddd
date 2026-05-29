# Java/Spring Infrastructure Idioms

Tech binding for `infrastructure.md`. Load alongside the universal rules.

## Health Check

- Spring Actuator endpoint (fixed port 8080, no port isolation): `curl http://localhost:8080/actuator/health`

## Run (local)

Backend main class `by.iivanov.rpm.RpmDddApplication`, Spring profile `local`, fixed port 8080.

**Prerequisite — dev Postgres:** the `local` profile connects to `localhost:5432/rpm_ddd`. Start it first (idempotent):

```bash
docker compose -f docker/services.yml up -d
```

**Run the backend, two options:**

1. **CLI:** `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
2. **IntelliJ MCP:** run the `App-Local` run configuration via `mcp__idea__execute_run_configuration` (`configurationName: "App-Local"`). Confirm the IDE/MCP tools are actually available before relying on this — they require the IDE to be open.

## Process Safety

- Never kill by executable name: `taskkill //IM java.exe` — use port-based stop scripts instead.
- Maven (the project's build tool) has no long-lived daemon — there is nothing to "stop". Just let `./mvnw` invocations exit on their own; never globally terminate Java/Maven processes (that would kill parallel sessions). See the universal "never kill processes by executable name" guidance in `.claude/rules/infrastructure.md`.

## Config Fallback Syntax

Each file type has its own fallback pattern:
- Spring YAML: `${VAR:fallback}` (colon only)
- Docker Compose, shell scripts: `${VAR:-fallback}` (colon-dash)

## Static Analysis (Pre-Commit)

- Run before every commit: `./mvnw checkstyle:check -B` and `./mvnw pmd:check -B` (both are listed as verification commands in the `ProductSpecification/technology.md` Conventions table).
- If violations are found, fix them before committing.

## Acceptance Tests

- Backend acceptance/integration tests run via Maven: `./mvnw verify -B` (see `ProductSpecification/technology.md` Conventions table).
- They require the shared test DB — see "Test Database" below.

## Test Database (DB-tagged tests)

DB-backed tests carry `@Tag("db")` (acceptance/integration tests, `db` adapter tests). At JUnit-platform startup `DbContainerTestExecutionListener` resolves the datasource: it first tries the shared local Postgres at `jdbc:postgresql://localhost:54034/`, recreates the `rpm_ddd` schema, and reuses it (fast). Only if that connection fails does it start a Testcontainer — a slow cold-start paid on **every** run.

**Before running any DB-tagged test, ensure the shared test DB is up. Both ways are idempotent (re-running on a live stack is a no-op):**

1. **Preferred — IntelliJ MCP available:** run the `Infra-Tests-Up` run configuration via `mcp__idea__execute_run_configuration` (`configurationName: "Infra-Tests-Up"`, `projectPath` = project root, `waitForExit: true`, `timeout: 90000`). Confirm the IDE/MCP tools are actually available before relying on this — they require the IDE to be open.
2. **Fallback — no IDE/MCP (headless/CI):**
   ```bash
   docker compose --env-file docker/.env -f docker/infra-tests.yml up -d --wait
   ```

- **Leave it running across runs** — never stop/`down` it. The next run reuses it; the listener recreates the schema each time, so reuse is safe (tmpfs data is ephemeral by design). On a fresh start allow a few seconds for the healthcheck.
- Only start (idempotent) — never remove it. Parallel sessions/worktrees share this single test DB on the fixed `54034` port by design (see Process Safety).
