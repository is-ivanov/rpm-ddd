# Infrastructure Tests — Email Integration

> **Implementation Order**: SMTP unavailable does not break registration → recovery delivers the pending email after the SMTP server returns.

---

## 4. SMTP Unavailable During Registration

### 4.1 SMTP unavailable does not fail registration

**Given** the SMTP server is unavailable
**When** a user is registered
**Then** the registration succeeds
**And** no activation email is delivered yet
**And** the event publication for the activation email remains incomplete

---

## 5. SMTP Recovery

### 5.1 Activation email is delivered after SMTP recovers

**Given** the SMTP server was unavailable when a user registered
**And** the activation email publication remains incomplete
**When** the SMTP server becomes available
**Then** the activation email is delivered to the registered email address without re-registering the user

---

## 7. Production Mail Bootstrap

> The acceptance/integration suites run under the `test` profile, where Mailpit supplies a
> `JavaMailSender` via `spring.mail.host`. Production (`prod` profile) had **no** `spring.mail.*`, so
> `MailSenderAutoConfiguration` never contributes a `JavaMailSender` and the unconditional, `@Primary`
> `SmtpEmailNotificationSender` fails to construct — the application context does not start on the
> deployed environment. This scenario guarantees the production context boots with mail configured.
> The `design` step must decide, via `/architecture` (ADR):
> 1. **Sender selection** — real SMTP sender when mail is configured vs. a fallback when it is absent,
>    and whether a missing *production* mail config should fail fast (preferred for a feature that needs email).
> 2. **Local development mail** — how the app runs locally without a real SMTP provider. Options to weigh:
>    (a) raise an SMTP server (e.g. Mailpit) in a local Docker image; (b) a `local`-profile `JavaMailSender`
>    implementation that writes each message to disk as two files — the rendered body (`.html`/text) and a
>    metadata `.json` (from/to/subject); (c) keep the logging `NoOpEmailNotificationSender`. This replaces
>    the current unconditional coexistence of `NoOp` + `Smtp` senders.

### 7.1 The application context starts with the production mail configuration

**Given** the production profile with SMTP mail settings supplied via environment configuration
**When** the application context is bootstrapped
**Then** the `JavaMailSender` bean is available
**And** the SMTP activation sender is wired and the context starts successfully

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the SMTP server is unavailable` | Point `spring.mail.*` at an unreachable host/port, or stop the Mailpit container; verify the SMTP port refuses connections |
| `a user is registered` | The registration flow runs, persists the PENDING user, and publishes `UserRegisteredEvent` |
| `the registration succeeds` | Registration returns its normal success response — the async email send never propagates an exception to the HTTP response |
| `no activation email is delivered yet` | Poll Mailpit for a short bounded window and assert no message exists for the registered recipient |
| `the event publication for the activation email remains incomplete` | The Spring Modulith JDBC event publication registry holds an incomplete publication for the `UserRegisteredEvent` listener |
| `the SMTP server becomes available` | Restore `spring.mail.*` to the live Mailpit instance / restart the Mailpit container; verify the SMTP port accepts connections |
| `the activation email is delivered ... without re-registering the user` | The resubmit scheduler reprocesses the incomplete publication; poll Mailpit (`awaitMessage()`) until the message for the registered recipient appears |
| `the production profile with SMTP mail settings supplied via environment configuration` | `application-prod.yml` declares `spring.mail.*` (host/port/username/password/TLS) bound to environment variables; the test supplies prod-like mail properties |
| `the application context is bootstrapped` | A focused Spring Boot context test loads with the production mail configuration as a sliced/partial context — it must NOT fork the shared full acceptance context (single-context rule) |
| `the JavaMailSender bean is available` | Spring Boot's `MailSenderAutoConfiguration` contributes a `JavaMailSender` because `spring.mail.host` is set |
| `the SMTP activation sender is wired and the context starts successfully` | `SmtpEmailNotificationSender` resolves its `JavaMailSender` dependency and the context refresh completes without a bean-creation failure |
