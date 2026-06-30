# Scenario 8.1 — Resubmit scheduler is wired, scheduled, and lock-guarded in production

## design (2026-06-01)

**Mistake:** Scenarios 6.1/7.1 invoked `resubmitJob.resubmit()` directly, treating the job as done.
**Why wrong:** Direct invocation never exercises the `@Scheduled` wiring, so the job was missing from production and never fired on the deployed app.
**Correct location/approach:** Add a fast `ApplicationContextRunner` wiring test (Scenario 8.1) asserting the schedule + lock beans exist — wiring is a separate concern from job logic.

## green-adapter scheduling (2026-06-02)

**Quirk:** `spring-modulith-moments`' `MomentsAutoConfiguration` carries `@EnableScheduling`, a hidden app-wide scheduling source that defeated the `rpm.scheduler.enabled` gate — and it was NOT the staleness monitor, the first suspect.
**Where:** excluded `spring-modulith-moments` from `spring-modulith-starter-core` in `pom.xml`; `SchedulingConfiguration`.
**Implication:** To truly gate scheduling, hunt down every starter-provided `@EnableScheduling` — gating your own config is not enough.
