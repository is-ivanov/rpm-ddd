# Task 148 — Journey Summary

## red-acceptance (2026-06-10)

**Surprise:** `SmtpRecoveryEmailDeliveryIntegrationTest` already asserts a *young* incomplete publication (`givenYoungIncompletePublicationFor`, clock not advanced) IS resubmitted and the email delivered — the exact opposite of the new `InFlightIncompletePublicationIntegrationTest`, which requires a young publication to NOT be resubmitted.
**Why:** Today the resubmit predicate has only a 24h upper cutoff and no grace lower bound, so "young → redelivered" and "young → not redelivered" cannot both be true; the grace boundary is what splits the young window into in-flight (skip) vs older-than-grace (resubmit).
**Impact:** green-usecase will break `SmtpRecoveryEmailDeliveryIntegrationTest` once the grace lower bound lands; it must advance the test clock past grace (older-than-grace, younger-than-24h) before `whenResubmitSchedulerRuns()` — realigning the premise, not loosening an assertion.
