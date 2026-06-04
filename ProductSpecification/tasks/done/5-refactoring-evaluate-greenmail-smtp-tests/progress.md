# Task 5: Evaluate GreenMail as alternative to Mailpit for SMTP integration tests — Progress

Type: refactoring

Tracks GitHub issue #98.

## Spec
- [x] spec

## Fix

### Step 1: Spike — evaluate GreenMail vs Mailpit & decide
- [x] spike — evaluated GreenMail in-JVM, GreenMail Docker, and current Mailpit on latency, realism, web UI,
  shared-first/container-fallback fit, and assertion ergonomics. **Decision: migrate to GreenMail in-JVM.** The
  latency bottleneck is the Docker Desktop port-proxy on server-first SMTP (server-agnostic), so GreenMail Docker
  would keep the ~9s tax; only leaving Docker (in-JVM) removes it. Decision + comparison recorded on issue #98
  (comment 4611891912).

### Step 2: Migrate to GreenMail in-JVM (decision from Step 1)

Sequenced so each commit leaves the build green: the listener (sets `spring.mail.port`) and the test client
(reads delivered mail) are coupled, so the harness switch must be one atomic commit. The first slice introduces the
GreenMail server + a proof test without touching the live Mailpit path.

- [x] refactor (added `greenmail-junit5` dep; built in-JVM `GreenMailServer` bootstrap + `GreenMailServerTest` proof
  — JavaMail→loopback delivery works near-instantly, `getReceivedMessages()` exposes subject/recipient/HTML body. The
  Mailpit path is untouched, so the full suite stays green.)
- [x] refactor (atomic harness switch: added `GreenMailServerTestExecutionListener` (starts `GreenMailServer`, sets
  `spring.mail.host/port` pre-boot, registered in `META-INF/services` in place of the Mailpit listener); replaced
  `MailpitTestClient` with `GreenMailTestClient` — `getReceivedMessages()` filtered by recipient, recursive decoded
  `text/html` extraction, returning a `DeliveredEmail` snapshot; updated `EmailStatements`/`StalePublicationStatements`
  to the snapshot API. Mail suite green: 4/4 — `UserRegistrationIntegrationTest` 0.37s & `SmtpRecovery…` 0.73s confirm
  the ~9s greeting tax is gone.)
- [x] refactor (cleanup: removed the `mailpit` service from `docker/infra-tests.yml` + its `docker/.env` vars; dropped
  the `mail.smtp.*` timeout/HELO-pin workaround and the `MailpitAutoConfiguration` exclude from `application-test.yml`
  (kept `spring.mail.host: localhost` — required for the `@ConditionalOnProperty` `SmtpEmailNotificationSender` bean in
  non-mail full-context runs); removed the `testcontainers-mailpit` dependency + version property and the dead Mailpit
  files (`MailpitContainerTestExecutionListener`, `MailpitContainersLifecycleManager`); fixed the now-dangling Mailpit
  Javadoc links in `GreenMailServer`/`SharedSpies`. Captured `GreenMailServer.instance()` in a `final GreenMail` field
  in `GreenMailTestClient`, making `receivedMessages()`/`clearInbox()` instance methods. Mail suite green: 4/4, still
  fast — dropping the HELO pin reintroduced no reverse-lookup slowdown.)
- [x] green-acceptance (mail integration suite green — 4/4 (`UserRegistrationIntegrationTest` 1.1,
  `SmtpRecoveryEmailDeliveryIntegrationTest` 5.1, `ExactlyOnceEmailDeliveryIntegrationTest` 6.1, plus
  `StaleIncompletePublicationIntegrationTest`); green on 3 consecutive runs, ~9s SMTP-greeting tax gone —
  `UserRegistration` 0.37s, `SmtpRecovery` 0.68s. Confirmation run only — no marker existed to remove.)

## Completion
- Decision (Step 1) recorded on issue #98; migration (Step 2) complete and verified. The full-suite mail flakiness
  (shared `JavaMailSender` spy + async race) remains tracked separately under Task 4 — out of scope here.

## Notes
- Step 1 was research/decision, not a TDD red/green cycle — no test-review/refactor sub-skills applied to the spike.
- Migration (Step 2) is verified by the existing full-context mail tests, not new tests (test-infra refactor).
- Orthogonal to Task 4 (flaky shared-spy/async race): GreenMail removes the Mailpit `Read timed out` symptom and
  shrinks the async-bleed window, but does not fix Task 4's root cause.
- Discovered during Story 2 (`green-acceptance`, Scenario 1.1).
