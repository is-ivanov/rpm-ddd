# Email Integration - Notes & Considerations

## Warnings

### Functional Warnings
- Exactly one email per `UserRegisteredEvent` — a buggy resubmit could double-send. The Modulith registry marks a publication complete only after the listener returns successfully, so the resubmit set must exclude already-completed publications (use `IncompleteEventPublications`, not a manual scan).
- The activation link must point at the **frontend** activation page (`{frontend-base-url}/activate?token=`), NOT the backend `/api/auth/activate` JSON endpoint. Wrong target breaks the two-step set-password flow from Story 1.
- The 24h age cutoff means a permanently-failing recipient silently stops being retried. There is no dead-letter / alert in this story — failures past 24h are dropped.

### Technical Warnings
- **JavaMail SMTP-timeout / reverse-DNS stall**: JavaMail's implicit `getLocalHost()` during the `EHLO`/`MAIL FROM` handshake can hang in Docker/CI. Pin `mail.smtp.localhost: localhost` and bound `connectiontimeout`/`timeout`/`writetimeout` to 5000ms in `application-test.yml` (and recommended for prod). See reference 2.
- Demoting/removing `@Primary` from `NoOpEmailNotificationSender`: ensure exactly one primary `EmailNotificationSender` bean remains after `SmtpEmailNotificationSender` is added, or context startup fails on ambiguous bean resolution. Optionally keep the no-op behind a dev profile.
- `@EnableScheduling` is required for the resubmit job — easy to forget; without it the scheduler silently never runs.
- Async send means the registration response returns before delivery. Acceptance assertions MUST poll (AssertJ `awaitMessage()`), never sleep or assert immediately.

---

## Suggestions & Future Enhancements

### Functional Suggestions
- Welcome / "account active" email after activation completes (explicitly out of scope — future story).
- Password-reset email reuses the same SMTP adapter + Thymeleaf infrastructure built here.
- Dead-letter handling / ops alert for publications that exceed the 24h cutoff.

### Technical Suggestions
- Admin-configurable / DB-stored templates (out of scope; templates are file-based under `templates/email/`).
- Delivery analytics, bounce/webhook handling (out of scope).

---

## Technical Notes

### Load Considerations
- Email volume is tiny: one per registration, and registrations are rare relative to the 40k/day IoT reading load. No rate-limit or throughput concerns.
- The 5s resubmit scheduler scans only **incomplete** publications (normally empty), so steady-state cost is negligible.

### Security Considerations
- SMTP credentials are externalized (`spring.mail.username`/`password` via env vars in prod) — never committed.
- The activation JWT travels in the email link; token lifetime and validation are owned by Story 1 (unchanged here).
- From-address spoofing / SPF/DKIM are a deploy-time provider concern, not application code.

### Infrastructure Notes
- New dependencies: `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf`, `ch.martinelli.oss:testcontainers-mailpit` (test scope).
- Config additions: `spring.mail.*` (local → Mailpit, prod → env vars), `app.frontend-base-url`, `app.mail.from`.
- Reusable Mailpit test infra (built at acceptance phase, mirrors DB setup):
  - `mailpit` service in `docker/infra-tests.yml` (fixed ports `54025:1025` SMTP / `54825:8025` HTTP, tmpfs); `Infra-Tests-Up` already deploys this file.
  - `MAILPIT_*` vars in `docker/.env` (same convention as `POSTGRES_*`).
  - `MailpitContainersLifecycleManager` (singleton reusable container, no `@ServiceConnection`).
  - `MailpitContainerTestExecutionListener` — counts `@Tag("mail")` tests, probes shared Mailpit at `localhost:54025`, reuses if alive else cold-starts container, sets `spring.mail.*` system properties. Registered via `META-INF/services/org.junit.platform.launcher.TestExecutionListener`.
  - `Constants.MAIL_TEST_TAG = "mail"` + `MailTest` meta-annotation (mirrors `DbTest`).

### Integration Notes
- Provider-agnostic: Brevo, SendGrid SMTP, Mailtrap, Gmail, etc. are all selected via `spring.mail.*` at deploy time — no code change.
- Async delivery via the Spring Modulith `@ApplicationModuleListener` + JDBC event publication registry (`spring-modulith-starter-jdbc`, already present).
- Resubmit via Spring Modulith `IncompleteEventPublications` API with a 24h age filter.

---

## Additional Context

- See `interview.md` for the full decision log, reuse inventory, and gap list.
- Reference 1: Testing emails with Testcontainers and Mailpit — https://martinelli.ch/testing-emails-with-testcontainers-and-mailpit/
- Reference 2: Solving SMTP timeouts with Mailpit and JavaMail — https://martinelli.ch/how-i-solved-smtp-timeouts-with-mailpit-and-javamail/
- **Depends on Story 1 (User login)** — reuses registration flow, `UserRegisteredEvent`, the port, the JWT generator, the Modulith registry, and the frontend activation page. No downstream story depends on Story 2 yet.
