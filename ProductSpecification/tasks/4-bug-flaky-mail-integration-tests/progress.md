# Task 4: Full-suite mail integration tests are flaky — Progress

Type: bug

## Spec
- [x] spec

## Fix (test-infrastructure only — no production code expected)

### Step 1: Reproduce & confirm root cause
- [ ] Reproduce deterministically (full `./mvnw test`; note ordering). Confirm the shared-spy stub leak + async-bleed hypothesis from `spec.md` via logs (which test arms `doThrow`, which test's send consumes the first throw).

### Step 2: Fix the isolation
- [ ] Apply the chosen fix (await async quiescence between mail tests and/or deterministically re-baseline the shared `JavaMailSender` spy, honoring the modulith-incomplete-publication-race reset constraint). No loosened assertions; no second full context.
- [ ] `/test-review` → `/refactor`

### Step 3: Verify deterministic green
- [ ] Run the full `./mvnw test` suite ≥3 times — green every time (the 3 mail tests pass within the suite).

## Notes
- Discovered 2026-06-02 during Story 2 §8.1 (scheduler wiring). Confirmed pre-existing at `f2dbec4` (same 3 failures without the scheduling changes), so it does not block the §8.1 scheduling commit — but the full suite stays red until this is fixed.
