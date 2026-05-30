# Story 2: Email integration — send email on user registration — Interview

## Scope

**In scope:**
- Real delivery of the **account-activation email** triggered by `UserRegisteredEvent` (registration).
- Replace `NoOpEmailNotificationSender` (logs token to console) with a real SMTP-backed adapter.
- HTML activation email (Thymeleaf template) with a clickable activation link, plus plain-text fallback.
- Mail configuration (`spring.mail.*`, from-address, frontend base URL) wired per environment.
- Failure resilience: rely on Spring Modulith's persisted event registry + a scheduler that resubmits incomplete email publications.

**Out of scope:**
- Welcome / "account active" email after activation completes (future story).
- Password-reset email, notifications, admin alerts, any other email type.
- Admin-configurable / DB-stored email templates.
- Delivery analytics, bounce/webhook handling, provider dashboards.

## Key Architectural Decisions

- DECISION: **Transport = Spring `JavaMailSender` over SMTP.** Add `spring-boot-starter-mail`. Host/port/username/password come from `spring.mail.*` (env-driven), so the concrete provider (Brevo, SendGrid SMTP, Mailtrap, Gmail, etc.) is chosen at deploy time **without code changes**. The adapter stays provider-agnostic.
- DECISION: **Email body = HTML via Thymeleaf template + plain-text alternative.** Templates live under `src/main/resources/templates/email/`. The HTML is the primary part; a plain-text alternative is included for non-HTML clients (multipart message).
- DECISION: **Activation link points at the frontend.** Format: `{app.frontend-base-url}/activate?token={jwt}`. The base URL is config-driven (`localhost:5173`-style in dev, the Render URL in prod). The link targets the Vue activation page from Story 1 (two-step set-password flow) — NOT the backend `/api/auth/activate` JSON endpoint.
- DECISION: **Sender identity is config-driven.** From: `RPM Platform <no-reply@rpm-platform.com>` (from-address is an app/`spring.mail` property so it can differ per environment). Subject: `Activate your RPM account`.
- DECISION: **Failure handling = Modulith persisted events + resubmit scheduler.** The `@ApplicationModuleListener` already persists each event via the Spring Modulith JDBC event publication registry. A failed SMTP send leaves the publication **incomplete** in the registry table. A `@Scheduled` job runs **every 5 seconds**, reads incomplete publications from the registry, and **resubmits** them.
- DECISION: **Resubmit has a 24h age cutoff.** The scheduler only resubmits publications **younger than 24h** (filter via Spring Modulith's `IncompleteEventPublications` API). A permanently-failing recipient stops being retried after 24h — no infinite 5-second resubmit loop.

## Business Rules & Constraints

- Exactly one activation email is sent per `UserRegisteredEvent` (one per successful registration).
- The activation email must contain a working link to the frontend activation page carrying the JWT activation token.
- The JWT activation token is generated in the existing `UserRegisteredEventListener` (Story 1) — Story 2 does NOT change token generation, only how the token reaches the user.
- Email send is asynchronous (Modulith listener) — registration response does not block on SMTP.
- On transient SMTP failure: the event stays incomplete and is resubmitted by the scheduler (every 5s, up to 24h old).
- From-address, subject, frontend base URL, and SMTP credentials are all externalized config — never hardcoded in the adapter.

## Already Implemented (REUSE)

- **`UserRegisteredEvent`** (`iam.user.domain`) — `record(UserId, Login, EmailAddress)`, published on registration.
- **`UserRegisteredEventListener`** (`iam.user.infrastructure.events`) — `@ApplicationModuleListener`; generates JTI + JWT activation token and calls `EmailNotificationSender.sendActivationToken(email, login, token)`. Persisted via Modulith.
- **`EmailNotificationSender`** (`iam.user.infrastructure.notification`) — the port: `void sendActivationToken(String toEmail, String login, String activationToken)`. **Reused as-is.**
- **`NoOpEmailNotificationSender`** — `@Primary` stub that logs the token. **Replaced** by the real adapter in this story (demote/remove `@Primary`; optionally keep behind a dev profile or delete).
- **`JwtActivationTokenGenerator` / `JtiGenerator`** — produce the activation token. Unchanged.
- **`UserRegistrationService`** — registers user (placeholder password, PENDING) and publishes `UserRegisteredEvent`. Unchanged.
- **Spring Modulith JDBC event registry** — `spring-modulith-starter-jdbc` already on the classpath; event publications are persisted, enabling the resubmit strategy.

## NOT Yet Implemented (Gaps)

- `spring-boot-starter-mail` dependency in `pom.xml`.
- `spring-boot-starter-thymeleaf` (or template engine) for HTML email rendering.
- SMTP config: `spring.mail.*` in `application-local.yml` (point at local Mailpit) and `application-prod.yml` (env vars).
- App config properties: `app.frontend-base-url`, `app.mail.from` (from-address + display name).
- **`SmtpEmailNotificationSender`** — real adapter implementing `EmailNotificationSender`: renders the Thymeleaf template, builds a multipart (HTML + text) `MimeMessage`, sends via `JavaMailSender`. Becomes the `@Primary`/sole bean.
- **Thymeleaf email template(s)** under `templates/email/` — HTML activation email + plain-text alternative; activation link built from `app.frontend-base-url`.
- **Resubmit scheduler** — `@Scheduled(fixedRate=5s)` component that resubmits incomplete Modulith publications younger than 24h (`IncompleteEventPublications`). Requires `@EnableScheduling`.
- **`FakeEmailNotificationSender`** (test infra) — in-memory fake for usecase-level tests (captures last/all sent activation tokens).
- **Reusable Mailpit test infrastructure** (mirrors the existing DB setup — built at the acceptance phase):
  - `ch.martinelli.oss:testcontainers-mailpit` dependency (`test` scope) — `MailpitContainer` + `MailpitClient` + AssertJ polling assertions.
  - `mailpit` service added to `docker/infra-tests.yml` (image + fixed ports `54025:1025` SMTP / `54825:8025` HTTP, tmpfs). `Infra-Tests-Up` already deploys this file, so it starts Mailpit too — **no run-config change**.
  - `MAILPIT_*` vars in `docker/.env` (image + ports) so compose and the Testcontainer stay in sync (same convention as `POSTGRES_*`).
  - `MailpitContainersLifecycleManager` — singleton, reusable Mailpit container (mirrors `PostgresContainersLifecycleManager`); uses the library's `MailpitContainer`, **not** `@ServiceConnection`.
  - `MailpitContainerTestExecutionListener` — counts `@Tag("mail")` tests; probes the shared Mailpit at `localhost:54025`, reuses if alive, else starts the Testcontainer; sets `spring.mail.*` system properties. Registered in `META-INF/services/org.junit.platform.launcher.TestExecutionListener`.
  - `Constants.MAIL_TEST_TAG = "mail"` + a `MailTest` meta-annotation (`@Tag("mail")`) on email-touching tests (mirrors `DbTest`).
  - SMTP-timeout / DNS fix in `application-test.yml` (see Testing Considerations).

## Cross-Story Dependencies

- **Depends on Story 1 (User login).** Reuses the registration flow, `UserRegisteredEvent`, the `EmailNotificationSender` port, the JWT token generator, and the Modulith event registry. The activation link points at Story 1's frontend activation page.
- No downstream story depends on Story 2 yet. Completing it makes the Story 1 activation flow work end-to-end via real email instead of console logs.

## Testing Considerations

- **Acceptance / e2e (Level 1): Mailpit, reusing the DB infra pattern.** Register a user, then assert the captured email — recipient, from-address, subject (`Activate your RPM account`), and the activation link (`{frontend-base-url}/activate?token=...`) in the body — via the library's `MailpitClient` / AssertJ assertions.
  - DECISION: **Mirror the DB "shared-first, container-fallback" approach.** A `MailpitContainerTestExecutionListener` (JUnit `TestExecutionListener`) detects `@Tag("mail")` tests, probes the shared Mailpit at `localhost:54025` (started by `Infra-Tests-Up` via `docker/infra-tests.yml`), and reuses it; only if unreachable does it cold-start a reusable Testcontainer. It sets `spring.mail.*` system properties — **no `@DynamicPropertySource` / `@ServiceConnection`** (consistent with `DbContainerTestExecutionListener`).
  - DECISION: **Use `ch.martinelli.oss:testcontainers-mailpit`** for the `MailpitContainer` (fallback) and the `MailpitClient` + **AssertJ polling assertions** (`awaitMessage().withSubject(...).withTimeout(...)`). Polling is required because the send is async (Modulith listener) — assertions must await delivery, never sleep. We skip the library's `@ServiceConnection` so the shared-instance reuse logic stays in our listener.
  - DECISION: **Apply the JavaMail SMTP-timeout / DNS fix in `application-test.yml`** (also recommended for prod). JavaMail's implicit reverse-DNS `getLocalHost()` during the `EHLO`/`MAIL FROM` handshake stalls in Docker/CI; pin it and bound the timeouts:
    ```yaml
    spring.mail.properties.mail.smtp.localhost: localhost
    spring.mail.properties.mail.smtp.connectiontimeout: 5000
    spring.mail.properties.mail.smtp.timeout: 5000
    spring.mail.properties.mail.smtp.writetimeout: 5000
    ```
  - Clear captured messages between tests (Mailpit `DELETE /api/v1/messages` / client reset) so assertions stay deterministic across the shared, reused instance.
- **Usecase (Level 3): in-memory fake.** Use `FakeEmailNotificationSender` to assert the listener/flow invoked the port with the right recipient/login/token — no SMTP, no containers.
- **Resubmit behavior (integration):** simulate a failed first send → publication stays incomplete → scheduler resubmits within the window → email ultimately delivered (and NOT resubmitted past the 24h cutoff).
- Keep acceptance scenarios few (one per endpoint-behavior category): the happy path is "registering a user results in a delivered activation email." Resubmit/age-cutoff detail belongs in cheaper integration/unit tests.

## References

1. Testing emails with Testcontainers and Mailpit — https://martinelli.ch/testing-emails-with-testcontainers-and-mailpit/ (`ch.martinelli.oss:testcontainers-mailpit`: `MailpitContainer` ports 1025/8025, `MailpitClient.getAllMessages()`, AssertJ polling `awaitMessage()`).
2. Solving SMTP timeouts with Mailpit and JavaMail — https://martinelli.ch/how-i-solved-smtp-timeouts-with-mailpit-and-javamail/ (implicit reverse-DNS during the SMTP handshake → set `mail.smtp.localhost` + bound timeouts).

## Performance / Load

- Email volume is tiny (one per registration; registrations are rare relative to the 40k/day IoT reading load). No rate-limit or throughput concerns.
- Sending is async (Modulith listener) and never blocks the registration HTTP response.
- The 5-second resubmit scheduler scans only **incomplete** publications (normally empty), so steady-state cost is negligible.
