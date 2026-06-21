# /continue Dispatch Reference

Reference material for the `/continue` skill: dispatch tables, pre-commit checklist, and stories.md update rules. The skill's algorithm references this file.

## Work Unit Dispatch

Each progress.md checkbox maps to sub-skills. Dispatch per `workflow.md` sequences. **This table applies equally to stories AND tasks — never skip `/test-review` or `/refactor` for task steps.**

| Checkbox | Sub-skills |
|----------|-----------|
| Spec items (`interview`, `story`, `mockups`, `api-spec`, `test-spec`) | `/{item}` then commit |
| `design` | `/design-preview` → user approves (optionally with ADR) or `/architecture` → commit (if ADR produced) |
| `red-*` (acceptance, usecase, domain, adapter, playwright, frontend, frontend-api) | `red-agent.md` → `/test-review` → `/refactor` → commit |
| `green-usecase`, `green-domain`, `green-adapter X` | `green-agent.md` → `/refactor` → `/test-coverage {module} --focus` → commit |
| `adapters-discovery` | Load `.claude/templates/workflow/adapter-discovery-checklist.md`, run all 3 checks (ports, exceptions, response shape), mark `[x] adapters-discovery`, insert concrete `red-adapter X` / `green-adapter X` steps (or `[S]`) → commit progress.md |
| `green-acceptance` | Run inline (no subagent): ensure the shared test DB is up (see `.claude/tech/java-spring/infrastructure.md` → "Test Database"), read `green-agent.md` workflow, load acceptance implementation template, enable the disabled test (remove disable marker — only allowed test change), run acceptance tests, verify GREEN → commit |
| `green-frontend`, `green-frontend-api` | `green-agent.md` → `/refactor` → commit |
| `green-playwright` | `/run-backend` → `/run-frontend` → `green-agent.md` (remove-marker-only: no production code, no Statements changes, no backend changes — if test fails, STOP and report) → commit |
| `align-design` | Build component → `/align-design` → `/design-review` (MANDATORY) → `/refactor` → `/align-design` verify-only → `/test-coverage frontend --focus` → commit |
| `demo` | `/demo {scenario_test_class}` then progress-only commit |
| `refactor usecase` / `refactor (...)` | Apply change then run affected tests then commit |

## Pre-Commit Checklist

Before committing, verify: (1) primary skill ran, (2) `/test-review` ran (red phases), (3) `/refactor` ran (all phases except `green-acceptance`/`green-playwright`/`demo`/spec items), (4) static analysis passes, (5) IDE inspections pass on changed files. If `/refactor` was skipped -- run it before committing.

**Static analysis** (step 4): run the checker for each stack the work unit touched — do NOT skip this step, it catches issues the agents don't check.
- **Backend files changed** (`.java`): `./mvnw checkstyle:check -pl . -q && ./mvnw pmd:check -pl . -q` — catches missing Javadoc, PMD violations.
- **Frontend files changed** (`frontend/**`): `npm run lint` from `frontend/` (`eslint . && prettier --check .`) — the same gate as the CI "Frontend Lint" job. Auto-fix with `npm run lint:fix`, then re-run `npm run lint` to confirm clean. See `.claude/tech/vue-ts/infrastructure.md` → "Static Analysis (Pre-Commit)".

Run whichever apply to the files in this work unit; run both when it touched both stacks. If violations are found, fix them before committing.

**IDE inspections** (step 5): for each non-spec file created or modified in this work unit, call the IntelliJ MCP tool `mcp__idea__get_file_problems` (`errorsOnly: false`, pass `projectPath`). It catches issues the static analyzers miss (e.g. `AutoCloseable` used without try-with-resources, redundant code). Fix real findings; for an intentional false positive add a narrowly-scoped `@SuppressWarnings("<id>")` with a comment explaining why. Skip only when the IDEA MCP server is unavailable (e.g. headless/cron runs) — then note it was skipped. Applies to code files only, not markdown/spec.

## Sub-Skill Dispatch

ALL sub-skills dispatch via Agent tool for context isolation:

| Sub-skill | Dispatch method |
|-----------|----------------|
| `red-*` | `Agent tool` (subagent_type: `red-agent`) — pass layer, story folder path, scenario name, and ADR content (if loaded) |
| `green-*` (except `green-acceptance`) | `Agent tool` (subagent_type: `green-agent`) — pass layer, story folder path, scenario name, and ADR content (if loaded) |
| `green-acceptance` | **Inline** — no subagent. Main agent ensures the shared test DB is up (tech binding → "Test Database"), reads `green-agent.md`, loads acceptance template, enables the test, runs it. Full visibility for user. |
| `/refactor` | `Agent tool` (subagent_type: `refactor-agent`) |
| `/test-review` | `Agent tool` (subagent_type: `test-review-agent`) |
| `/test-coverage` | `Agent tool` (subagent_type: `coverage-agent`) |

Derive the layer from the checkbox (e.g., `red-adapter db` → layer `db`, `green-usecase` → layer `usecase`). Both red-agent and green-agent receive: layer, story folder path, scenario name, and ADR content (if loaded in step 6). The agent resolves test files and templates from its own workflow.

**CHAINING: After each sub-step completes (Agent tool return), echo a 1-2 line status summary (agent name, outcome, pass/fail counts) to the user, then immediately dispatch the next sub-step. Do NOT wait for user input between sub-steps — the echo is informational only.**

**AGENT LOG: Before the first agent dispatch, clear the log: `> infrastructure/agent-progress.log`. After the final commit, include the log contents in the stop-and-report summary.**

**LOG REMINDER: Every time you dispatch a sub-agent (Agent tool call), output this line immediately before the call:**
```
> Dispatching {agent-name}. Live progress: tail -f infrastructure/agent-progress.log
```
**This reminds the user where to watch. The line appears in conversation output before the agent starts, so the user can open a terminal and tail the log while the agent works.**

## Updating stories.md

After updating `progress.md` for a **story** (not tasks), update the story's row in `ProductSpecification/stories.md` to reflect current phase status. The file has columns: `Spec | Backend | Integration | Frontend | Security | Load | Infra | Status`.

**Phase column values** — derive from `progress.md` sections:
- `✅` — all checkboxes in that section are `[x]` or `[S]`
- `🔧` — section has at least one `[~]` or `[ ]` checkbox (work in progress or next up within the current active lifecycle phase)
- `—` — section exists but no work started yet (all `[ ]`)
- `n/a` — phase not applicable (test spec file says "No tests" or story has no scenarios for that phase)
- `·` — no story folder or no progress file exists

**Tests and % columns** — recount after every progress.md update:
- **Total** = number of `### ` scenario headings in progress.md (not `## ` section headers)
- **Done** = scenarios where ALL checkboxes are `[x]` or `[S]` (no `[ ]` or `[~]` remaining)
- **%** = `round(done / total * 100)`
- Format: `done/total` in Tests column, `N%` in % column
- Do NOT add a Total row — it causes merge conflicts

**Story completion** — when all scenarios reach 100% (Tests column = `total/total`, % = `100%`), move the story row from the **In Progress** table to the **Done** table in `ProductSpecification/stories.md`. Keep all column values intact.

**When to update**: after every progress.md commit for a story. Include `ProductSpecification/stories.md` in the same commit.

**Lifecycle ordering**: Spec → Backend → Integration → Frontend → Security → Load → Infra. A phase becomes `🔧` when it is the current active phase (has `[~]` items) OR when it still has `[ ]` items and all prior phases are `✅`.
