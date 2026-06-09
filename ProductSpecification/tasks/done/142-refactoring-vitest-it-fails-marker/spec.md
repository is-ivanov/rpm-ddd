# Task 142: RED-phase marker → Vitest it.fails() (frontend)

Type: refactoring
Issue: #142  <- enhancement issue (not a bug); tracks this framework/docs change. Frontend counterpart of #141.

## Problem

Our frontend TDD workflow commits a known-failing (RED) test behind `.skip`. `.skip`
has the same two weaknesses we fixed on the backend in #141:

1. **RED state is not machine-verified** — once skipped, CI never re-confirms the
   test still fails for the predicted reason; it is dead weight until GREEN.
2. **The GREEN transition is not enforced** — GREEN means manually removing `.skip`;
   forget, and nothing notices.

## Solution

Adopt Vitest's [`test.fails` / `it.fails`](https://vitest.dev/api/#test-fails) as the
frontend RED-phase marker — the direct analog of junit-pioneer's `@ExpectedToFail`:

| Event | `it.fails` behavior |
|---|---|
| Test runs and fails | passes (RED expected) — build stays green |
| Test runs and passes | build **FAILS** — forces marker removal at GREEN |

Since **Vitest 4.1**, `fails`-marked tests are tracked in the test summary; our FE is
on `vitest@^4.1.8`, so this is available.

### Difference from the backend annotation

- **No `withExceptions` equivalent.** `it.fails(name, fn)` only takes a name + function —
  you cannot pin a specific error type the way junit-pioneer's `withExceptions` does.
  To keep the RED reason precise, **pin it via the assertion inside the test** (a specific
  `expect(...)` that fails for the predicted reason), so an unrelated failure isn't
  silently absorbed. This guidance must be documented as the compensating control.
- Applies to the frontend logic/API unit tests (Vitest). **Playwright E2E** uses its own
  skip mechanism — out of scope here.

**No test migration needed:** there are no live `.skip` RED markers in frontend tests
today (prior RED tests are already green). This is a docs / templates / agents update —
no production or test code change beyond the throwaway demonstrative test.

### Decision: demonstrative test is throwaway

Mirroring #141: a demonstrative RED test using `it.fails` is written to prove the
mechanism (runs-and-fails → green; runs-and-passes → build fails), then **removed**.
We do not commit a permanently-marked example — the proof is the verification.

## Key Files

- `ProductSpecification/technology.md` — Conventions table (Frontend): `Test skip marker`
  `.skip` → `.fails` (RED-state)
- `.claude/tech/vue-ts/tdd.md` — "Test Skip Marker" section: marker + RED/GREEN mechanics
  + the "pin the reason via the assertion" compensating-control note
- `.claude/tech/vue-ts/templates/logic-test.md` — ".skip Convention" section → `.fails`
  syntax + example
- `.claude/rules/frontend-rules.md` — test-skip-marker wording (universal, semantics now
  "test runs every build")
- `.claude/agents/red-agent.md`, `.claude/agents/green-agent.md` — frontend marker wording
- `.claude/templates/workflow/red-phase-formats.md` — universal red-phase formats (frontend
  marker reference)

## Acceptance

- A demonstrative RED test using `it.fails` runs-and-fails (build green) and FAILS the
  build once the code works (forcing marker removal). Verified, then removed.
- All framework docs/templates/agents reference `.fails` with the "pin the reason via the
  assertion" compensating control documented.
- `npm run test` (frontend) is green.

## Related

- Backend counterpart: #141 (junit-pioneer `@ExpectedToFail`). Landed first.
