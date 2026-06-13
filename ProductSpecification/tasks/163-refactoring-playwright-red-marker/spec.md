# Task 163: Align Playwright RED-marker docs on test.fail

Type: refactoring
Issue: #163

## Problem

Three documents disagree about the Playwright RED marker, and two of them are wrong:

- `.claude/tech/playwright/tdd.md` still prescribes `test.skip()` — a skipped test does not run, violating the "RED test runs every build" rule.
- The Conventions table in `ProductSpecification/technology.md` (browser-testing concern) prescribes `test.fails` — that is the **Vitest** API; in Playwright it does not exist.
- The correct Playwright API is **`test.fail(...)`** (expected-failure marker: the test runs, passes while it fails, and fails the build once it starts passing).

This drift already burned a red-playwright run in Task 8: the agent used `test.fails` as documented and the run died with `TypeError: test.fails is not a function`.

## Solution

Align all three places on `test.fail(...)`:

1. `.claude/tech/playwright/tdd.md` — replace the `test.skip()` prescription with `test.fail(...)`; keep the rule that the RED reason is pinned by a comment above the marker **and** by a specific assertion inside the test.
2. `ProductSpecification/technology.md` — fix the Conventions table row for the browser-testing test-disable marker (`test.fails` → `test.fail`). Keep the Vitest row (`it.fails`) as is — it is correct for the frontend unit concern.
3. The universal RED-phase template/agent docs (`.claude/templates/**`, `.claude/agents/red-agent.md`) — wherever the marker is referenced generically, ensure it defers to the Conventions table and does not hardcode a wrong name.

Verification: grep the repo docs for `test.fails` and `test.skip` (in the RED-marker context) — zero remaining references for the Playwright concern.

## Key Files

- `.claude/tech/playwright/tdd.md`
- `ProductSpecification/technology.md` (Conventions table)
- `.claude/templates/` RED templates, `.claude/agents/red-agent.md`
