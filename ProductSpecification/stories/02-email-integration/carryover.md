# Story 2 — Carryover

Enduring codebase quirks and decisions promoted from completed scenarios. Read on resume; verify against current code before relying on them.

## Quirk: spring-modulith-moments enables scheduling app-wide
**Quirk:** `spring-modulith-moments`' `MomentsAutoConfiguration` carries `@EnableScheduling`, a hidden app-wide scheduling source — it defeats any `@ConditionalOnProperty` gate on your own scheduling config until excluded.
**Where:** `spring-modulith-moments` excluded from `spring-modulith-starter-core` in `pom.xml`; `SchedulingConfiguration`.
**From:** scenario 8.1 (8-1-scheduler-wiring)

## Quirk: prod context requires SPRING_MAIL_* or it fails to start
**Quirk:** The mail sender is the sole `EmailNotificationSender`, `@ConditionalOnProperty(spring.mail.host)`; prod must bind `SPRING_MAIL_*` env vars or the bean can't construct and the context fails to start (it passes under `test` because Mailpit supplies the bindings).
**Where:** `SmtpEmailNotificationSender`; `application-prod.yml`; see ADR production-mail-bootstrap.
**From:** scenario 7.1 infrastructure (7-1-prod-mail-bootstrap)

## Quirk: failed SMTP send does not fail registration
**Quirk:** The async `@ApplicationModuleListener` swallows a send exception, so registration stays 201 and the Modulith publication is left incomplete for the resubmit scheduler to redeliver within 24h.
**Where:** `UserRegisteredEventListener` → `SmtpEmailNotificationSender`.
**From:** scenario 4.1 infrastructure (4-1-smtp-unavailable)
