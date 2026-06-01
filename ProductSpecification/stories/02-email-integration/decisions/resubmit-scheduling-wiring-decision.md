# Decision: Resubmit scheduler wiring — fixedDelay, app-wide gated scheduling, ShedLock

**Date**: 2026-06-01 **Scenarios**: 8.1

`ResubmitIncompletePublicationsJob.resubmit()` has no `@Scheduled` and the app has no `@EnableScheduling`, so the job never fires in production; wiring it must also keep the existing direct-invocation tests (6.1/7.1/5.1) working and stay safe across multiple instances.

| Rejected | Why |
|----------|-----|
| Gate the **job bean** with `@ConditionalOnProperty(rpm.events.resubmit.enabled)` | Under the `test` profile the bean would be absent, but `EmailStatements.whenResubmitSchedulerRuns()` injects `ResubmitIncompletePublicationsJob` to call `resubmit()` directly — 6.1/7.1/5.1 would fail to wire. Gating must stop auto-firing, not remove the bean. |
| `@EnableScheduling` on the job's own config / under the resubmit flag | `@EnableScheduling` enables *every* `@Scheduled` method app-wide — it is a general concern, not job-specific. |
| `fixedRate` | A slow run can pile up against the next tick; `fixedDelay` (next run N after the previous completes) is safer for a retry poll. |
| Hardcoded interval literal | A missing schedule must fail context startup loudly — bind it to a required property (`tdd-rules.md` "Scheduled / Recurring Jobs"). |
| Postgres advisory lock | Zero-dependency and SB4-safe, but hand-rolled; kept as the **fallback** only if ShedLock proves incompatible with Spring Framework 7. |

**Chosen**: An app-wide `SchedulingConfiguration` owns `@EnableScheduling` + `@EnableSchedulerLock` + the `LockProvider`, gated by a general `@ConditionalOnProperty(name = "rpm.scheduler.enabled", matchIfMissing = true)`; `application-test.yml` sets `rpm.scheduler.enabled=false`, so no `@Scheduled` fires in the full-context tests while the job bean (a plain `@InfrastructureComponent`) stays injectable for their direct `resubmit()` calls — and the ShedLock aspect, being inactive in tests, makes `@SchedulerLock` a no-op there. The job gains `@Scheduled(fixedDelayString = "${rpm.events.resubmit.interval}")` + `@SchedulerLock`. Multi-instance safety via ShedLock (`shedlock-spring` + `shedlock-provider-jdbc-template`).

## Model

- `shared.infrastructure.scheduling.SchedulingConfiguration` — `@Configuration`, `@EnableScheduling`, `@EnableSchedulerLock`, `@ConditionalOnProperty(name = "rpm.scheduler.enabled", matchIfMissing = true)`; `@Bean LockProvider` = `JdbcTemplateLockProvider(dataSource)`.
- `shared.infrastructure.events.EventResubmitProperties` — `@ConfigurationProperties("rpm.events.resubmit")`, `@Validated`, record `{ @NotNull Duration interval }`. Age-cutoff stays the inline 24h constant (per `resubmit-job-placement-decision`).
- `ResubmitIncompletePublicationsJob.resubmit()` — gains `@Scheduled(fixedDelayString = "${rpm.events.resubmit.interval}")` + `@SchedulerLock(name = "resubmitIncompletePublications", lockAtMostFor = "PT30S", lockAtLeastFor = "PT0S")`. Body unchanged (still reads the inline cutoff and the injected `Clock`).
- `application.yml` — `rpm.events.resubmit.interval: 5s` (required; `rpm.scheduler.enabled` defaults true via `matchIfMissing`).
- `application-test.yml` — `rpm.scheduler.enabled: false`.
- `pom.xml` — add `net.javacrumbs.shedlock:shedlock-spring` + `shedlock-provider-jdbc-template` (version property). **Green must verify Spring Framework 7 / Spring Boot 4 compatibility**; if incompatible → advisory-lock fallback.
- Liquibase — new changeset (`shedlock` table) included via `v.2026.1/changelog-cumulative.xml` (prod). The full-context tests don't need it (scheduler off); the wiring test provisions its own shedlock table in a throwaway `DataSource`.
- Wiring test (`ApplicationContextRunner`, base `application.yml`, no profile) — loads `SchedulingConfiguration` + job + mocked `IncompleteEventPublications`/`Clock` + throwaway `DataSource`; asserts `hasNotFailed()` (interval placeholder resolved), `LockProvider` present, `EventResubmitProperties.interval()` == 5s.

## Edge Cases

| Case | Behavior |
|------|----------|
| `rpm.events.resubmit.interval` missing/blank | `fixedDelayString` placeholder fails to resolve → context startup fails loudly (intended). |
| `rpm.scheduler.enabled=false` (tests) | `SchedulingConfiguration` absent → no `@EnableScheduling`, no `LockProvider`; `@SchedulerLock` aspect inactive → direct `resubmit()` runs the real logic. |
| Two instances tick simultaneously | `@SchedulerLock` (`lockAtMostFor=PT30S`) lets only one acquire the row lock and run; the other skips. |
| Lock holder crashes mid-run | `lockAtMostFor` releases the lock after 30s so a later tick resumes. |
| ShedLock incompatible with Spring 7 | Fall back to `pg_try_advisory_lock` in the job; drop the ShedLock deps + table. |
