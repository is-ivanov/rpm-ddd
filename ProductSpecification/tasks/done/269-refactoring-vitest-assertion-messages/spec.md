# Task 269: Vitest assertion messages over comments

Type: refactoring
Issue: #269  <- refactoring records the issue for traceability; migrated tests are NOT tagged

## Problem

RED-phase rationale comments get stranded on now-green FE tests (`// RED — stub returns X…`,
`// Pinned to […] so an OR impl differs`). They surface only when someone reads the file — never at
failure time, when the rationale is most useful — and they silently rot as the code evolves. This was
found during Story 4 Scn 3.4.

Vitest supports `expect(actual, message?)` where `message` is shown as the failure message in the
console — the frontend analog of AssertJ's `as()` (a recorded backend preference: "AssertJ as() over
comments + chain"). The project **already** uses this in Playwright Statements
(`.claude/tech/playwright/tdd.md`: `expect(locator, 'task form is displayed').toBeVisible()`), but it
is not applied to Vitest logic / API-client tests.

## Solution

Move the **per-assertion** "why this expected value / what a wrong impl would produce" rationale from
comments into the `expect(actual, message)` failure description, so it shows in the console on failure
instead of rotting in a comment. Apply a hybrid rule so we don't over-migrate:

- **Per-assertion rationale** (why THIS expected value, what a wrong impl produces, pinned
  discrimination like AND-vs-OR) → into `expect(actual, message)`.
- **Block / setup / dataset-level context** and contract lock-step notes → stay comments (the message
  arg has no single-assertion anchor).
- **Delete** a comment only when nothing but a restatement of the test title survives.

Then codify the convention in `.claude` docs so green-agent and the tech bindings prescribe it going
forward.

## Key Files

- `frontend/src/features/users/__tests__/users-grid.logic.test.ts` — pilot (Scn 3.4 multi-column block)
- `frontend/src/**/__tests__/*.test.ts` — remaining FE Vitest logic / API-client tests (sweep)
- `.claude/agents/green-agent.md` — "RED-Comment Reframing" guidance → prefer the message arg
- `.claude/tech/vue-ts/tdd.md` — add the message-arg convention
- `.claude/tech/playwright/tdd.md` — existing precedent to reference (no change expected)

## Notes

- Behavior-preserving: the message only shows on failure; `toEqual`/`toBe` outcomes are unchanged, so
  the full suite must stay green throughout.
- Full-stack-journey verdict: **no-impact** (test-comment + docs refactor; no rendered critical path).
- Supersedes a reverted local attempt that codified "reframe the RED comment" in green-agent (dropped
  to avoid doc flip-flop — this task is the agreed replacement direction).
- Discovered during Story 4 Scn 3.4; execute after that scenario is complete.
