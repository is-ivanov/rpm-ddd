---
name: test-acceptance
description: Run acceptance tests (backend API or frontend UI). Use when user wants to run E2E acceptance tests or mentions /test-acceptance command.
---

# Run Acceptance Tests

## Pre-Checks

Verify required services are up **before** running tests. Use the health-check command and endpoint path from the tech binding (`.claude/tech/{backend}/infrastructure.md` → "Health Check") and the `technology.md` Conventions table (Health endpoint).

### Backend (always required)

Run the backend health check from the tech binding.

- Healthy (`200` / `UP`) → OK.
- Unavailable or non-200 → start backend first: `Skill tool: skill="run-backend"`. Wait for startup.

### Frontend (required when argument is `frontend` or a frontend test class)

Check the frontend dev server is reachable (the run/dev command and port resolution are in the `frontend` and `browser-testing` tech bindings).

- Reachable → OK.
- Unavailable → start frontend first: `Skill tool: skill="run-frontend"`. Wait for startup.

## Action

Resolve the command from the `technology.md` Conventions table — never hardcode a build tool here:

- **Backend** (`backend`, a backend test class, or default) → "Acceptance test command" under the **Backend** conventions.
- **Frontend** (`frontend` or a frontend test class) → "Acceptance test command" under the **Browser Testing** conventions.

Map the argument to the command:
- `backend` / no args → backend acceptance command, full suite.
- `backend {ClassName}` or bare `{ClassName}` → backend acceptance command scoped to a single class. Use the build tool's single-test filter (see the tech binding's "Acceptance Tests" / test-execution notes for the exact flag).
- `frontend` → frontend acceptance command, full suite.
- `frontend {ClassName}` → frontend acceptance command scoped to a single class.

DB-tagged backend acceptance tests need the shared test DB up — see the tech binding's "Test Database" before running.

**Always pass the test class name** when running a specific test — never run the full suite just to check one test. The full suite is the "long" path; keep it for full runs only.

## Execution Strategy

Follow `tdd-rules.md` "Stop on first failure" protocol:

1. **Launch in background:** `run_in_background: true` — note the output file path from the result. Track lines already shown so each poll prints only new output.
2. **Poll with separate Bash calls:** Make repeated **individual** Bash calls (NOT a loop inside one call — that hides output until the loop finishes). Each call prints only the new lines since the last poll, filtered to progress/result markers, and reports whether the build's success or failure marker has appeared. Use the build tool's own progress and terminal markers (see the tech binding's "Acceptance Tests" / test-execution notes for the exact success/failure strings) — do not assume a specific build tool here.
   After each call: advance the seen-line counter, check whether the run finished, and if still running wait ~5s then poll again. Repeat until the run finishes.
   **CRITICAL:** Each check must be a separate tool call so the user sees output immediately.
3. **If the build succeeded** → read output file, report pass counts. Done.
4. **If the build failed** → stop suite (`TaskStop`), read stack trace from the output file, investigate root cause. Do NOT collect further failures.
   - **Compilation error** → read error lines, report immediately. No need to check infrastructure.
   - **Infrastructure error** (driver exception, connection reset) → re-check backend/frontend health.
   - **Application error** (assertion failure, wrong status) → investigate and fix.

## Output

Report the test results from output. Always include pass/fail counts.
