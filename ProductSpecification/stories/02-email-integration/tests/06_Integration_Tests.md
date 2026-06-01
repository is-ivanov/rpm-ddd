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
> gap that shortcut leaves: it proves the job is actually **wired and scheduled** in production. Verified
> as a fast `ApplicationContextRunner` wiring test under the production config — NOT by awaiting a real
> tick (slow/flaky). Decisions in ADR `resubmit-scheduling-wiring`: the interval is a **required property**
> bound to `@Scheduled(fixedDelayString = "${rpm.events.resubmit.interval}")`; `@EnableScheduling` lives on
> an **app-wide** `SchedulingConfiguration` gated by `rpm.scheduler.enabled` (default true), set `false` in
> the `test` profile so the scheduler never auto-fires — while the job bean (a plain `@InfrastructureComponent`)
> stays injectable, so 6.1/7.1 keep calling `resubmit()` directly; multi-instance safety via **ShedLock**
> (`@SchedulerLock` + `LockProvider`, adds the dependency + a `shedlock` migration; green verifies SB4 compat).

### 8.1 The resubmit scheduler is wired, scheduled, and lock-guarded in production

**Given** the production scheduling configuration is loaded
**When** the application context is bootstrapped
**Then** the context starts successfully — `@EnableScheduling` is active and `@Scheduled` resolved its required resubmit-interval property
**And** the configured resubmit interval equals the intended value
**And** a distributed lock provider is configured so only one instance resubmits per tick
**And** under the `test` profile the job's automatic firing is disabled (resubmit logic stays covered by 6.1/7.1 via direct invocation)

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
| `the production scheduling configuration is loaded` | An `ApplicationContextRunner` (or focused `@SpringBootTest(classes=…)`) boots the scheduling config under `spring.profiles.active=prod` — a sliced/partial context that does not fork the shared full acceptance context |
| `the context starts successfully — @EnableScheduling is active and @Scheduled resolved its required resubmit-interval property` | `then(context).hasNotFailed()`; a missing/blank `rpm.events.resubmit.interval` would fail placeholder resolution at startup. `@EnableScheduling` lives on an application-wide config, not on the job |
| `the configured resubmit interval equals the intended value` | `context.getBean(EventResubmitProperties.class).interval()` equals the configured duration (e.g. `Duration.ofSeconds(5)`) |
| `a distributed lock provider is configured so only one instance resubmits per tick` | `then(context).hasSingleBean(LockProvider.class)` — ShedLock (`@SchedulerLock` on the job, `@EnableSchedulerLock` + JDBC `LockProvider`, `shedlock` Liquibase table) |
| `under the test profile the job's automatic firing is disabled` | `application-test.yml` sets `rpm.scheduler.enabled=false`; the gated `SchedulingConfiguration` is absent (no `@EnableScheduling`, no `LockProvider`), so no `@Scheduled` fires and the `@SchedulerLock` aspect is inactive — but the job bean stays present so the direct-invocation logic tests (6.1/7.1/5.1) keep working |
