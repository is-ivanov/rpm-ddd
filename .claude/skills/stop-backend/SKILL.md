---
name: stop-backend
description: Stop the running backend application. Use when user wants to stop the backend server or mentions /stop-backend command.
---

# Stop Backend Application

There is no stop script. Stop ONLY the backend process you started — never kill Java by executable name (that breaks parallel sessions). See the "Process Safety" section of the tech binding `.claude/tech/java-spring/infrastructure.md`.

## Action

Use the handle captured by `/run-backend`:

1. **Started via CLI (`run_in_background`):** stop that background task with `TaskStop` using its task ID. If you only have the OS PID, kill that specific PID — never `taskkill //IM java.exe` or any by-name kill.
2. **Started via IntelliJ MCP (`App-Local`):** stop that run configuration through the IDE.

If you don't know which process you started, do NOT guess and kill processes by name. Check the health endpoint (`curl http://localhost:8080/actuator/health`) to confirm whether a backend is up, and stop only the specific process/run you launched.

## Output

Report: backend stopped (which process/run was stopped), or that no backend you started was running.
