# Task 148: Resubmit job re-processes in-flight event publications → duplicate activation emails -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: add a grace-period lower bound to the resubmit predicate
- [x] red-acceptance
- [~] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
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
