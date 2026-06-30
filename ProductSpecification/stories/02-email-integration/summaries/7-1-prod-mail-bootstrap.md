# Scenario 7.1 (Infrastructure) — Context starts with the production mail configuration

## red-acceptance (2026-06-02)

**Surprise:** The app started fine under `test` (Mailpit supplies `JavaMailSender`) but failed to start on Render — `prod` had no `spring.mail.*`, so the unconditional `@Primary SmtpEmailNotificationSender` could not construct.
**Why:** Only the `test` profile bound mail properties; the sole sender bean had no conditional guard.
**Impact:** Prod requires `SPRING_MAIL_*` env bindings; the sender is now `@ConditionalOnProperty(spring.mail.host)` and the sole `EmailNotificationSender` (NoOp removed) — see ADR production-mail-bootstrap.
