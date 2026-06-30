# Task 255: Cleanup story 4 progress.md verbosity

Type: refactoring
Issue: #255

## Problem

Story 4 (user-management, in progress) `progress.md` is trending toward the same bloat as the closed stories: recent `[x]` entries (esp. frontend Scn 3.x) carry full phase-agent reports inline — file lists, data-testid enumerations, PREDICT tables, lint/IDE statuses, refactor notes — instead of terse state. The story is NOT closed yet, so this is the right moment to set the terse norm before more scenarios accumulate.

Depends on Task #254 (the rule) for the target format.

## Solution

Refactoring (documentation cleanup), scoped to the open story 4, same pattern as the closed-stories cleanup (#253):

- `/handoff` over completed scenarios → extract unique "why" (page.clock determinism decision, statusRank / lifecycle-order "FE owns order" decision, read-model LazyInitialization quirk, etc.) into `summaries/` + promote enduring quirks to `carryover.md`.
- Trim each `[x]` entry to ONE line: status + test-class/ADR ref + `see summaries/X`. Delete noise (lint statuses, file enumerations, PREDICT tables, refactor notes).
- Keep inline: promotion notes, cross-scenario dependencies, ordering.

## Steps

1. `/handoff` over completed story-4 scenarios (backend 1.1/2.1/3.1/E1 + frontend 1.1/1.2/2.1/2.2/3.1/3.2/3.3) → summaries/carryover.
2. Trim story 4 `progress.md` to terse one-line entries.

## Key Files

- `ProductSpecification/stories/04-user-management/{progress.md, summaries/, carryover.md}`

## Notes

- Execute after Task #254 (the rule lands) so the terse format is defined.
- Story 4 is in progress (Frontend 🔧, Scn 3.3 / 10/27); trim only completed `[x]` entries, leave `[~]`/`[ ]` intact.
