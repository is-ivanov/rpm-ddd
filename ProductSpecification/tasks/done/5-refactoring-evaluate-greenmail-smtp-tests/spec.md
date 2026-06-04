# Task 5: Evaluate GreenMail as alternative to Mailpit for SMTP integration tests

Type: refactoring

Tracks GitHub issue [#98](https://github.com/is-ivanov/rpm-ddd/issues/98) (labels: `tech-debt`, `testing`).

## Problem

Acceptance/integration tests send activation emails through a **real SMTP server (Mailpit)**, started as a shared
Docker container via `Infra-Tests-Up` (`docker/infra-tests.yml`), using the `testcontainers-mailpit` library plus a
custom `MailpitContainerTestExecutionListener` for shared-instance reuse.

On **Windows + Docker Desktop** the Mailpit path has a significant latency problem because **SMTP is
server-speaks-first**: the `220` greeting, routed through the Docker Desktop port-proxy, arrives only after a delay:

- `localhost:54025` → ~30s (the `::1`/IPv6 attempt stalls ~21s before falling back to IPv4)
- `127.0.0.1:54025` → ~9s (residual proxy latency for the server-first greeting)
- In-container probe (`nc 127.0.0.1 1025`) → instant — so Mailpit itself is fine; the cost is the host↔container path.

HTTP (Mailpit REST API, 8025) is unaffected because it is client-speaks-first. Reproduced with a plain Python
`smtplib` probe (not JavaMail), so it is **not** a JavaMail client-side hostname-lookup issue.

### Current workaround (committed, Story 2)

- `MailpitContainerTestExecutionListener` pins `127.0.0.1` (avoids the ~21s IPv6 stall) for both SMTP and the REST API.
- `application-test.yml` sets `mail.smtp.localhost` + `mail.from` (skip JavaMail reverse lookups) and raises
  `mail.smtp.{connection,read,write}timeout` to `20000ms` to tolerate the ~9s server-first greeting while still
  failing fast.
- Net effect: tests pass, but each SMTP-backed acceptance test pays ~9s on the greeting.

## Solution

Spike-first refactoring. **Step 1 is an evaluation/decision** comparing [GreenMail](https://greenmail-mail-test.github.io/greenmail/)
(an embeddable in-JVM SMTP/IMAP/POP3 server, also shipping a Docker image usable with Testcontainers) against the
current Mailpit setup. Migration steps (Step 2) are **conditional on the decision** — if the decision is *keep
Mailpit*, Step 2 is `[S]` and the task closes after recording the decision on issue #98.

Comparison dimensions (per #98):

- **Latency** — measure all three modes: GreenMail in-JVM (loopback, no Docker proxy → expected instant), GreenMail
  Docker/Testcontainers (re-measure whether the server-first greeting latency reappears through the proxy), and the
  current Mailpit container.
- **Realism** — real SMTP wire protocol vs in-JVM.
- **Web UI** — Mailpit has a UI for manual inspection; GreenMail in-JVM is test-only.
- **Fit with the shared-first / container-fallback infra pattern** (`project_reusable-test-infra-pattern`):
  in-JVM is per-JVM (no cross-run reuse); GreenMail Docker could mirror the DB/Mailpit shared-container reuse.
- **Assertion ergonomics** — GreenMail Java API (`getReceivedMessages()`) vs the Mailpit REST API.

Decision options: **migrate to GreenMail**, **keep Mailpit**, or **support both**. Record the decision (and the
measured numbers) on issue #98 and/or as an ADR. If migrating, plan the change to the Key Files below.

## Key Files

- `src/test/java/by/iivanov/rpm/testing/MailpitContainerTestExecutionListener.java` — shared-instance reuse listener
- `src/test/java/by/iivanov/rpm/iam/user/fixtures/MailpitTestClient.java` — Mailpit REST API client (assertions)
- `src/test/java/by/iivanov/rpm/iam/user/fixtures/EmailStatements.java` — mail test DSL
- `docker/infra-tests.yml` — shared Mailpit container definition
- `src/test/resources/application-test.yml` — `mail.smtp.*` timeouts + `127.0.0.1` pinning workaround

## Regression suite (verifies migration)

These existing full-context mail tests are the regression suite for any migration — they must stay green
deterministically:

- `by.iivanov.rpm.iam.user.UserRegistrationIntegrationTest` (Scenario 1.1)
- `by.iivanov.rpm.iam.user.SmtpRecoveryEmailDeliveryIntegrationTest` (Scenario 5.1)
- `by.iivanov.rpm.iam.user.ExactlyOnceEmailDeliveryIntegrationTest` (Scenario 6.1)

## References

- https://rieckpil.de/use-greenmail-for-spring-mail-javamailsender-junit-5-integration-tests/
- https://martinelli.ch/how-i-solved-smtp-timeouts-with-mailpit-and-javamail/
- Related (separate concern): Task 4 — flaky full-suite mail tests (shared spy + async race).
