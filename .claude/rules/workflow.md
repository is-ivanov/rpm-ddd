# Development Workflow

## Lifecycle

Every story follows: **interview → spec → backend scenarios → integration scenarios → frontend scenarios → security scenarios → load scenarios → infrastructure scenarios → full-stack journey**.

Each phase ends with a **Per-Phase Extended Gate** (below): once a phase's last core scenario is done, its extended cases (`tests/extended/NN_<Category>_Extended.md`) are reviewed for promote/defer before the next phase starts — not deferred wholesale to story close.

The trailing **full-stack journey** phase is the top-tier E2E assessment (see `tdd-rules.md` → "Top-Tier Full-Stack Journey Assessment"): its verdict is produced during the **spec** phase by `/test-spec` (as `tests/07_FullStack_Journey.md`) and executed once at the end as a tracked `fullstack-journey` step (see "Full-Stack Journey Step" below).

**High-level progress** is tracked in `ProductSpecification/stories.md` — three tables: **In Progress**, **Backlog**, and **Done**. Phase columns (Spec, Backend, Integration, Frontend, Security, Load, Infra) per story. The `/continue` skill updates it after each work unit commit. Phase values: ✅ done, 🔧 in progress, — not started, · no story folder yet. When a story reaches 100% (all scenarios done), run the **Story Completion Gate** (below) and then move its row from the **In Progress** table to the **Done** table.

**Backlog** stories have all `·` columns (no folder yet). When `/continue N` targets a Backlog story, auto-promote it: move the row from **Backlog** to **In Progress** in `ProductSpecification/stories.md` before starting work.

Spec phase: `/interview` → `/story` → `/mockups` → `/api-spec` → `/test-spec` (one at a time, review each before proceeding).

## Per-Phase Extended Gate

