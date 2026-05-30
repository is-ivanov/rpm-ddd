# API Tests — Email Integration

> **Implementation Order**: Single end-to-end happy path — registering a user results in a delivered activation email. Resubmit, cutoff, and resilience detail live in the Integration and Infrastructure suites.

---

## 1. Activation Email Delivery

> **Implementation note:** email delivery is an additional consequence of the existing registration action. Per the "one action, assert all consequences" rule (`tdd-rules.md`), **extend the existing `UserRegistrationIntegrationTest`** with the email `then` assertions (tag it `@MailTest`) — do not create a parallel acceptance class for the same `register` action.

### 1.1 Registering a user delivers an activation email

**Given** a valid registration request for a new user
**When** the user is registered
**Then** an activation email is delivered to the registered email address
**And** the email from-address is `RPM Platform <no-reply@rpm-platform.com>`
**And** the email subject is `Activate your RPM account`
**And** the email body contains an activation link to the frontend activation page carrying the activation token

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a valid registration request for a new user` | Registration input with a unique login and a valid email address |
| `the user is registered` | The registration flow runs, persists the PENDING user, and publishes `UserRegisteredEvent` |
| `an activation email is delivered to the registered email address` | Poll Mailpit (`MailpitClient` AssertJ `awaitMessage()`) for a message whose recipient equals the registered email; never sleep — the send is async |
| `the email from-address is RPM Platform <no-reply@rpm-platform.com>` | Assert the delivered message `From` header equals the configured `app.mail.from` value exactly |
| `the email subject is Activate your RPM account` | Assert the delivered message `Subject` equals the constant exactly |
| `the email body contains an activation link to the frontend activation page` | Assert the HTML body contains `{app.frontend-base-url}/activate?token=` followed by the JWT activation token |
