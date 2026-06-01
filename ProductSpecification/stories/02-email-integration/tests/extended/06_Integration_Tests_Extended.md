# Integration Tests (Extended) — Email Integration

> These are additional edge case tests. Implement after core tests pass.

---

## 9. Repeated Resubmit Until Success

### 9.1 The scheduler keeps resubmitting across ticks until delivery succeeds

**Given** an incomplete activation-email publication younger than 24 hours
**And** the SMTP server fails the first resubmit attempt and succeeds on a later one
**When** the resubmit scheduler runs across multiple ticks
**Then** the activation email is eventually delivered exactly once
**And** the publication is then marked complete

---

## 10. Independent Processing of Multiple Publications

### 10.1 Multiple incomplete publications are resubmitted independently

**Given** two incomplete activation-email publications younger than 24 hours for two different recipients
**When** the resubmit scheduler runs
**Then** each recipient receives exactly one activation email
**And** both publications are marked complete

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the SMTP server fails the first resubmit attempt and succeeds on a later one` | Toggle Mailpit/SMTP availability between scheduler ticks (down for the first tick, up for a later one) |
| `the resubmit scheduler runs across multiple ticks` | Await multiple `@Scheduled(fixedRate=5s)` ticks, or invoke the scheduled method repeatedly |
| `the activation email is eventually delivered exactly once` | Poll `awaitMessage()` for the recipient; assert the final delivered count is exactly one |
| `two incomplete activation-email publications ... for two different recipients` | Register two users while SMTP is unavailable so both publications stay incomplete |
| `each recipient receives exactly one activation email` | Poll Mailpit per recipient; assert exactly one message per recipient |
| `both publications are marked complete` | The Modulith registry holds no incomplete publications for the listener after delivery |
