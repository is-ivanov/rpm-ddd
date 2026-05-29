# Infrastructure

## Local Development

Services (backend, frontend, supporting containers) run locally on their configured ports. The concrete run, stop, and health-check commands — and the port numbers — are tech-specific. **Always defer to the tech binding (`.claude/tech/{concern-value}/infrastructure.md`) for the exact commands; never hardcode commands or port numbers in this universal file, in skills, agents, or ad-hoc commands.**

- **Start, stop, and health-check via the tech binding.** Look up the run command, stop command, and health endpoint there rather than improvising — the binding is the single source of truth for how this stack is launched and probed.
- **NEVER kill processes by executable name.** This kills ALL instances system-wide, including other Claude sessions and user tools. To stop a specific service, target only the specific PID you started (or the binding's dedicated stop command).
- **NEVER remove Docker containers you didn't start.** Multiple Claude sessions may run in parallel, each with its own supporting containers. If `docker ps` shows containers you don't recognize, leave them alone — only manage the containers you started yourself.
- **NEVER run build-daemon stop commands** for unknown processes — they kill ALL daemons system-wide, breaking test runs and backends in parallel Claude sessions.

Config files use fallback patterns. **Syntax differs by file type** — see tech binding for specific syntax per framework.

## Reusable Test Infrastructure

When the test harness can connect to an already-running local service instead of provisioning an ephemeral container per run, ensure that service is up **before** a test run that needs it, and leave it running across runs. Never tear it down at the end — the next run reuses it and skips container cold-start. Fall back to per-run ephemeral containers only when the shared service is unavailable.

- Starting must be **idempotent** — running the start step on an already-live service is a no-op.
- Only start it (never stop/remove it). Other parallel sessions may share the same shared service.
- The concrete start command/port and which tests need it are tech-specific — see the tech binding's "Test Database".
