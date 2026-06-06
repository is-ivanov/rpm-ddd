# Task 10: Port Upstream Framework Improvements (77d66f1) -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

> Docs/prompt-only task — no TDD cycle. Each step ports a coherent group from
> upstream `77d66f1`, adapting terminology (usecase→application, h2→db,
> Selenium→Playwright) and is closed by a `/prompt-refactor` validation pass on
> the files it touched. After all steps, verify every changed file ≤200 lines.

### Step 1: tdd-rules hardening
- [x] Port "Flaky Test Fix Protocol" into `tdd-rules.md`
- [x] Rename "Zero Tolerance…" + add skip count / Hidden Non-Execution / stop-on-first-failure disclosure
- [x] Tighten Assertion Rules: storage **ports** + "No reclassification" clause
- [x] /prompt-refactor `.claude/rules/tdd-rules.md`

### Step 2: coding-rules — JsonNode→DTO + 200-line + app-service no-call
- [x] Add "Typed deserialization at the boundary" to Code Style
- [x] Update 200-line rule to "every source file regardless of type"
- [x] Add "application service must not call another application service" (FORBIDDEN list + Usecases section)
- [x] Create `.claude/templates/refactoring/replace-jsonnode-with-dto.md` (adapt to Jackson/Spring)
- [x] Reference new template from `.claude/templates/refactoring/scan-checklist.md` (A0 note, A56 row, 3 routing rows)
- [x] /prompt-refactor `.claude/rules/coding-rules.md` + new template

### Step 3: Agent tightening
- [x] `red-agent.md`: mandatory full Output Summary + no storage Fakes in Statements
- [x] `refactor-agent.md`: `wc -l` on every changed file regardless of type
- [x] `test-review-agent.md`: scan all assertion calls + Fakes' verify/assert methods
- [x] /prompt-refactor the three agent files

### Step 4: design-preview options
- [x] Rewrite `design-preview/SKILL.md`: 2-3 options + AskUserQuestion choice + separate ADR decision (adapt to our adr-format/`decisions/`); fixed ExpectedLoad.txt→.md ref
- [x] /prompt-refactor `.claude/skills/design-preview/SKILL.md`

### Step 5: /handoff skill + journey summaries
- [~] Create `.claude/templates/workflow/summary-format.md` (adapt file layout to our paths)
- [ ] Create `.claude/skills/handoff/SKILL.md`
- [ ] Update `workflow.md` "Resuming Across Conversations" (journey summaries + carryover)
- [ ] Update `continue/SKILL.md` to READ carryover.md + scenario summary on resume
- [ ] /prompt-refactor workflow.md + handoff + continue

### Step 6: Final validation
- [ ] /prompt-refactor sweep across all changed files (layer violations, cross-refs)
- [ ] Verify every changed/new file ≤200 lines (`wc -l`)
- [ ] Verify `[[links]]` / file-path references resolve
