# Task 10 — Deferred Upstream Items (NOT ported)

Companion to `spec.md`. Task 10 ported the *generally-useful* parts of upstream
`continue-framework` commit `77d66f1` (2026-05-26). This file documents the two
deferred areas the maintainer flagged for possible later adoption — **load-test
infrastructure** and the **`qa` task type** — with the full upstream text and
what it would take to adopt them here.

Upstream repo: `\\wsl.localhost\Debian\home\ivan\JavaProjects\continue-framework`
Target commit: `77d66f119fdea0cfb21a89ed3a120320caafde7c`
Inspect any file with: `git -C <upstream> show 77d66f1:<path>`

---

## 1. Load-Test Infrastructure (deferred)

### Why this matters now

**Story 1 already has 3 load scenarios** in
`ProductSpecification/stories/01-user-login/tests/03_Load_Tests.md`:

| # | Scenario | Assertion |
|---|----------|-----------|
| 3.1 | Login response time | status 200 AND < 200ms |
| 4.1 | 50 concurrent logins | all 200 AND max < 500ms |
| 5.1 | Activation token validation response time | status 200 AND < 200ms |

In `ProductSpecification/stories.md` the **Load** column for story 1 is `—`
(not started), and story-1 `progress.md` has **no Load Scenarios section yet** —
so these are unimplemented and currently being skipped on purpose. If/when they
are implemented, the upstream load harness below becomes relevant.

### What upstream 77d66f1 added (and we did NOT port)

Three coupled pieces. None exist locally.

**(a) `infrastructure.md` → "Load-Test Infrastructure"** — session-scoped
lifecycle for a baked-baseline Postgres + dedicated load backend:

```
The baked baseline Postgres container and the dedicated load backend are
ephemeral and resource-heavy — the baseline container holds hundreds of
thousands of rows and its autovacuum churns CPU even at idle. Unlike the dev
backend and dev infra (meant to stay up across a session), load infra exists
only for the duration of a load-testing session.

- Cleanup is session-scoped, not run-scoped. Iterative re-runs (TDD
  green-acceptance cycles) reuse the same warm load backend + baseline
  container — do NOT tear them down between runs.
- When the load-testing session is finished, stop both: the load backend
  (kill only the specific PID you started) and the baseline container
  (stop by name, matching your repo index).
```

**(b) `tdd-rules.md` → "Load Test Isolation"** — per-test data isolation rules:

```
- Each mutating load test owns a dedicated baked disposable user. Declare the
  user as an immutable class-level constant on the Statements (one user per
  Statements class). Read-only load tests use the shared baseline users.
- Per-user data IDs are inlined as immutable constants computed from the
  baseline generator's deterministic formulas (UUIDs, external resource IDs,
  child entity IDs, business keys). Keep the generator formulas as the single
  source of truth; never derive IDs at runtime in test code.
- Witness user for cross-user assertions. When a mutating load test must assert
  "another user is unaffected", use a separate, never-mutated baked user.
- No global state-reset hooks for test isolation. Sequential test execution +
  per-test dedicated users provide isolation at zero infra cost.
  See ProductSpecification/decisions/load-test-isolation-decision.md.
```

**(c) `test-acceptance/SKILL.md` → Load pre-checks** — a two-step guard before
running the load suite (health check + baked-baseline DB probe), an `## arguments`
`load` / `load {ClassName}`, and a "Post-Run Cleanup (load only)" section. The
key insight it encodes:

```
The health endpoint alone is not enough: a dev backend started by run-backend
also returns 200 from /actuator/health while having an empty DB, and the load
suite will then fail every test with confusing HttpClientErrorException 401s at
the login step. Catch this upfront with a baked-baseline DB probe
(docker ps --filter name=postgres-container-$REPO_INDEX --format {{.Image}}).
```

### Why deferred

- We do not yet run the upstream load-baseline harness (no
  `docker-compose.load.yml`, no `infrastructure/load-baseline/` image, no
  baked-baseline generator).
- Story 1's load scenarios are intentionally skipped for now.
- Adopting the rules without the harness would add dangling references
  (e.g. `decisions/load-test-isolation-decision.md`) and unrunnable pre-checks.

### What adoption would require (checklist for a future task)

1. Decide the load tooling (virtual threads / Gatling / JMeter — `03_Load_Tests.md`
   DSL leaves it open).
2. Create the baked baseline: `infrastructure/load-baseline/` image + generator
   with deterministic ID formulas, `docker-compose.load.yml`, and a README
   ("Wiring load tests to the baked image").
3. Write `ProductSpecification/decisions/load-test-isolation-decision.md` (the
   ADR the isolation rule references).
