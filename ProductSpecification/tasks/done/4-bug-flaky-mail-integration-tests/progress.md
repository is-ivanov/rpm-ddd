# Task 4: Full-suite mail integration tests are flaky — Progress

Type: bug

## Spec
- [x] spec

## Fix (test-infrastructure only — no production code expected)

### Step 1: Reproduce & confirm root cause
- [x] Reproduce deterministically. Plain repetition under the **production** config (DB lock READ_WRITE → integration tests serialized) was **green 5/5** (161 tests). Forced repro (throwaway: DB `@ResourceLock` relaxed to `READ` mode so full-context integration tests run **concurrently** again) failed **on demand**: `Tests run: 161, Errors: 3`, all `ConditionTimeoutException` (15s) in the mail tests via `StalePublicationStatements` — the exact `spec.md` symptom. **Root cause confirmed** = the spec's hypothesis: concurrency among full-context integration tests sharing the single `@MockitoSpyBean JavaMailSender`; the `doThrow().doCallRealMethod()` first-throw armed by a stale/in-flight-publication test is consumed by a *different* concurrent test's async send (async bleed) → a delivery-expecting test never gets its email → 15s timeout. The specific 3 victims vary run-to-run (shared-spy race signature).

### Step 2: Fix the isolation
- [S] No code change needed — **already fixed at HEAD by intervening test-isolation work** that postdates this task (filed 2026-06-02):
  - `DbTest` applies `@ResourceLock(DB_LOCK)` in **READ_WRITE** mode → all DB/full-context integration tests **serialize** ("Replaces the blanket `@Execution(SAME_THREAD)`"). The forced repro proves this lock is the load-bearing guard: relax it → bug returns; keep it → green.
  - `EventPublicationCleanupExtension` clears `event_publication` per test (Task 224 / PR #232) → removes the registry order-coupling leak.
  The spec's fix candidates (await async quiescence / re-baseline the spy) are therefore moot.
- [S] `/test-review` → `/refactor` — N/A, no code change.

### Step 3: Verify deterministic green
- [x] Full `./mvnw test` suite run **5×** under production config — green every time (the mail tests pass within the suite). The forced-concurrency run additionally proves the serialization guard is what keeps it green (not luck).

## Notes
- Discovered 2026-06-02 during Story 2 §8.1 (scheduler wiring). Confirmed pre-existing at `f2dbec4` (same 3 failures without the scheduling changes), so it did not block the §8.1 scheduling commit.
- Resolution (2026-06-28): no fix to apply — the flakiness was already eliminated structurally. Root cause and the load-bearing guard were both confirmed via a deterministic forced repro (DB lock → READ) that reverted cleanly. No loosened assertions, no second full context.
