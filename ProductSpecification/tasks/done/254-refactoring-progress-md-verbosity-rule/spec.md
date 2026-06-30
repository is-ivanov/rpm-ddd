# Task 254: Progress.md verbosity rule

Type: refactoring
Issue: #254

## Problem

No project rule governs the verbosity/detail-level of `progress.md` entries. `templates/workflow/progress-format.md` shows bare checkboxes (`- [x] red-acceptance`); `skills/continue/SKILL.md` step 9 says only "mark completed, advance next"; `rules/workflow.md` "Updating Progress" says "change `[~]` to `[x]`". None specify detail level, nor that "why" belongs in summaries/carryover rather than progress.

Result: the dispatcher LLM copies the full phase-agent report (files, PREDICT tables, lint statuses) into the checkbox parenthetical → entries balloon (story 1 hit 101 KB), because nothing forbids it. This is a **gap in the prompt rules**, not a code bug.

## Solution

Use `/prompt-update` + `/prompt-refactor` to add an explicit norm:

> "progress.md entries are TERSE — one line: status + test-class/ADR ref + summary link. The 'why' (decisions, quirks, surprises) goes to `summaries/` via `/handoff` and `carryover.md`. Lint/refactor/PREDICT noise is NOT recorded (it lives in git/commit-msg)."

Target layers (per `prompt-update-classification.md`): likely `rules/workflow.md` "Updating Progress" + `templates/workflow/progress-format.md` (show a terse `[x]` example with `see summaries/X` link). Don't create a new rules file.

## Steps

1. `/prompt-update` — classify the norm and write it to the correct layer(s).
2. `/prompt-refactor` on the touched files — verify structural cleanliness + no layer violations.

## Key Files

- `.claude/rules/workflow.md` ("Updating Progress")
- `.claude/templates/workflow/progress-format.md`
- `.claude/skills/continue/SKILL.md` (step 9, reference only — update if needed)

## Notes

- This is the rule task B/C depend on; execute it before the cleanup tasks (#253, #255).
- When the rule lands: `progress.md` entries = one line; `/handoff` owns the "why".
