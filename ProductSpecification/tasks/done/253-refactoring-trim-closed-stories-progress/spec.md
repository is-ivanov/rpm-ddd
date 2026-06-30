# Task 253: Cleanup closed stories (1-3) progress.md verbosity

Type: refactoring
Issue: #253

## Problem

Closed stories 1, 2, 3 have bloated `progress.md` files (story 1 alone = 429 lines / ~101 KB). The verbosity mixes three things:

1. **STATE** (next work unit) — progress.md's actual job.
2. **WHY** (decisions, quirks, surprises) — belongs in `summaries/` + `carryover.md` per `workflow.md` "Resuming Across Conversations", NOT in progress.md.
3. **NOISE** (lint/IDE/refactor statuses, file/testid enumerations, PREDICT tables) — pure duplicates of git/code/commit-msg.

Every `/continue` then loads ~100 KB of mostly-stale context just to find one `[ ]`; the unique "why" (promotion notes, spec-vs-reality gaps, cross-scenario dependencies) is buried in noise and mostly NOT promoted to summaries (story 1 has 1 summary file, tiny carryover).

## Solution

Refactoring (documentation cleanup), same pattern per story:

- `/handoff` over completed scenarios → extract unique "why" into `summaries/{scenario}.md`, promote enduring quirks to `carryover.md`.
- Trim each `progress.md` entry to ONE line: status + test-class/ADR ref + `see summaries/X`. Delete the noise.
- Keep inline only: state + cross-scenario dependencies / promotion notes (they drive task ordering).

## Steps

1. Story 1 (user-login): handoff → summaries/carryover → trim progress.md.
2. Story 2 (email-on-registration): same.
3. Story 3 (home-page): same.

## Key Files

- `ProductSpecification/stories/01-user-login/{progress.md, summaries/, carryover.md}`
- `ProductSpecification/stories/02-*/{progress.md, summaries/, carryover.md}`
- `ProductSpecification/stories/03-*/{progress.md, summaries/, carryover.md}`

## Notes

- Apply the terse format once Task #254 (the rule) lands; until then mirror the existing story-4 / carryover idiom.
- `/handoff` is the sole writer of `summaries/` + `carryover.md`; this task drives that extraction.