4. Port pieces (a)+(b)+(c), adapting `green-selenium`→`green-playwright`,
   `h2`→`db`, and our health path (`/api/...` — confirm vs `/actuator/health`).
5. Add the Load Scenario Sequence steps to story-1 `progress.md` for 3.1/4.1/5.1.

Until then: keep the **Load** column `—` and skip the scenarios.

---

## 2. `qa` Task Type + Bug Discovery-First (deferred)

We kept the local task taxonomy at **two types — `bug`, `refactoring`** (see
`.claude/rules/workflow.md` "Task Workflow"). Upstream `77d66f1` introduced a
**third type, `qa`**, and reworked the **bug** flow to be discovery-first. Both
were NOT ported.

### What the `qa` task type is for

A `qa` task is a **reusable manual checklist** (smoke / regression) verified by a
human against an external environment (prod-copy, staging) — **no code change,
no TDD cycle**. It is the framework's way to track "click through these N things
after each deploy" as first-class, resumable state.

How it differs from bug/refactoring (upstream `workflow.md` → "QA Task Sequence"):

```
- No TDD, no dispatch. progress.md checkboxes are not work units — each is a
  manual verification step performed by a human in a browser. /continue does NOT
  auto-dispatch QA cases; on a QA task it reports the next unchecked case and
  reminds the tester to run it by hand.
- Session lifecycle. spec.md is the immutable checklist definition (Cases
  section). progress.md mirrors those cases as [ ] checkboxes for the active test
  session. To re-run for a new deploy, revive the task from done/ and reset
  checkboxes — never edit spec.md to track sessions.
- Failures file separate bug tasks. When a case fails during a session, the
  checkbox stays [ ] and the tester creates a separate /task bug (prod-copy
  variant if reproduced there). Never overload the checkbox with a fail marker —
  [x] means verified, [ ] means not yet verified or under investigation.
```

Supporting changes upstream made for `qa` (also not ported):
- `task/SKILL.md`: accepts `qa` type; spec sections become **Problem / Solution
  / Cases** (numbered one-line intents, no Gherkin); no Affected Layers / Key
  Files (QA changes no code).
- `creation-formats.md`: a `## Cases` progress format with one `[ ]` per case.
- `continue/SKILL.md` dispatch: `QA ## Cases checkbox → No dispatch. Report the
  next unchecked case and stop — the tester verifies manually, then ticks it (or
  files a bug task).`

**When you'd want it:** post-deploy smoke checklists, release regression passes,
"verify prod-copy after infra change" — anything that is a recurring *human*
verification ritual rather than an automated test. If you never track manual
QA sessions in the framework, you don't need this type.

### Bug discovery-first (coupled change, also deferred)

Upstream also changed **bug** tasks to NOT pre-plan TDD steps; instead they start
with discovery (`reproduce in prod-copy` → `root cause analysis` →
`steps discovery` gate, analogous to `adapters-discovery`). We kept the local
bug flow (pre-planned TDD steps from `creation-formats.md`). This is bundled here
because it lives in the same upstream `workflow.md` "Task Workflow" rewrite as the
`qa` type — adopt them together if at all.

### Why deferred

- Flow not yet understood / no current need for tracked manual QA sessions.
- Adopting `qa` touches 4 files (`workflow.md`, `task/SKILL.md`,
  `creation-formats.md`, `continue/SKILL.md`) and changes the bug flow — a
  taxonomy change better done deliberately than bundled into a port.

### What adoption would require (checklist for a future task)

1. Port the `workflow.md` "Task Workflow" rewrite (3 types + Bug Discovery-First
   + QA Task Sequence), adapting `green-selenium`→`green-playwright`.
2. Update `task/SKILL.md` (qa type, Cases section) and `creation-formats.md`
   (`## Cases` progress format).
3. Update `continue/SKILL.md` dispatch table (QA = no-dispatch, report-and-stop).
4. Decide whether to also adopt bug discovery-first (it is coupled in the
   upstream diff).

---

## Other un-ported items (for completeness)

These were also excluded per `spec.md`; not the focus here but recorded so the
picture is complete:

- **`workflow.md` "Source Control / no PR-MR" block** — contradicts our PR +
  GitHub MCP + story-branch workflow. Intentionally never adopting.
- **`infrastructure.md` "Infrastructure as Code"** section — not in Task 10 scope.
- **`hooks/notify.sh` + `settings.json enableAllProjectMcpServers`** — we use our
  own Windows `notify-*.ps1` hooks.

See `spec.md` → "Out of scope" for the authoritative list.
