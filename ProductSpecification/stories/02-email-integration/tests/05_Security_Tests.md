# Security Tests — Email Integration

> **Implementation Order**: One story-relevant scenario — user-controlled text rendered into the email template must be escaped (no template/HTML injection).

Most OWASP categories do not apply to this story: it adds no new HTTP endpoint or user-facing input surface, the recipient is a pre-validated `EmailAddress` value object (no header-injection CRLF), the subject and from-address are server-side constants/config, and the activation JWT is generated and validated by Story 1. Generic concerns (auth, CSRF, transport, SQL injection) are unchanged and tested elsewhere.

---

## 5. Email Template Injection

> **Implementation note:** escaping is rendered-content verification — realize it as a **focused fast rendering test** (render the template with fixed inputs, assert the produced string), NOT as an e2e/Mailpit test. See the "rendered-content verification" rule in `tdd-rules.md`.

### 5.1 User-controlled login is escaped in the rendered email

**Given** a login value containing HTML/template markup
**When** the activation email template is rendered with that login
**Then** the markup appears escaped (inert text) in the rendered body
**And** no markup is interpreted as HTML or as a template expression

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a login value containing HTML/template markup` | A login with payloads such as `<script>...</script>` and a Thymeleaf/SpEL fragment like `[[${...}]]` / `${...}` |
| `the activation email template is rendered with that login` | Render the Thymeleaf template directly via the renderer/message builder with fixed inputs — no SMTP, no Mailpit |
| `the markup appears escaped (inert text) in the rendered body` | Assert the rendered HTML contains the HTML-entity-escaped form of the payload (e.g., `&lt;script&gt;`), not the raw tags — Thymeleaf escapes by default |
| `no markup is interpreted as HTML or as a template expression` | Assert the raw `<script>` tag and the unevaluated template-expression delimiters do not appear as live markup in the rendered body |
