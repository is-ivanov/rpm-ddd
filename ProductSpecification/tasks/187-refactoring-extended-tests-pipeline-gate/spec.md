# Task 187: Extended Test Cases Pipeline Gate

Type: refactoring
Issue: #187

## Dependencies

- **Process fix for the root cause of #189** — #189 (Story 1 extended UI cases) is exactly the work
  that this gate would have surfaced instead of orphaning. #189 can be promoted independently, but
  this gate prevents the same silent drop in future stories. No code dependency between them.
- **Independent** of the other FE-audit decisions (#190–#193); touches only `.claude`/`.opencode`
  process docs.

## Problem

`/test-spec` generates `tests/extended/*_Extended.md` with the header *"Implement after core
tests pass"*, but nothing ever pulls those cases into work:

- **Bootstrapping** (`workflow.md` → Bootstrapping, step 2) enumerates only the 6 main test files
  into `progress.md`; the `extended/` folder is never listed.
- **`/continue`** executes only the checkboxes in `progress.md`, so extended cases are invisible.
- **Story completion** (move to Done in `stories.md`) has no gate to review `extended/` or
  `improvements.md` before closing.

Result: extended cases are generated and then orphaned. Story 1's extended UI cases (password
mismatch, real-time strength indicator, login loading state, banner dismiss) were silently dropped
and only resurfaced via an external FE audit (`ProductSpecification/audits/2026-06-20-frontend-audit.md`).
The instruction *"implement after core"*
has no owner and no trigger in the process.

## Solution

1. **Story-completion gate** in `workflow.md` lifecycle: before moving a story to Done, review
   `tests/extended/*` → promote chosen cases into `progress.md` as scenarios OR log to
   `improvements.md`.
2. **Bootstrapping surfaces `extended/`**: when bootstrapping `progress.md`, add an
   `### Extended (deferred — decide at story completion)` block listing extended cases as `[S]`, so
   they are visible rather than silently omitted.
3. **Optional**: `/handoff` reminds about unreviewed `extended/` when a story's last scenario
   completes. Decide during Step 1.

## Key Files

- `.claude/rules/workflow.md` (story lifecycle + Bootstrapping)
- `.claude/skills/continue/SKILL.md` (bootstrap reads)
- `.claude/skills/test-spec/SKILL.md` (extended header generation)
- `.claude/skills/handoff/SKILL.md` (optional reminder)
- `.opencode/` mirrors of the above (keep both frameworks in sync)
