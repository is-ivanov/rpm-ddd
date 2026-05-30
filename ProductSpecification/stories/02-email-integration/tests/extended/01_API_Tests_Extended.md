# API Tests (Extended) — Email Integration

> These are additional edge case tests. Implement after core tests pass.

---

## 1. Multipart Structure

### 1.1 Activation email includes a plain-text alternative

**Given** a valid registration request for a new user
**When** the activation email is delivered
**Then** the message is multipart with an HTML part and a plain-text alternative
**And** the plain-text alternative contains the activation link

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the message is multipart with an HTML part and a plain-text alternative` | Assert the delivered message content type is `multipart/alternative` with both `text/html` and `text/plain` parts |
| `the plain-text alternative contains the activation link` | Assert the `text/plain` part contains `{app.frontend-base-url}/activate?token=` followed by the token |
