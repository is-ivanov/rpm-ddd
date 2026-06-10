# Task 148: Resubmit job re-processes in-flight event publications → duplicate activation emails -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: add a grace-period lower bound to the resubmit predicate
- [x] red-acceptance
- [x] red-usecase
- [x] green-usecase
- [~] adapters-discovery
- [ ] green-acceptance

Notes for the fix session (refine on bootstrap/discovery):
- `red-acceptance` here is integration-level (Modulith event republish), not HTTP: assert that ONE
  registration delivers exactly ONE activation email even while the resubmit scheduler ticks — drive
  it with the test `Clock` so an in-flight publication stays within the grace window and is NOT
  resubmitted. Tag the test with issue #148.
- `red-usecase` = pure predicate/selection test on `ResubmitIncompletePublicationsJob` with the test
  `Clock`: younger than grace → NOT selected; between grace and 24h → selected; older than 24h →
  NOT selected. Tag with #148.
- `adapters-discovery` likely `[S]` (no new ports/adapters — pure scheduled-job logic).
- Keep the production-schedule wiring test green (per tdd-rules "Scheduled / Recurring Jobs").
- **green-usecase MUST update `SmtpRecoveryEmailDeliveryIntegrationTest`** (collateral, not yet a
  checkbox): it currently feeds a *young* incomplete publication (`givenYoungIncompletePublicationFor`,
  no clock advance) and asserts the email IS redelivered. Once the grace lower bound lands, a young
  publication is no longer resubmitted, so that test will break (no delivery). Fix: advance the test
  clock PAST the grace period before `whenResubmitSchedulerRuns()` so the publication is "older than
  grace, younger than 24h" — e.g. add `StalePublicationStatements.givenIncompletePublicationOlderThanGraceFor(email)`.
  This is NOT loosening an assertion to match behavior — it realigns the test's premise with the new
  spec (recovery happens AFTER grace, not while in-flight). This existing integration test is the
  Level-1 proof of "after grace → resubmitted → delivered"; the three selection boundaries themselves
  are covered cheaply by red-usecase. Do NOT add a new acceptance test for the after-grace case.
