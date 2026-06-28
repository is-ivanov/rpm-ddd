# Task 221: prompt-refactor doc layer fixes

Type: refactoring
Issue: #221  <- the task number IS the issue number; refactoring records it for traceability but does not tag tests

## Problem

A full `/prompt-refactor` scan of all 8 agents + 30 skills surfaced four objective documentation-layer violations:

1. **B6 tech leakage** — `agents/green-agent.md` (L62) and `agents/test-runner.md` (L24) hardcode `.claude/tech/java-spring/infrastructure.md` instead of the `{backend}` concern placeholder used everywhere else in those files.
2. **A5 template mismatch** — `skills/refactor/SKILL.md` omits 4 refactoring templates that exist on disk: `replace-jsonnode-with-dto`, `recursive-comparison`, `test-data-builder` (backend) and `extract-tailwind-class` (frontend).
3. **A4/A5 broken reference** — `agents/design-review-agent.md` (L54, L67) and `skills/design-review/SKILL.md` (L32) all reference `design-review-patterns.md`, which does not exist, leaving the design-review agent with no output-format spec at all.

## Solution

- Replace the hardcoded `java-spring` paths with the `{backend}` placeholder in green-agent and test-runner.
- Add the 4 missing templates to the refactor skill's Available Templates lists.
- Create `.claude/tech/playwright/templates/design-review-patterns.md` with the BAD→GOOD examples, review output format, and verdict examples the agent and skill delegate to it.

Documentation-only; no production code or tests affected.

## Key Files

- `.claude/agents/green-agent.md`
- `.claude/agents/test-runner.md`
- `.claude/skills/refactor/SKILL.md`
- `.claude/agents/design-review-agent.md` (references the new template)
- `.claude/skills/design-review/SKILL.md` (references the new template)
- `.claude/tech/playwright/templates/design-review-patterns.md` (new)
