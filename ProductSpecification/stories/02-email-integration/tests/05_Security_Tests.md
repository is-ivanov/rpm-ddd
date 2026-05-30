# Security Tests — Email Integration

> **Implementation Order**: One story-relevant scenario — user-controlled text rendered into the email template must be escaped (no template/HTML injection).

Most OWASP categories do not apply to this story: it adds no new HTTP endpoint or user-facing input surface, the recipient is a pre-validated `EmailAddress` value object (no header-injection CRLF), the subject and from-address are server-side constants/config, and the activation JWT is generated and validated by Story 1. Generic concerns (auth, CSRF, transport, SQL injection) are unchanged and tested elsewhere.

---

## 5. Email Template Injection

### 5.1 User-controlled login is escaped in the rendered email

**Given** a registration request whose login contains HTML/template markup
**When** the activation email is rendered and delivered
**Then** the markup appears escaped (inert text) in the email body
**And** no markup is interpreted as HTML or as a template expression

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a registration request whose login contains HTML/template markup` | Register a user whose login contains payloads such as `<script>...</script>` and a Thymeleaf/SpEL fragment like `[[${...}]]` / `${...}` |
| `the activation email is rendered and delivered` | The async send renders the Thymeleaf template and delivers via Mailpit; poll with `awaitMessage()` for the recipient |
| `the markup appears escaped (inert text) in the email body` | Assert the HTML body contains the HTML-entity-escaped form of the payload (e.g., `&lt;script&gt;`), not the raw tags — Thymeleaf escapes by default |
| `no markup is interpreted as HTML or as a template expression` | Assert the raw `<script>` tag and the unevaluated template-expression delimiters do not appear as live markup in the delivered body |
