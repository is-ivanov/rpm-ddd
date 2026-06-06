# Task 10: Port Upstream Framework Improvements (77d66f1)

Type: refactoring

## Problem

The project's Continue framework under `.claude/` was forked from the upstream
`continue-framework` **before** commit `77d66f1` (published 2026-05-26,
"update rules, skills, and templates; add handoff skill and notify hook").
None of that commit's improvements are present locally (verified: no markers
for `Flaky Test Fix`, `Hidden Non-Execution`, `Typed deserialization`,
`/handoff`, etc.). Since the fork, the local copy was also heavily restructured
for this project's stack.

We want to bring the **generally-useful** improvements from `77d66f1` into the
local framework, adapted to our structure — without importing the upstream
choices that conflict with our process.

Upstream repo: `\\wsl.localhost\Debian\home\ivan\JavaProjects\continue-framework`
Target commit: `77d66f119fdea0cfb21a89ed3a120320caafde7c`

## Solution

Port the selected items below, **adapting upstream terminology to ours** on
every edit:

| Upstream term | Our term |
|---|---|
| `usecase` layer / "usecases" | `application` layer / "application services" |
| `adapters` layer | `infrastructure` layer |
| `h2` templates / `test-review-h2` | `db` templates / `test-review-db` |
| Selenium / `green-selenium` | Playwright / `green-playwright` |
| "no PR/MR" source control | KEEP our PR + GitHub MCP + story/task-branch flow |

**In scope (confirmed):**

1. **tdd-rules hardening** (`.claude/rules/tdd-rules.md`)
   - New "Flaky Test Fix Protocol" (reproduce 5×→fix→verify 5×).
   - Rename "Zero Tolerance for Test Failures" → "…, Skips, and Hidden
     Non-Execution"; require **skip count** in every summary; never hide
     skipped/disabled/filtered tests; stop-on-first-failure must disclose
     "N passed, M did not run".
   - Tighten Assertion Rules: forbid injecting **storage ports** (not just
     "storage adapters") into Statements; add the "No reclassification" clause.

2. **JsonNode→DTO** (`.claude/rules/coding-rules.md` + new template)
   - Add "Typed deserialization at the boundary" rule to Code Style.
   - New refactoring template `.claude/templates/refactoring/replace-jsonnode-with-dto.md`.
   - Reference it from `.claude/templates/refactoring/scan-checklist.md`.

3. **200-line limit on any file** (`.claude/rules/coding-rules.md` +
   `.claude/agents/refactor-agent.md`)
   - File-size rule applies to **every source file regardless of type**
     (stylesheets, config), not only classes; refactor-agent runs `wc -l` on
     every changed file regardless of type.

4. **app-service no-call rule** (`.claude/rules/coding-rules.md`) — adapted
   - Add (Clean Architecture FORBIDDEN list + Usecases section): an application
     service MUST NOT inject or call another application service; shared logic
     goes to the domain or a non-service helper.

5. **Agent tightening**
   - `red-agent.md`: mandatory full Output Summary (Predicted/Actual/Comparison,
     no abbreviation); never inject storage Fakes into Statements.
   - `refactor-agent.md`: `wc -l` on every changed file regardless of type;
     split oversize files touched by the refactoring now, not later.
   - `test-review-agent.md`: scan ALL assertion calls and include Fakes'
     `verify*`/`assert*` methods in scope.

6. **design-preview options** (`.claude/skills/design-preview/SKILL.md`)
   - Generate 2-3 viable options (Summary/Pros/Cons, one Recommended), present
     them, choose via `AskUserQuestion`, then a separate ADR decision; reject-all
     escalates to `/architecture`. Adapt paths to our `adr-format.md` and
     `decisions/` convention.

7. **/handoff skill + journey summaries**
   - New `.claude/skills/handoff/SKILL.md` (sole writer of summaries/carryover).
   - New `.claude/templates/workflow/summary-format.md`.
   - `.claude/rules/workflow.md` "Resuming Across Conversations": describe
     journey summaries + carryover (adapt file layout to our story/task paths).
   - `.claude/skills/continue/SKILL.md`: read `carryover.md` + scenario summary
     on resume (read-only; `/handoff` is the writer).

**Out of scope (intentionally NOT ported):**

- "Source Control / no PR-MR" block in `workflow.md` — contradicts our PR +
  GitHub MCP + story-branch workflow.
- Load-Test Infrastructure / Load Test Isolation rules and load pre-checks in
  `test-acceptance` — we don't run the upstream load-baseline harness.
- `qa` task type and bug "discovery-first" sequence — not adopting the new task
  taxonomy in this pass.
- `notify.sh` / `enableAllProjectMcpServers` — we already have our own Windows
  `notify-*.ps1` hooks.

## Key Files

Rules:
- `.claude/rules/tdd-rules.md`
- `.claude/rules/coding-rules.md`
- `.claude/rules/workflow.md`

Agents:
- `.claude/agents/red-agent.md`
- `.claude/agents/refactor-agent.md`
- `.claude/agents/test-review-agent.md`

Skills:
- `.claude/skills/design-preview/SKILL.md`
- `.claude/skills/handoff/SKILL.md` (new)
- `.claude/skills/continue/SKILL.md`

Templates:
- `.claude/templates/refactoring/replace-jsonnode-with-dto.md` (new)
- `.claude/templates/refactoring/scan-checklist.md`
- `.claude/templates/workflow/summary-format.md` (new)

Reference (upstream, read-only):
- `\\wsl.localhost\Debian\home\ivan\JavaProjects\continue-framework` @ `77d66f1`

## Notes

- This task edits **prompt/documentation files only** — no production code, no
  TDD red/green cycle. Validation is structural: run `/prompt-refactor` on each
  changed file to catch layer violations, and verify every file stays ≤200 lines.
- Compare against upstream with: `git -C <upstream> show 77d66f1 -- <path>`.
