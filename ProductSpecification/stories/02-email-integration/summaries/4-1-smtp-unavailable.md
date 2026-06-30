# Scenario 4.1 (Infrastructure) — SMTP unavailable does not fail registration

## red-acceptance (2026-06-01)

**Quirk:** A failed SMTP send never fails registration — the async `@ApplicationModuleListener` swallows the send exception, so the HTTP response stays 201 and the Modulith publication is left incomplete for resubmit.
**Where:** `UserRegisteredEventListener` (async `@ApplicationModuleListener`) → `SmtpEmailNotificationSender`.
**Implication:** Resilience to SMTP outages is structural; recovery relies on the resubmit scheduler redelivering incomplete publications within 24h.
