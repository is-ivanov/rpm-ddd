# Security Tests (Extended) — Email Integration

> These are additional edge case tests. Implement after core tests pass.

---

## 6. Email Header Injection

### 6.1 CRLF in the recipient address cannot inject extra headers

**Given** a registration attempt whose email contains CRLF and an injected header (e.g. `\r\nBcc: attacker@evil.com`)
**When** the email is validated and the activation email would be sent
**Then** the malicious address is rejected at the `EmailAddress` value-object boundary
**And** no email with an injected `Bcc`/`Cc` header is delivered

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a registration attempt whose email contains CRLF and an injected header` | Registration input whose email field contains `\r\n`-separated header text |
| `the malicious address is rejected at the EmailAddress value-object boundary` | `EmailAddress` construction throws a domain validation exception; registration never reaches the send step |
| `no email with an injected Bcc/Cc header is delivered` | Poll Mailpit for a bounded window; assert no message exists, and no message carries an injected `Bcc`/`Cc` recipient |
