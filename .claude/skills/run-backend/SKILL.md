---
name: run-backend
description: Run the backend application. Use when user wants to start the backend server or mentions /run-backend command.
---

# Run Backend Application

Concrete commands (run command, dev Postgres prerequisite, health endpoint) live in the tech binding `.claude/tech/java-spring/infrastructure.md` ("Run (local)" and "Health Check"). Read it first; this skill only orchestrates.

## Prerequisite

The `local` profile shares the fullstack real-stack infra: Postgres at `localhost:54035`
and Mailpit at `localhost:1025`. Start it first (idempotent — no-op if already up):

```bash
docker compose --env-file docker/.env -f docker/infra-fullstack-tests.yml up -d --wait
```

Liquibase migrations run automatically on boot.

## Action

Start the backend (main class `by.iivanov.rpm.RpmDddApplication`, profile `local`, fixed port 8080). Use the option that matches the environment:

1. **CLI (default):** run with `run_in_background: true` so the server keeps serving:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
2. **IntelliJ MCP (only when IDE/MCP is available):** `mcp__idea__execute_run_configuration` with `configurationName: "App-Local"`, `waitForExit: false`.

Capture the background task ID (CLI) or run-config handle (IDE) — `/stop-backend` needs it to stop only the process you started.

## Output

Poll readiness until the health endpoint returns 200, then report:

```bash
curl http://localhost:8080/actuator/health
```

Report: server ready at `http://localhost:8080`, plus the task ID / run handle to use with `/stop-backend`.
