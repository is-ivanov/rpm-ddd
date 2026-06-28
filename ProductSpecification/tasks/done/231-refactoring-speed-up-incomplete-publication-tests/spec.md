# Task 231: Speed up incomplete-publication negative integration tests

Type: refactoring
Issue: #231

## Problem

Two full-context integration tests each take ~15s and run back-to-back on a single thread,
dominating the suite wall-clock (~30s of single-threaded tail in a ~50s run — observed via the
Allure Timeline after Task #224):

- `StaleIncompletePublicationIntegrationTest.when_resubmitSchedulerRuns_expect_stalePublicationNotResubmitted_andNoEmailDelivered`
- `InFlightIncompletePublicationIntegrationTest.when_resubmitSchedulerRuns_expect_inFlightPublicationNotResubmitted_andNoDuplicateEmailDelivered`

Each spends ~15s in `StalePublicationStatements.assertNoActivationEmailDeliveredTo`, which uses an
Awaitility `.during(NO_DELIVERY_WINDOW = 15s)` no-delivery window that always waits the full 15
seconds. The two tests cannot run concurrently with each other: both are DB-lane
(`@ResourceLock("DB")`) AND share mutable singletons in the cached context (the `JavaMailSender`
spy, `MutableClock`, GreenMail inbox, event publication registry), so serialization is required for
correctness — they alternate `15s + 15s = 30s`.

This is **not** a regression from Task #224 (`@ResourceLock`). The tests were always this slow; the
parallelization work just made it visible. The `@ResourceLock` serialization is correct and must
stay.

## Solution

Investigate and reduce the per-test wall-clock **without introducing flakiness** (these are
race-sensitive — see the Modulith incomplete-publication race history; never restore a failing spy
mid-flight, use `doThrow().doCallRealMethod()`):

- **Option B (preferred):** make the no-delivery assertion deterministic — wait for the Modulith
  async republication executor to quiesce, then assert zero delivered emails, instead of watching
  the inbox for a fixed 15s window.
- **Option A (fast fallback):** shorten the `.during()` window from 15s to ~2-3s. The wrongful
  resubmit path republishes synchronously inside `resubmitJob.resubmit()` and the async listener
  fires within hundreds of ms, so a few seconds keeps the assertion's teeth.

Note: `assertActivationPublicationStaysIncompleteFor` (registry holds exactly 1 incomplete
publication) already largely proves "no resubmit happened" — a resubmit would have completed the
publication via `doCallRealMethod()`. The window only needs to let any in-flight async task settle.

**Hard constraint: must not introduce flakiness.** Verify with repeated runs.

### CI statistics requirement (read at implementation start)

When implementing, the agent MUST consult CI pipeline logs/statistics on GitHub — `gh run list`,
`gh run view [--log]`, and per-test timing from CI runs (Allure/surefire artifacts if published) —
**IN ADDITION** to local repeated-run timings. CI is typically slower and noisier than local, so the
chosen window margin (Option A) or quiesce timeout (Option B) must be validated against real CI
timing distributions, and flakiness must be confirmed absent across both environments before the
task is considered done.

## Key Files

- `src/test/java/by/iivanov/rpm/iam/user/fixtures/StalePublicationStatements.java` (the `.during(15s)` window)
- `src/test/java/by/iivanov/rpm/iam/user/StaleIncompletePublicationIntegrationTest.java`
- `src/test/java/by/iivanov/rpm/iam/user/InFlightIncompletePublicationIntegrationTest.java`
- `src/test/java/by/iivanov/rpm/iam/user/fixtures/EmailStatements.java` (`whenResubmitSchedulerRuns` → `resubmitJob.resubmit()`)
- `src/main/.../shared/infrastructure/events/ResubmitIncompletePublicationsJob.java` (resubmit decision + async republication path)

## Full-stack journey verdict

`no-impact` — test-infrastructure speedup only; no production behaviour or rendered critical-path change.
