---
name: test-all
description: Run all tests - unit tests in parallel, then acceptance tests with backend. Use when user wants to run the full test suite or mentions /test-all command.
---

# Run All Tests

Runs the complete test suite: unit tests in parallel, then acceptance tests.

## Setup

Read `ProductSpecification/technology.md` Conventions table for:
- **Backend test command**
- **Frontend test command**
- **Acceptance test command**

This is a single-module Maven project — there are no adapter module directories to discover. The full backend unit suite runs in one command; there is no per-module fan-out.

## Workflow

### Phase 1: Run Unit Tests in Parallel

There are no separate backend modules to fan out across — the whole backend unit suite runs in one command. Run these in parallel using multiple Bash tool calls in a single message:
- Backend unit tests (usecase + all adapters): `{Backend test command}` — the single-module suite covers `*.application` usecase tests and `*.infrastructure.*` adapter tests together
- Frontend tests: `{Frontend test command}`

Wait for both to complete. If either fails, report failures and STOP.

### Phase 2: Start Backend

Use the Skill tool to invoke `/run-backend`:
```
Skill tool: skill="run-backend"
```

Wait for backend to start.

### Phase 3: Run Acceptance Tests

```
{Acceptance test command} for backend tests
```

### Phase 4: Stop Backend

Use the Skill tool to invoke `/stop-backend`:
```
Skill tool: skill="stop-backend"
```

## Output

Report summary:
- Backend unit tests: PASS/FAIL (single-module suite — usecase + adapters)
- Frontend unit tests: PASS/FAIL
- Acceptance tests: PASS/FAIL (with details if failed)
- Overall: PASS/FAIL
