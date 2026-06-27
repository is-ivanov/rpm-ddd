# Task 231: Speed up incomplete-publication negative integration tests -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Investigate & baseline (local + CI)
- [ ] investigate (capture baselines BEFORE changing anything: local repeated-run per-test timings for both tests AND GitHub CI stats via `gh run list` / `gh run view [--log]` + published surefire/Allure timing artifacts; confirm `.during(15s)` is the dominant cost; map the Modulith async republication executor used by `resubmitJob.resubmit()`; decide Option B vs A and record the chosen window/quiesce margin with rationale)

### Step 2: Make the no-delivery assertion fast
- [ ] refactor (rework `StalePublicationStatements.assertNoActivationEmailDeliveredTo`: Option B -- deterministically wait for the async republication executor to quiesce, then assert 0 emails; or Option A -- shorten `.during()` to the margin chosen in Step 1. Keep `assertActivationPublicationStaysIncompleteFor`. Do NOT weaken the assertion's teeth; do NOT touch the `doThrow().doCallRealMethod()` spy setup)

### Step 3: Verify no flakiness (local + CI)
- [ ] green-acceptance (run both tests 5+ times locally AND the full suite green; confirm zero flakiness and the wall-clock drop; cross-check the chosen margin against CI timing distributions before closing -- per the CI statistics requirement in spec.md)
