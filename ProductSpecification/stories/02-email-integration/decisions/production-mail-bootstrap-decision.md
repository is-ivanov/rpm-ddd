# Decision: Production mail bootstrap — fail-fast prod config, conditional SMTP sender, local Mailpit

**Date**: 2026-06-02 **Scenarios**: 7.1

The unconditional `@Primary SmtpEmailNotificationSender` hard-requires a `JavaMailSender`, but `application-prod.yml` declares no `spring.mail.*`, so the production context cannot start on Render.

| Rejected | Why |
|----------|-----|
| Keep the unconditional `@Primary` Smtp sender + `NoOp` coexistence | The sender hard-requires `JavaMailSender`; with prod mail absent the failure is a buried bean-creation stack trace, and the spec explicitly wants the NoOp+Smtp coexistence replaced. |
| Fall back to `NoOp` when prod mail is absent | Email is required for activation — silently logging tokens means users can never activate. A silent prod misconfiguration is worse than a loud startup failure. |
| `spring.mail.*` with **default** values in prod.yml | A default host lets the context start misconfigured and fail only at first send; required env bindings (no default) fail fast at startup, attributable to mail. |
| Gate the sender with `@ConditionalOnBean(JavaMailSender.class)` | Order-fragile on a component-scanned bean — the autoconfigured `JavaMailSender` is registered after user components, so the condition can evaluate false. Gate on the property that triggers `MailSenderAutoConfiguration` instead. |
| Share one mail server between tests and local dev | Tests are moving toward GreenMail in-JVM (in-process, no UI — #98); local dev needs a UI mailcatcher. Keep them independent so the #98 migration never touches local. |
| File-writing local `JavaMailSender` / keep `NoOp` for local | Both add production code (and a TDD cycle) and feed the iam-transport-extraction debt (#97); local Mailpit is config-only and ships a web UI. |

**Chosen**: Production fails fast — `application-prod.yml` declares `spring.mail.{host,port,username,password}` bound to **required** `${SPRING_MAIL_*}` env vars (no defaults, mirroring `${SPRING_DATASOURCE_URL}`); a missing var leaves the placeholder unresolved → loud startup failure. `SmtpEmailNotificationSender` drops `@Primary`, gains `@ConditionalOnProperty(prefix = "spring.mail", name = "host")`, and becomes the **only** `EmailNotificationSender` (`NoOpEmailNotificationSender` is removed). Every environment supplies a `JavaMailSender`: test → Mailpit, local → Mailpit, prod → real SMTP. Local dev runs Mailpit via `docker/services.yml` with `application-local.yml` pointing `spring.mail.*` at it — config only, independent of the #98 test-server choice.

## Model

- `application-prod.yml` — add `spring.mail.host/port/username/password` bound to required `${SPRING_MAIL_HOST}`/`…PORT`/`…USERNAME`/`…PASSWORD` (no defaults); add `spring.mail.properties.mail.smtp.*` (TLS/timeouts) as the deploy provider requires.
- `application-local.yml` — add `spring.mail.host: localhost` + `spring.mail.port` (local Mailpit SMTP).
- `docker/services.yml` — add a `mailpit` service (SMTP + `8025` web UI) for local dev, separate from `docker/infra-tests.yml`.
- `SmtpEmailNotificationSender` — drop `@Primary`; add `@ConditionalOnProperty(prefix = "spring.mail", name = "host")`; sole `EmailNotificationSender`.
- Remove `NoOpEmailNotificationSender` (replaces the unconditional NoOp+Smtp coexistence).
- Wiring test `iam.user.infrastructure.notification.ProductionMailBootstrapTest` — a sliced `ApplicationContextRunner` (mirrors `EventResubmitSchedulingTest`; must **not** fork the shared full acceptance context). Loads with the prod mail configuration supplied; asserts `hasNotFailed()`, `hasSingleBean(JavaMailSender.class)`, and `hasSingleBean(EmailNotificationSender.class)` (the Smtp sender is the one wired).

## RED expectation

`hasSingleBean(EmailNotificationSender.class)` fails **today** — `NoOp` and the unconditional `Smtp` sender both register (two beans). After removing `NoOp` and gating `Smtp`, exactly one sender remains → GREEN. This is the legitimate red state for the scenario; the positive happy-path alone (context starts when mail is configured) already passes via autoconfiguration and is not red on its own.

## Edge Cases

| Case | Behavior |
|------|----------|
| Prod mail env vars unset | `${SPRING_MAIL_HOST}` unresolved → context fails loudly at startup (intended fail-fast). |
| Mail configured (test / local / prod) | `MailSenderAutoConfiguration` contributes `JavaMailSender`; the `@ConditionalOnProperty` Smtp sender contributes as the sole `EmailNotificationSender`. |
| Context with no `spring.mail.host` (slice/unit) | Smtp sender absent (conditional) — only matters where the sender is injected; the full context that hosts the listener always has Mailpit. |