**Extended cases are reviewed at the end of the phase they belong to — not deferred wholesale to story close.** Each test category produces its own extended file (`tests/extended/NN_<Category>_Extended.md`), and each maps to a phase: `01_API`→Backend, `06_Integration`→Integration, `02_UI`→Frontend, `05_Security`→Security, `03_Load`→Load, `04_Infrastructure`→Infrastructure. When the **last core scenario of a phase** is completed (every other checkbox in that phase's section is `[x]`/`[S]`), `/continue` MUST **STOP** before starting the next phase (a deliberate exception to "Atomic Work Units: never pause") and run the extended gate for that phase's extended file.

**The agent only surfaces and recommends; the user decides** — identical mechanics to the Story Completion Gate. Present every case in that phase's `NN_<Category>_Extended.md` with a one-line promote/defer recommendation, ask the user per item, then execute:

1. **Promote** — append the case as a new scenario block in `progress.md` *within the same phase* (full TDD steps per that phase's scenario sequence). The phase is not finished while a promoted scenario has open checkboxes — continue in-phase before advancing to the next phase.
2. **Defer** — log the case to `improvements.md` as an `Open` item (id `I{n}`) and mark its entry in the `### Extended` block of `progress.md` as reviewed, so the Story Completion Gate does not re-surface it.

If the phase's extended file is empty or absent, record "no extended items for `<phase>`" and proceed. This gate is the per-phase analog of the Story Completion Gate; together they guarantee every extended case gets a user decision at the earliest meaningful point — when its phase's core work is still fresh — instead of all at once at story close. The reviewed-vs-open marking in the `### Extended` block is what lets the two gates share state without double-asking.

## Story Completion Gate

**A story may NOT move to the Done table until its extended test cases and improvements backlog have been reviewed — and the promote/defer decision is the user's, not the agent's.** `/test-spec` generates `tests/extended/*_Extended.md` files (header: *"Implement after core tests pass"*) and QA fills `improvements.md` during the story — both are deferred work that has no per-scenario checkbox, so without a gate they are silently orphaned when the story closes (this is the root cause of issue #189).

**The agent only surfaces and recommends; the user decides.** This gate is the backstop to the **Per-Phase Extended Gate** (above): extended cases already decided at their phase gate are marked reviewed in the `### Extended` block and are NOT re-surfaced here. When the work unit it just completed was a story's final scenario, `/continue` MUST **STOP** before moving the row to Done (this is a deliberate exception to "Atomic Work Units: never pause") and present, for the user to decide:

- every still-unreviewed case in `tests/extended/*_Extended.md` (any whose phase gate was skipped — e.g. older stories, or a phase that ended before this rule existed), and
- every `Open` item in the story's `improvements.md`,

each annotated with a one-line agent recommendation (promote / defer + why). **The agent never auto-promotes a case, never auto-closes the story, and never moves the row to Done on its own** — it waits for the user's per-item decision.

After the user decides, the agent executes the decisions:

1. **Promote** — append the case as a new scenario block in `progress.md` (with full TDD steps per the relevant scenario sequence). The story is NOT done while a promoted scenario has open checkboxes — work continues normally.
2. **Defer** — log the case to the story's `improvements.md` backlog as an `Open` item (id `I{n}`), recording why it is deferred, and mark the corresponding entry in the `### Extended` block of `progress.md` (see Bootstrapping) as reviewed.
3. **Improvements** — for each `Open` item the user chose to defer rather than promote, leave it `Open` with an owner/rationale, or (real regression) raise it as a bug task + issue. The story closes with a reviewed backlog, not an unexamined one.

Only once every extended case and `Open` item has a user decision does the agent move the row to Done. The completion commit message records what the user chose to promote vs defer; if `tests/extended/` is empty and `improvements.md` has no `Open` items, it states "no extended/backlog items to review."

This gate is the owner and trigger for the *"implement after core"* instruction. `/continue` enforces it (the STOP above) when it detects that the work unit it just completed was a story's final scenario.

## Backend Scenario Sequence

This sequence is for a scenario tagged **`Level: L1 acceptance`** (a happy-path, full-context behavior). A scenario tagged **`Level: L2 web-slice`** (a validation / error / business-exception→HTTP-status category) does NOT use this sequence — its domain rule is already covered at a lower level, so it is driven by a `@WebTest` web-slice test, not an acceptance test: `red-adapter rest` → `design` → `green-adapter rest`, with `red/green-usecase`, `red/green-domain` and `green-acceptance` marked `[S]` (already covered / no Level-1 test for an error category). The `**Level:**` tag is authored in the test spec (`test-spec-format.md`) and consumed by Bootstrapping below.

**Dispatch note — `red-*`/`green-*` are NOT slash-command skills.** A `red-*` or `green-*` phase step (`red-acceptance`, `red-usecase`, `red-adapter`, `red-playwright`, `red-frontend`, `red-frontend-api`, and their `green-*` counterparts) is dispatched via the **Agent tool** — `subagent_type: red-agent` for red phases, `green-agent` for green phases — passing the layer derived from the checkbox (`red-adapter db` → layer `db`). There is no `Skill(red-playwright)` / `Skill(green-usecase)` etc.: those skills do not exist, and calling them fails with "Unknown skill". `green-acceptance` and `green-playwright` are run by the main agent **inline** (remove-marker-only), not as a subagent. Only the genuine skills named below with a leading slash — `/test-review`, `/refactor`, `/design-preview`, `/architecture`, `/test-coverage`, `/align-design`, `/design-review`, `/run-backend`, `/run-frontend`, `/demo` — are invoked via the Skill tool. In the sequences below, `→ red-agent` / `→ green-agent` means "dispatch that phase via the Agent tool"; see `.claude/templates/workflow/continue-dispatch.md` → "Sub-Skill Dispatch" for the authoritative table.

For each scenario in `tests/01_API_Tests.md`:

1. `red-acceptance` → `red-agent` → `/test-review` → `/refactor` (MANDATORY) → commit
2. `design` → `/design-preview` → user approves (optionally with ADR) or escalates to `/architecture` → commit (if ADR produced)
3. `red-usecase` → `red-agent` → `/test-review` → `/refactor` (MANDATORY) → commit
4. `green-usecase` → `green-agent` → `/refactor` (MANDATORY) → `/test-coverage usecase --focus` → commit
4a. `red-domain` / `green-domain` (OPTIONAL) — only when coverage-agent finds uncovered domain branches after step 4, OR when design-preview identifies domain objects with testable logic (value object validation, entity state transitions, domain policies). Follows same TDD cycle: `red-domain` (red-agent) → `/test-review` → `/refactor` → `green-domain` (green-agent) → `/refactor` → commit. Otherwise `[S]`.
5. `adapters-discovery` → adapter discovery: identify ports and map to adapters, mark `[x] adapters-discovery`, insert concrete `red-adapter X` / `green-adapter X` steps below it (or `[S]` if no new adapters), commit progress.md
6. `red-adapter X` → `red-agent` (layer `X`) → `/test-review` → `/refactor` (MANDATORY) → commit (one per port)
7. `green-adapter X` → `green-agent` (layer `X`) → `/refactor` (MANDATORY) → `/test-coverage {adapter} --focus` → commit (one per port)
8. `green-acceptance` → run inline (green-agent workflow, remove-marker-only) → commit

The `[ ] adapters-discovery` checkbox is a gate — it must be resolved before any subsequent step executes. The full procedure is in `.claude/templates/workflow/adapter-discovery-checklist.md`.

## Integration Scenario Sequence

For each scenario in `tests/06_Integration_Tests.md` (if exists): same TDD cycle as backend scenarios above. Integration scenarios cover cross-cutting concerns: scheduled jobs, webhook idempotency, resilience, and email triggers.

**Scheduled jobs require a wiring scenario, not just a logic scenario.** When a story introduces a scheduled/recurring job, the integration scenario set MUST include a production-schedule wiring scenario in addition to any direct-invocation logic scenarios. A job verified only by direct invocation leaves its scheduling wiring untested and can silently never run in production. For what the wiring scenario must assert, see `tdd-rules.md` → "Scheduled / Recurring Jobs"; for the concrete test mechanism, see the `.claude/tech/{backend}/templates/scheduling/` binding.

## Frontend Scenario Sequence

For each scenario in `tests/02_UI_Tests.md`:

1. `red-playwright` → `red-agent` → `/test-review` → `/refactor` (MANDATORY) → commit
2. `red-frontend` → `red-agent` → `/test-review` → `/refactor` (MANDATORY) → commit
3. `green-frontend` → `green-agent` → `/refactor` (MANDATORY) → commit
4. `red-frontend-api` → `red-agent` → `/test-review` → `/refactor` (MANDATORY) → commit
5. `green-frontend-api` → `green-agent` → `/refactor` (MANDATORY) → commit
6. `align-design` → Build component → `/align-design` → `/design-review` (MANDATORY) → `/refactor` (MANDATORY) → `/align-design` verify-only → `/test-coverage frontend --focus` → commit
7. `green-playwright` → `/run-backend` → `/run-frontend` → run inline (green-agent workflow, remove-marker-only) → commit
8. `demo` → `/demo {test_class}` → progress-only commit


## Full-Stack Journey Step

After the frontend scenarios are green, a single story-level `fullstack-journey` step executes the verdict recorded in `tests/07_FullStack_Journey.md` (produced by `/test-spec`). It runs once per story, not per scenario, because the journey needs the real grid/page UI and the page Statements built during the frontend phase.

- **`extend`** → edit the existing full-stack journey spec to weave in this story's critical path (reusing the page Statements from the frontend phase), run it against the real stack, commit. Mark `[x] fullstack-journey`.
- **`new`** → add a new `*.fullstack.spec.ts` journey for the independent lifecycle, run it, commit. Mark `[x] fullstack-journey`.
- **`no-impact`** → mark `[S] fullstack-journey (no-impact: <reason from 07_FullStack_Journey.md>)`. No spec change.

The concrete mechanics (journey location, suffix, how to extend vs create, real-stack run recipe) are in the browser-testing tech binding (`.claude/tech/{browser-testing}/tdd.md`). This step is the per-story analog of the Scheduled-jobs wiring scenario: a recorded artifact plus a tracked checkbox so the assessment can never be silently skipped — the **Story Completion Gate** will not close a story with an open `fullstack-journey` checkbox.

## Security Scenario Sequence

For each scenario in `tests/05_Security_Tests.md` (if exists): same TDD cycle as backend scenarios above. Security scenarios cover OWASP concerns: injection, XSS, CSRF, rate limiting, mass assignment, and input validation.

## Load Scenario Sequence

For each scenario in `tests/03_Load_Tests.md` (if exists): same TDD cycle as backend scenarios above. Load scenarios cover performance and volume concerns: response time baselines, concurrent request handling, and large data set behavior.

## Infrastructure Scenario Sequence

For each scenario in `tests/04_Infrastructure_Tests.md` (if exists): same TDD cycle as backend scenarios above. Infrastructure scenarios cover resilience concerns: database failure handling, recovery after outages, and external service unavailability.

## Infrastructure & Port Configuration

Moved to `.claude/rules/infrastructure.md` (rules) and `.claude/tech/{backend}/templates/infrastructure/infrastructure-details.md` (full details).

## Progress Tracking

Each story has a progress file: `ProductSpecification/stories/NN-story-name/progress.md`.

### Status Markers

- `[x]` — done
- `[~]` — in-progress (current step)
- `[ ]` — pending
- `[S]` — skipped

### Reading Progress

When the user says "continue working on story X" or runs `/continue X`:
1. Read `ProductSpecification/stories/NN-story-name/progress.md`
2. Find the first `[ ]` or `[~]` entry — that is the next work unit
3. Report current status and what step will execute next

### Updating Progress

After completing a work unit:
1. Change `[~]` to `[x]` for the completed step
2. Change the next `[ ]` to `[~]` if continuing
3. Commit the progress file with the work unit commit

**Progress entries are TERSE — one line each.** A checkbox carries only its status plus, at most, a short reference: the test class/ADR it produced and a link to the scenario's summary file (`see summaries/<scenario-slug>.md`). Nothing more. `progress.md` tracks *state* (which work unit runs next), not narrative.

The "why" — decisions reached in discussion, surprises, prediction mismatches, quirks a future scenario will hit — belongs in the scenario summary (written by `/handoff`) and, when enduring, in `carryover.md`; never in a checkbox parenthetical. Mechanical phase noise — lint/static-analysis status, refactor steps applied, RED prediction tables, lists of files created — is NOT recorded in `progress.md` at all; it lives in git history and the commit message. Do not copy a phase agent's full report into the checkbox line. If an entry grows past one line, the detail belongs in a summary or the commit, not here.

### Bootstrapping

If no `progress.md` exists, create one by:
1. Detecting spec artifacts in the story directory:
   - `interview`: check if `interview.md` exists
   - `story`: check if `NN_StoryName.md` exists
   - `mockups`: check if `mockups/` has files
   - `api-spec`: check if `endpoints.md` exists
   - `test-spec`: check if `tests/01_API_Tests.md` exists
   - **Edge case**: if all spec items exist EXCEPT `interview.md`, mark `[S] interview (spec completed without interview)` — don't force retroactive interviews on old stories
2. Reading the story's test specs (`tests/01_API_Tests.md`, `tests/06_Integration_Tests.md` if exists, `tests/02_UI_Tests.md`, `tests/05_Security_Tests.md` if exists, `tests/03_Load_Tests.md` if exists, `tests/04_Infrastructure_Tests.md` if exists) and the extended cases (`tests/extended/*_Extended.md` if the folder exists)
3. Scanning existing test classes and production code for completed steps
4. Marking completed steps as `[x]`, next step as `[~]`, rest as `[ ]`
5. **Map each backend-style scenario to a step sequence by its `**Level:**` tag** (authored in the test spec — `test-spec-format.md` → "Per-scenario Level tag + overlap pass"). Do NOT default every `### Scenario` in `01_API_Tests.md` to the acceptance sequence:
   - **`L1 acceptance`** → the Backend Scenario Sequence above (`red-acceptance` → `design` → `red/green-usecase` → `adapters-discovery` → adapter steps → `green-acceptance`).
   - **`L2 web-slice`** (validation / error / business-exception→HTTP-status category — duplicate-key 4xx, invalid-field 422, CSRF 403) → a web-slice sequence: `red-adapter rest` → `design` → `green-adapter rest`, and mark `red/green-usecase`, `red/green-domain`, `green-acceptance` as `[S]` when the domain rule is already covered at a lower level. **Never bootstrap an error/validation/per-status category as `red-acceptance`.**
   - **`L3 usecase` / `L4 domain`** → the usecase/domain cycle only.
   - **`db-adapter`** (e.g. SQL-injection literal-treatment, complex `@Query`) → a `red-adapter db` / `green-adapter db` cycle (`@DataJpaTest`), with the other layers `[S]`.
   - **Untagged scenario (older spec):** infer the level from the pyramid (`tdd-rules.md`) and the overlap check — an error/validation category is L2, not L1. Never assume "API-test scenario = acceptance".
   For an `L1 acceptance` scenario that needs new implementation, **always include `design` after `red-acceptance`** (omit only when the whole scenario is `[S]`). Include `[ ] adapters-discovery` after `green-usecase`; include `[ ] red-domain` / `[ ] green-domain` after `green-usecase` as `[S]` by default — activated only when coverage-agent or design-preview identifies need.
6. For frontend scenarios, include `demo` as the final step per scenario
6a. **Full-stack journey step.** After the frontend scenarios block, include one story-level `fullstack-journey` step driven by `tests/07_FullStack_Journey.md`: if the verdict is `extend`/`new` add `[ ] fullstack-journey`; if `no-impact` add `[S] fullstack-journey (no-impact: <reason>)`. If `07_FullStack_Journey.md` is missing (older story), add `[ ] fullstack-journey (assess: produce 07_FullStack_Journey.md verdict)` so the assessment isn't skipped. See "Full-Stack Journey Step".
7. **Surface extended cases as `[S]`, grouped by phase.** If `tests/extended/*_Extended.md` exists, add an `### Extended (deferred — decide at the phase's Extended Gate)` block at the end of each matching phase's scenarios section, listing that phase's extended cases as `[S] {case name} (deferred — review at the <phase> Extended Gate)`. These are **never executed by `/continue`** (they stay `[S]`); the block exists only so the cases are visible in `progress.md` instead of silently omitted, and so the **Per-Phase Extended Gate** (or the **Story Completion Gate** as backstop) can review them. Do not add TDD sub-steps for them — promotion happens at a gate, under user decision.

## Atomic Work Units

A work unit is indivisible: ALL sub-skills in the dispatch sequence (primary skill → test-review → test-coverage → refactor → commit) must execute to completion before stopping. Within a work unit, never pause between sub-skills to report status or ask for confirmation. But after the commit that concludes the work unit, STOP — do not continue to the next work unit. The only valid stop points are: (1) after the commit, (2) on sub-skill failure. If a sub-skill fails, stop immediately and report — but a successful sub-skill must be followed by the next sub-skill in the sequence without interruption.

## Resuming Across Conversations

`progress.md` is the single source of truth for **state** — a new conversation reads it to know which work unit runs next. It does not capture the *why*: predictions that did not match, decisions made in discussion, surprises in existing code, approaches that failed. That context is lost when the user runs `/clear` or `/compact`.

**Journey summaries** preserve the why. They are written by the `/handoff` skill and read by `/continue` on resume — `/handoff` is the sole writer, `/continue` only reads. Run `/handoff` the moment you observe one of these worth-noting moments during work, rather than waiting for the end of the conversation; run it again before `/clear` or `/compact` as a final sweep. Do not spam it: `/handoff` fires only on a genuine trigger — a prediction mismatch, a decision reached in discussion, a surprise, a mistake worth not repeating, a quirk a future scenario will hit (the authoritative list is in `.claude/templates/workflow/summary-format.md` — "When to Write"). Never run it for routine progress that a future session can derive from `progress.md`, the commit, or the code. It is a targeted capture, not a periodic checkpoint. Capturing noteworthy material as it happens is why `/handoff` writes and `/continue` does not — the signal lives in the discussion and debugging, not in the work-unit artifacts. Because `/handoff` may run many times per conversation, it is idempotent: before appending it checks the summary file and skips any entry already recorded.

Summary files are append-only and created lazily: if a conversation had nothing noteworthy, no file is written, and "nothing to record" is a valid, common outcome. When a scenario's last step commits, `/handoff` promotes enduring codebase quirks to `carryover.md` at the story root so later scenarios inherit them.

See the `/handoff` and `/continue` skills for the mechanics (file layout, carryover promotion, reading on resume) and `.claude/templates/workflow/summary-format.md` for when to write an entry and the strict entry format.

---

# Task Workflow

Tasks are standalone work items that don't need the full story lifecycle. Two types:

- **bug** — Something is broken. Fix it with a targeted TDD cycle.
- **refactoring** — Structural improvement. User-defined steps with standard TDD sub-skills.

Tasks live in `ProductSpecification/tasks/{N}-{type}-{slug}/`, where `N` is the **GitHub issue number** for the task (allocated atomically by GitHub — see `/task`). Using the issue number, rather than a locally incremented counter, makes parallel task creation across worktrees/branches collision-free: each branch only sees its own committed folders, so a "max folder + 1" counter races (the `14`/`14` clash that motivated #141). Each task has a progress file at that path. When all checkboxes in a task's `progress.md` are `[x]` (or `[S]`), the task folder is moved to `ProductSpecification/tasks/done/`. Legacy tasks 1–14 predate this scheme and keep their sequential numbers; issue numbers (140s+) never clash with them.

Tasks follow the same TDD discipline as stories: `/test-review` after red phases, `/refactor` after every phase (except `green-acceptance`, `green-playwright`, `demo`). Task commits use `task:` prefix. Tasks don't need bootstrapping -- `/task` generates everything at creation time.

## Bug Tasks → GitHub Issues

**Every task is backed by a GitHub issue** — the issue number is the task number (see `/task` and the task-folder note above). `/task` opens it at creation (or reuses an existing issue) and records it in the task `spec.md` (`Issue: #N`), giving every task a stable, linkable identifier shared across the codebase, the commit history, and the test report. **Bug** tasks additionally require their tests to be tagged with the issue number (below); **refactoring** tasks record the issue for numbering/traceability only and do not tag tests.

**Every test written in a bug task's TDD cycle — backend AND frontend — MUST be tagged with the bug's issue number.** This is mandatory and applies to every red phase of the task (red-acceptance, red-usecase, red-domain, red-adapter, red-playwright, red-frontend, red-frontend-api). The tag links each test back to the tracked bug in the report, so a regression is traceable to its origin. Do NOT reuse a story's UI/API scenario numbering for a bug test — a bug test is identified by its issue number, not a story-scenario slot. The concrete tagging mechanism is technology-specific — see the tech binding's `tdd.md` (backend: `.claude/tech/{backend}/tdd.md`; frontend/E2E: `.claude/tech/{frontend}/tdd.md` and `.claude/tech/{browser-testing}/tdd.md`).

**Scoped steps:** Progress should only include TDD steps for layers the fix actually touches — applies to tasks, and to individual story scenarios. If the fix is pure CSS, don't generate logic/API/align-design steps. If the fix is backend-only, don't generate frontend steps. Affected layers are determined from the spec at creation time.

**Full-stack journey verdict (bug tasks).** A `bug` task whose fix changes a critical user-lifecycle path (a step the top-tier full-stack journey exercises, or should) MUST record a full-stack-journey verdict — `extend` / `new` / `no-impact` — in its `spec.md`, and add a `fullstack-journey` step when the verdict is `extend`/`new` (`[S]` with reason when `no-impact`). See `tdd-rules.md` → "Top-Tier Full-Stack Journey Assessment". Pure refactoring tasks and fixes that don't touch the rendered critical path record `no-impact` (or omit the verdict entirely when there is plainly no UI/lifecycle surface).

Operational details: `/task` skill (creation, sections, progress format), `/continue` skill (execution, dispatch, adapter discovery).

## Improvements vs Bugs

Not everything found during QA is a bug. Classify before acting:

- **Bug** — behaviour is broken or regressed: it was specified **and** built, and now fails. → a separate `bug` task with a backing GitHub issue (see "Bug Tasks → GitHub Issues").
- **Improvement** — an enhancement, or behaviour that was under-specified or missing **by design** (never built, never a scenario). → do **not** open a bug task. Collect it in a per-story **improvements backlog**: `ProductSpecification/stories/NN-story-slug/improvements.md` — one running list per story.

Diagnostic: *"Did it ever work / was it specified-and-built?"* Yes → bug task + issue. No (never built / under-spec) → improvements backlog.

Improvements backlog conventions:
- Items accumulate as `Open` (ids `I1`, `I2`, …), each capturing observed behaviour, spec context, current code state, and scope options. Move an item to `Done` with the resolving task/PR.
- **Architecture is deferred:** finish the base story in its current form first, then revisit the backlog and design solutions (an ADR, or promote the list into a dedicated improvement story).
- A real bug discovered while filling the backlog still goes to a separate bug task + issue — never folded into the improvements list.
