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

## 8. Scheduler Wiring

> Scenarios 6.1/7.1 exercise resubmit **logic** by invoking the job directly. This scenario closes the
> gap left by that shortcut: it proves the job is actually **wired to fire on its own** in a running
> application (`@Scheduled` + `@EnableScheduling`). Without it the scheduler silently never runs in
> production. Multi-instance safety (`@SchedulerLock`/ShedLock, since the backend runs as multiple
> instances) is a design concern for this scenario — resolve it in the `design` step.

### 8.1 The resubmit scheduler runs automatically on its configured schedule

**Given** an incomplete activation-email publication younger than 24 hours (the first SMTP send failed)
**And** no code invokes the resubmit job directly
**When** the configured scheduler interval elapses
**Then** the activation email is delivered automatically to the registered recipient
**And** the publication is marked complete in the registry

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
| `no code invokes the resubmit job directly` | The test MUST NOT call `resubmitJob.resubmit()`; delivery can only occur if `@Scheduled` + `@EnableScheduling` are wired and firing |
| `the configured scheduler interval elapses` | Await up to a bounded window (longer than the fixed rate) for an automatic `@Scheduled` tick; the rate should come from a property (e.g. `fixedRateString = "${...}"`) so the `test` profile can shorten it for a fast, non-flaky run |
| `the activation email is delivered automatically to the registered recipient` | Poll Mailpit `awaitMessage()` until the message appears — proving the scheduled tick (not a direct call) resubmitted the incomplete publication |
| `the publication is marked complete in the registry` | After automatic delivery, the Spring Modulith JDBC registry holds no incomplete publication for the listener |
