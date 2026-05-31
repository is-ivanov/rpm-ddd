# Infrastructure Tests — Email Integration

> **Implementation Order**: SMTP unavailable does not break registration → recovery delivers the pending email after the SMTP server returns.

---

## 4. SMTP Unavailable During Registration

### 4.1 SMTP unavailable does not fail registration

**Given** the SMTP server is unavailable
**When** a user is registered
**Then** the registration succeeds
**And** no activation email is delivered yet
**And** the event publication for the activation email remains incomplete

---

## 5. SMTP Recovery

### 5.1 Activation email is delivered after SMTP recovers

**Given** the SMTP server was unavailable when a user registered
**And** the activation email publication remains incomplete
**When** the SMTP server becomes available
**Then** the activation email is delivered to the registered email address without re-registering the user

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the SMTP server is unavailable` | Point `spring.mail.*` at an unreachable host/port, or stop the Mailpit container; verify the SMTP port refuses connections |
| `a user is registered` | The registration flow runs, persists the PENDING user, and publishes `UserRegisteredEvent` |
| `the registration succeeds` | Registration returns its normal success response — the async email send never propagates an exception to the HTTP response |
| `no activation email is delivered yet` | Poll Mailpit for a short bounded window and assert no message exists for the registered recipient |
| `the event publication for the activation email remains incomplete` | The Spring Modulith JDBC event publication registry holds an incomplete publication for the `UserRegisteredEvent` listener |
| `the SMTP server becomes available` | Restore `spring.mail.*` to the live Mailpit instance / restart the Mailpit container; verify the SMTP port accepts connections |
| `the activation email is delivered ... without re-registering the user` | The resubmit scheduler reprocesses the incomplete publication; poll Mailpit (`awaitMessage()`) until the message for the registered recipient appears |
