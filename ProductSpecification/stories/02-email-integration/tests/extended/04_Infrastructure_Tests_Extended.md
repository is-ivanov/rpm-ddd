# Infrastructure Tests (Extended) — Email Integration

> These are additional edge case tests. Implement after core tests pass.

---

## 6. SMTP Timeout Bounding

### 6.1 A slow SMTP handshake is bounded by the configured timeout

**Given** an SMTP server that accepts the connection but never completes the handshake
**When** a user is registered
**Then** the send attempt aborts within the configured SMTP timeout
**And** the registration still succeeds
**And** the event publication remains incomplete for later resubmit

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `an SMTP server that accepts the connection but never completes the handshake` | A stub/sink SMTP endpoint that stalls after connect (simulates the reverse-DNS / EHLO stall) |
| `the send attempt aborts within the configured SMTP timeout` | The send fails within `mail.smtp.connectiontimeout`/`timeout`/`writetimeout` (5000ms) rather than hanging — verify `mail.smtp.localhost` is pinned |
| `the registration still succeeds` | Registration returns its normal success response; the async failure does not propagate to the HTTP response |
| `the event publication remains incomplete for later resubmit` | The Modulith registry holds an incomplete publication for the listener |
