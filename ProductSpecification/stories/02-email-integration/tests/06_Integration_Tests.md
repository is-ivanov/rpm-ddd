# Integration Tests — Email Integration

> **Implementation Order**: Exactly-once delivery on success → age cutoff stops resubmitting stale publications.

These scenarios cover the Spring Modulith persisted-event + resubmit-scheduler mechanics. The failure-then-recovery delivery path is covered by the Infrastructure suite (SMTP outage + recovery).

---

## 6. Exactly-Once Delivery

### 6.1 A successful registration delivers exactly one activation email

**Given** a user is registered and the activation email is delivered successfully
**When** the resubmit scheduler runs
**Then** no additional activation email is delivered for that registration
**And** the event publication is marked complete in the registry

---

## 7. Resubmit Age Cutoff

### 7.1 Incomplete publications older than 24 hours are not resubmitted

**Given** an incomplete activation-email publication older than 24 hours
**When** the resubmit scheduler runs
**Then** the stale publication is not resubmitted
**And** no activation email is delivered for it

---

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `a user is registered and the activation email is delivered successfully` | Registration publishes `UserRegisteredEvent`; the listener sends via SMTP to Mailpit; poll `awaitMessage()` to confirm delivery |
| `the resubmit scheduler runs` | The `@Scheduled(fixedRate=5s)` resubmit job executes (await one tick, or invoke the scheduled method directly) |
| `no additional activation email is delivered for that registration` | After the scheduler tick, assert Mailpit still holds exactly one message for the recipient (count unchanged) |
| `the event publication is marked complete in the registry` | The Spring Modulith JDBC event publication registry has no incomplete publication for the listener |
| `an incomplete activation-email publication older than 24 hours` | An incomplete publication whose registry timestamp is more than 24h in the past, using a test clock — never mutate registry rows directly to fake the timestamp |
| `the stale publication is not resubmitted` | The scheduler filters via `IncompleteEventPublications` younger than 24h; assert the stale publication is excluded and stays incomplete |
| `no activation email is delivered for it` | Poll Mailpit for a bounded window and assert no message appears for that recipient |
