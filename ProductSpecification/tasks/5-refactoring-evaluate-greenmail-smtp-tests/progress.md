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
- [~] refactor (atomic harness switch: add a GreenMail `TestExecutionListener` that starts `GreenMailServer` and sets
  `spring.mail.host/port` in `testPlanExecutionStarted` when `mail`-tagged tests exist; swap `MailpitTestClient`/
  `EmailStatements` to the GreenMail Java API — `getReceivedMessages()` filtered by recipient, `MimeMessage` HTML-body
  extraction; deregister the Mailpit listener from `META-INF/services`) — run mail integration suite green
- [ ] refactor (cleanup: remove the `mailpit` service from `docker/infra-tests.yml`; drop the `mail.smtp.*` timeout +
  `127.0.0.1`-pin workaround and the `MailpitAutoConfiguration` exclude from `application-test.yml`; remove the
  `testcontainers-mailpit` dependency and the dead Mailpit files)
- [ ] green-acceptance (mail integration suite green deterministically — `UserRegistrationIntegrationTest` 1.1,
  `SmtpRecoveryEmailDeliveryIntegrationTest` 5.1, `ExactlyOnceEmailDeliveryIntegrationTest` 6.1 — and the ~9s tax gone)

## Notes
- Step 1 was research/decision, not a TDD red/green cycle — no test-review/refactor sub-skills applied to the spike.
- Migration (Step 2) is verified by the existing full-context mail tests, not new tests (test-infra refactor).
- Orthogonal to Task 4 (flaky shared-spy/async race): GreenMail removes the Mailpit `Read timed out` symptom and
  shrinks the async-bleed window, but does not fix Task 4's root cause.
- Discovered during Story 2 (`green-acceptance`, Scenario 1.1).
