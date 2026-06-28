# Task 231: Speed up incomplete-publication negative integration tests -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Investigate & baseline (local + CI)
- [x] investigate (capture baselines BEFORE changing anything: local repeated-run per-test timings for both tests AND GitHub CI stats via `gh run list` / `gh run view [--log]` + published surefire/Allure timing artifacts; confirm `.during(15s)` is the dominant cost; map the Modulith async republication executor used by `resubmitJob.resubmit()`; decide Option B vs A and record the chosen window/quiesce margin with rationale)

#### Findings (baseline)
Per-test duration:

| Test | CI (Allure, run 28320992757) | Local steady (surefire) |
|------|------|------|
| Stale | 15.63s | 15.69s |
| InFlight | 15.80s | 25.55s (incl. ~6.9s cold-start, first in run) |
| **Positive recovery** (`SmtpRecoveryEmailDeliveryIntegrationTest` — republish + await delivery) | **0.62s** | — |
| `ResubmitIncompletePublicationsJobTest` (unit) | 0.20s | — |

- `.during(NO_DELIVERY_WINDOW = 15s)` confirmed dominant: ~15s of each test's ~15.7s steady time.
- Async republication executor = `SimpleAsyncTaskExecutor` (threads `task-N`, `SimpleAsyncUncaughtExceptionHandler`) — **no pollable pool / active-count**, so a clean "wait for quiesce" handle does not exist without swapping the executor in tests (would risk forking the shared cached context — disallowed by single-context rule).
- In both negative tests the resubmit predicate (`ResubmitIncompletePublicationsJob.resubmit()`) **excludes** the test publication (stale: past 24h cutoff; in-flight: within grace), so no async republication task is created at all — the window only guards against a regression that wrongly resubmits.

#### Decision: Option A — shorten `NO_DELIVERY_WINDOW` from 15s to **3s**
Rationale:
- The full async republish → SMTP → GreenMail roundtrip is **0.62s on CI** (positive recovery test, identical path a wrongful resubmit would take). 3s ≈ 5× margin over the observed CI worst case.
- `assertActivationPublicationStaysIncompleteFor` is an independent second proof of "no resubmit" (a resubmit would complete the publication via `doCallRealMethod()`, dropping the incomplete count to 0).
- False-fail risk ≈ 0: each test uses a unique random recipient and clears the inbox, so no stray email can arrive in-window.
- Expected drop: each test ~15.7s → ~3.7s; serialized tail ~31s → ~7s (~24s saved).
- Option B rejected: `SimpleAsyncTaskExecutor` offers no deterministic quiesce handle; achieving one would require test-only executor wiring that risks a second cached full context.

### Step 2: Make the no-delivery assertion fast
- [~] refactor (rework `StalePublicationStatements.assertNoActivationEmailDeliveredTo`: Option B -- deterministically wait for the async republication executor to quiesce, then assert 0 emails; or Option A -- shorten `.during()` to the margin chosen in Step 1. Keep `assertActivationPublicationStaysIncompleteFor`. Do NOT weaken the assertion's teeth; do NOT touch the `doThrow().doCallRealMethod()` spy setup)

### Step 3: Verify no flakiness (local + CI)
- [ ] green-acceptance (run both tests 5+ times locally AND the full suite green; confirm zero flakiness and the wall-clock drop; cross-check the chosen margin against CI timing distributions before closing -- per the CI statistics requirement in spec.md)
