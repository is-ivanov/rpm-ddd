# Decision: Production mail bootstrap — fail-fast prod config, conditional SMTP sender, local Mailpit; Gmail SMTP for the no-domain MVP (SMTP2GO upgrade)

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

- `application-prod.yml` — add `spring.mail.host/port/username/password` bound to required `${SPRING_MAIL_HOST}`/`…PORT`/`…USERNAME`/`…PASSWORD` (no defaults); add `spring.mail.properties.mail.smtp.{auth=true, starttls.enable=true, starttls.required=true, connectiontimeout/timeout/writetimeout=20000}`. The block stays **provider-agnostic** (all values from env vars) — the provider choice below is realized purely as the env-var values on Render.
- `application-local.yml` — add `spring.mail.host: localhost` + `spring.mail.port` (local Mailpit SMTP).
- `docker/services.yml` — add a `mailpit` service (SMTP + `8025` web UI) for local dev, separate from `docker/infra-tests.yml`.
- `SmtpEmailNotificationSender` — drop `@Primary`; add `@ConditionalOnProperty(prefix = "spring.mail", name = "host")`; sole `EmailNotificationSender`.
- Remove `NoOpEmailNotificationSender` (replaces the unconditional NoOp+Smtp coexistence).
- Wiring test `iam.user.infrastructure.notification.ProductionMailBootstrapTest` — a sliced `ApplicationContextRunner` (mirrors `EventResubmitSchedulingTest`; must **not** fork the shared full acceptance context). Loads with the prod mail configuration supplied; asserts `hasNotFailed()`, `hasSingleBean(JavaMailSender.class)`, and `hasSingleBean(EmailNotificationSender.class)` (the Smtp sender is the one wired).

## RED expectation

`hasSingleBean(EmailNotificationSender.class)` fails **today** — `NoOp` and the unconditional `Smtp` sender both register (two beans). After removing `NoOp` and gating `Smtp`, exactly one sender remains → GREEN. This is the legitimate red state for the scenario; the positive happy-path alone (context starts when mail is configured) already passes via autoconfiguration and is not red on its own.

## Provider selection (free SMTP relay)

Render provides no email; an external SMTP relay is required. Constraints: transactional activation emails over `JavaMailSender`/SMTP (provider-agnostic — no code change), **very low volume** (registrations are rare), free tier, **no custom domain** (this is a free-tier MVP on Render + Supabase). In 2026, sending to arbitrary inboxes (Gmail/Yahoo/Microsoft sender rules) effectively requires a domain-authenticated sender (SPF/DKIM) — so domain-requiring providers are deferred until a domain exists.

| Rejected (for the no-domain MVP) | Why |
|----------|-----|
| SMTP2GO | Best free-tier deliverability and most port-flexible, **but blocks signup with free webmail** — requires an email on your own domain to even create the account. Kept as the **upgrade** once a domain exists. |
| Brevo | Gmail signup allowed, but free webmail sender addresses **cannot be authenticated** — domain DNS auth is required to send. Domain-gated. |
| Mailjet | 6,000/mo free; signup with Gmail allowed **and a single sender address can be verified by clicking a link (no domain/DNS)** — the closest no-domain alternative. Runner-up; rejected only because Gmail SMTP needs zero new signup for the MVP. |
| SendGrid / MailerSend / Resend | SendGrid free tier removed; MailerSend free tier shrinking (500/mo); Resend is HTTP-API-first and still needs domain verification to send to others. |

**Chosen (current MVP, no domain): Gmail SMTP.** The account already exists; sending works without a domain. Limits: ~500 recipients/day; Gmail rewrites the `From` to the authenticated account, so the sender identity must be the Gmail address (override `rpm.mail.from-address`). It is a grey area under Google's terms for app sending but is fine for a low-volume MVP smoke test.

**Production env vars on Render** (Gmail; the agnostic `spring.mail.*` block needs no code change):

| Env var | Value |
|---------|-------|
| `SPRING_MAIL_HOST` | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | `587` (STARTTLS) |
| `SPRING_MAIL_USERNAME` | the Gmail address |
| `SPRING_MAIL_PASSWORD` | a Google **App Password** (16 chars; requires 2-Step Verification — the normal password will not work) |
| `RPM_MAIL_FROMADDRESS` | the same Gmail address (Gmail overrides any other `From`) |

**Immediate Render unblock:** setting these env vars fixes the deploy crash *before* the 7.1 TDD cycle lands — Spring relaxed binding maps `SPRING_MAIL_*` to `spring.mail.*` even with no `application-prod.yml` entry, so `MailSenderAutoConfiguration` contributes a `JavaMailSender` and the sender constructs. (Actual *sending* over 587 also needs STARTTLS — added to `application-prod.yml` by 7.1, or temporarily via `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true`.)

**Upgrade path (when a domain is acquired):** switch to **SMTP2GO** (`mail.smtp2go.com:2525`, plain SMTP user/password) for the best free-tier deliverability and port flexibility, or Brevo/Mailjet. A provider swap is a pure env-var change — no code redeploy. With a domain, set `rpm.mail.from-address` to a domain sender (e.g. `no-reply@<domain>`) and add SPF/DKIM for inbox placement.

## Edge Cases

| Case | Behavior |
|------|----------|
| Prod mail env vars unset | `${SPRING_MAIL_HOST}` unresolved → context fails loudly at startup (intended fail-fast). |
| Mail configured (test / local / prod) | `MailSenderAutoConfiguration` contributes `JavaMailSender`; the `@ConditionalOnProperty` Smtp sender contributes as the sole `EmailNotificationSender`. |
| Context with no `spring.mail.host` (slice/unit) | Smtp sender absent (conditional) — only matters where the sender is injected; the full context that hosts the listener always has Mailpit. |
