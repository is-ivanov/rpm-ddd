# Task 4: Full-suite mail integration tests are flaky (shared JavaMailSender spy + async race)

Type: bug

## Symptom

Running the **full** suite `./mvnw test`, three full-context mail integration tests error with
`org.awaitility.core.ConditionTimeoutException` after 15s — the activation email is never delivered:

- `by.iivanov.rpm.iam.user.ExactlyOnceEmailDeliveryIntegrationTest` (6.1)
- `by.iivanov.rpm.iam.user.SmtpRecoveryEmailDeliveryIntegrationTest` (5.1)
- `by.iivanov.rpm.iam.user.UserRegistrationIntegrationTest` (1.1)

`Tests run: 115, Failures: 0, Errors: 3` → BUILD FAILURE. Each errors at `MailpitTestClient.awaitMessageDeliveredTo`
(predicate returns false for `Optional.empty`).

**They pass in isolation** (and a 5-class targeted run incl. 6.1/7.1/5.1 passes). The failure only appears in the
full suite, and it is **order/timing dependent**.

## Pre-existing — NOT caused by the scheduling work (Story 2 §8.1)

Reproduced **identically at commit `f2dbec4`** (before any of the scheduler-wiring / Modulith-Moments-exclusion
changes): same 3 classes, same 15s timeout. The scheduling guard test proves no scheduler runs in the test
context, so this is unrelated to scheduling. It is a pre-existing mail-test-isolation defect surfaced when the
mail tests run together under load.

## Root cause (from the failure logs — verify before fixing)

The `JavaMailSender` is a shared `@MockitoSpyBean` declared on `AbstractApplicationIntegrationTest` (via
`SharedSpies`) — one instance across the cached full context. `StalePublicationStatements.givenActivationSendFails()`
arms it with `doThrow(MailSendException).doCallRealMethod().when(mailSender).send(any(MimeMessage.class))` (first
send throws, later sends real). Two leaks combine:

1. **Async bleed:** the activation send runs in an async `@ApplicationModuleListener`; sends from one test's
   registration are still in flight when the next test starts (logs show 4–5 concurrent `User registered` events
   across `task-3..7`).
2. **Shared-spy stub leak:** the armed `doThrow` "first throw" is consumed by a *different* test's send. Tests that
   expect a successful delivery (6.1, 1.1) then hit `Simulated SMTP failure` or Mailpit `SocketTimeoutException:
   Read timed out`, so no message is delivered → 15s timeout.

See project memory: `feedback_shared-context-spy-not-import`, `feedback_modulith-incomplete-publication-race`
(reset constraints), `project_reusable-test-infra-pattern` (Mailpit).

## Fix direction (candidates — implementer chooses)

- Await async-event **quiescence** between mail tests (no in-flight publications) before the next test arms/uses the spy.
- Re-establish the spy baseline (`doCallRealMethod`) deterministically per test, honoring the modulith
  incomplete-publication-race constraint (do **not** `reset()` mid-flight — see memory).
- Make the SMTP-failure arming local so its first-throw cannot be consumed by another test's send.
- Investigate Mailpit `Read timed out` under concurrent load (the `test` profile already sets generous
  `mail.smtp.*` timeouts; confirm whether contention or the spy leak is the dominant cause).

## Acceptance

`./mvnw test` (full suite) is green **deterministically across repeated runs** — the 3 mail tests pass within the
suite, not only in isolation. No loosened assertions, no `@DirtiesContext` that forks a second full context
(single-context rule).
