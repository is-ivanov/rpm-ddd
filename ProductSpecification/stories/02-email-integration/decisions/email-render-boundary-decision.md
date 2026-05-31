# Decision: Activation-email rendering lives in the notification adapter

**Date**: 2026-05-30 **Scenarios**: 1.1

Where to render the activation email's HTML/text: inside the `EmailNotificationSender` implementation, or in `UserRegisteredEventListener` before calling the port.

| Rejected | Why |
|----------|-----|
| Render in the listener, pass a rendered `EmailMessage` to a generic `EmailSender.send(msg)` transport port | Changes the existing port contract (`sendActivationToken` is reused as-is per the interview), puts presentation/template logic into an infrastructure event handler, and is premature with a single email type (YAGNI). |
| Inline rendering inside `SmtpEmailNotificationSender` (no separate renderer) | Couples content rendering to SMTP I/O; the rendered-content approval test would need a mock `JavaMailSender` + `MimeMessage` capture instead of asserting renderer output directly. |

**Chosen**: The port stays `EmailNotificationSender.sendActivationToken(toEmail, login, activationToken)` (unchanged). The SMTP adapter renders via an injected `ActivationEmailRenderer` collaborator, then sends. Rendered content (`subject` + `html` + `text`) is plain data, so any future provider adapter (Brevo/SendGrid/Mailtrap REST) reuses the **same** `ActivationEmailRenderer` — reuse-across-implementations is satisfied without exposing rendered content through the port. Extracting a generic transport port (the rejected Option B) becomes worthwhile only when a second email type appears.

## Model

- Port `EmailNotificationSender` — unchanged: `void sendActivationToken(String toEmail, String login, String activationToken)`.
- `SmtpEmailNotificationSender implements EmailNotificationSender` (`@Primary`, `@InfrastructureComponent`) — builds the activation link `{app.frontend-base-url}/activate?token={token}`, calls the renderer, assembles a multipart (HTML + text alternative) `MimeMessage` via `MimeMessageHelper`, sends via `JavaMailSender`.
- `ActivationEmailRenderer` — renders Thymeleaf `templates/email/activation.html` + `activation.txt` from `(login, activationLink)` into `ActivationEmailContent(subject, htmlBody, textBody)`. Verified by a fast **approval test** with deterministic inputs and checked-in fixtures (not pinned into the e2e test).
- `EmailProperties` (`@ConfigurationProperties("app.mail")`) — from display-name + from-address; `app.frontend-base-url` for the link.
- `NoOpEmailNotificationSender` — demoted (remove `@Primary`; keep behind a dev profile or delete) so exactly one primary `EmailNotificationSender` remains.
- Listener `UserRegisteredEventListener` — untouched.

## Test layering

- Level 1 acceptance (Mailpit): delivery + recipient + from + subject + link-present.
- `red/green-adapter email`: the renderer **approval test** (deterministic fixtures) is the meaningful adapter test; SMTP-send mechanics ride on Level 1.
- `red/green-usecase`: **`[S]`** — the listener→port flow is reused unchanged from Story 1; zero production files in the usecase/application layer change.
