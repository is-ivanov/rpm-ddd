# Session Handoff — Story 2 (Email integration) — 2026-06-01

Resume point for tomorrow. Branch `story/2-email-integration`, worktree `email-notification`.

## TL;DR — where we stopped

Mid **`green-adapter scheduling`** (Story 2, Integration Scenario **8.1** — resubmit scheduler wiring). The green-agent implemented the ADR but hit a **verified ADR flaw** and stopped. **A design decision (A vs B) is pending — answer it first tomorrow, then implement.** The green WIP is preserved in a git **stash** (see below); HEAD is green.

## The blocking decision (answer this first)

**ADR `resubmit-scheduling-wiring` premise is FALSE in this codebase.** It assumed that under the `test` profile, `rpm.scheduler.enabled=false` excludes `SchedulingConfiguration` → no `@EnableScheduling` → the job's `@Scheduled` is inert.

**Reality (verified by trace logs):** Spring Modulith's `org.springframework.modulith.events.config.StalenessMonitorConfiguration` is an `@AutoConfiguration` implementing `SchedulingConfigurer`, which installs the `ScheduledAnnotationBeanPostProcessor` **app-wide, independent of our config**. So `@Scheduled` on `ResubmitIncompletePublicationsJob` **still fires every 5s under the `test` profile** even though `SchedulingConfiguration` is excluded. The firing repeatedly resubmits, exhausts the SMTP spy, and breaks **Scenario 5.1** (SMTP-recovery). 6.1/7.1 tolerate it; 5.1 does not. On clean HEAD all three pass.

**Fix principle** (`tdd-rules.md` "gate each job independently"): `@Scheduled` must live on a **conditionally-created** bean, not on the always-present, always-injectable job.

### Option B (RECOMMENDED) — trigger on `SchedulingConfiguration`
- `ResubmitIncompletePublicationsJob` → ungated `@InfrastructureComponent`, **logic only**, NO `@Scheduled`/`@SchedulerLock`. Stays injectable so 6.1/7.1/5.1's direct `resubmit()` calls run real logic.
- Add a `@Scheduled(fixedDelayString="${rpm.events.resubmit.interval}")` + `@SchedulerLock(name="resubmitIncompletePublications", lockAtMostFor="PT30S", lockAtLeastFor="PT0S")` **method on the gated `SchedulingConfiguration`** that injects the job and calls `resubmitJob.resubmit()`.
- Under `test` (`rpm.scheduler.enabled=false`): `SchedulingConfiguration` excluded → no trigger bean → **no firing**. Job bean present → direct calls work.
- **Keeps the user's `rpm.scheduler.enabled` flag.** **The frozen wiring test `EventResubmitSchedulingTest` is UNCHANGED and stays valid** — it loads `SchedulingConfiguration`, so the `@Scheduled` placeholder + interval are still verified. No red reopening.

### Option A — separate per-job trigger bean
- Separate `@Scheduled` trigger bean gated by `@ConditionalOnProperty("rpm.events.resubmit.enabled", matchIfMissing=true)`, `false` in `application-test.yml`. Job ungated/logic-only.
- More granular (disable one job), closer to tdd-rules ideal. **BUT** the frozen wiring test does NOT load this separate bean → it wouldn't verify the trigger → must edit the wiring test → **brief reopen of red-adapter scheduling**.

**My recommendation: B** (no red reopening, keeps the chosen flag, wiring test stays valid). The user paused before answering — ask again tomorrow.

## After the decision — implementation steps (Option B)

1. Amend ADR `decisions/resubmit-scheduling-wiring-decision.md`: add the Modulith `StalenessMonitorConfiguration` finding; move `@Scheduled`/`@SchedulerLock` from the job onto a delegating method on the gated `SchedulingConfiguration`; job becomes ungated logic-only. Commit ADR amendment.
2. Re-run / finish `green-adapter scheduling` (recover the stash or re-dispatch green-agent with the amended ADR). The stash already has ~90% correct code — only the `@Scheduled` placement (job → config) needs to change.
3. Verify: wiring test GREEN **and** 6.1 (`ExactlyOnceEmailDeliveryIntegrationTest`) + 7.1 (`StaleIncompletePublicationIntegrationTest`) + 5.1 (`SmtpRecoveryEmailDeliveryIntegrationTest`) all GREEN.
4. `/refactor` (scheduling layer) → `/test-coverage` (note: config classes like `ClockConfiguration` are jacoco-excluded in `pom.xml`; `SchedulingConfiguration` may warrant the same) → checkstyle+PMD → commit `green-adapter scheduling`.
5. Then §8.1 is done. Next: Infra §7.1 (prod-mail bootstrap) — its `design` routes through `/architecture` for sender selection + **local-dev mail decision** (docker SMTP vs `local`-profile file-writing `JavaMailSender` vs `NoOp`).

## Git state

- **HEAD**: `f2dbec4` red: scheduler wiring test (disabled) — **green build** (wiring test `@Disabled`, ShedLock deps inert).
- **Stash** (created this handoff): green-adapter WIP per the *flawed* ADR (fails 5.1). Recover with `git stash pop` (or `git stash show -p stash@{0}`). Contents:
  - `SchedulingConfiguration.java` — `@EnableScheduling` + `@EnableSchedulerLock(PT30S)` + `@ConditionalOnProperty(rpm.scheduler.enabled, matchIfMissing=true)` + `@EnableConfigurationProperties` + `@Bean LockProvider` (correct, keep).
  - `EventResubmitProperties.java` — `@ConfigurationProperties("rpm.events.resubmit")` + `@Validated`, `@NotNull Duration interval` (correct, keep).
  - `ResubmitIncompletePublicationsJob.java` — has `@Scheduled`+`@SchedulerLock` ← **THIS is what must move to `SchedulingConfiguration` for Option B**.
  - `application.yml` — `rpm.events.resubmit.interval: 5s` (REQUIRED — without it context fails; keep).
  - `application-test.yml` — `rpm.scheduler.enabled: false` (keep for B).
  - `changelog-cumulative.xml` + new `2026.06.01-01-changelog-shedlock.xml` — shedlock table (correct, keep).
  - `EventResubmitSchedulingTest.java` — `@Disabled` removed (re-remove when green).
- **ShedLock**: version **7.7.0** in `pom.xml` (already committed at f2dbec4). Compatibility with Spring Boot 4.x / Spring 7.0 / JVM 17+ verified via the ShedLock compat matrix.

## Story 2 progress (6/8 = 75%)

Done: 1.1, 6.1, 7.1, Security 5.1, Infra 4.1 `[S]`, Infra 5.1. In progress: **Integration 8.1** (scheduler wiring — at `green-adapter scheduling`, blocked above). Pending: **Infra 7.1** (prod-mail bootstrap — context must start in prod with `spring.mail.*`; the original production gap that prompted reopening).

## This session's arc (what changed)

1. Diagnosed three production gaps the ATDD process missed: (a) resubmit job never `@Scheduled` in prod; (b) no prod `spring.mail.*` → no `JavaMailSender`; (c) → context wouldn't start on Render.
2. Reopened Story 2 (Done → In Progress); added Integration §8.1 (scheduler wiring) + Infra §7.1 (prod-mail bootstrap).
3. Hardened the process docs so scheduled-job wiring can't be skipped again: `tdd-rules.md` "Scheduled / Recurring Jobs", `workflow.md` mandate, rewrote `.claude/tech/java-spring/templates/scheduling/*`. prompt-refactor pass applied.
4. ADR `resubmit-scheduling-wiring` (fixedDelay + required interval property + ShedLock); user chose `rpm.scheduler.enabled` flag + fixedDelay + ShedLock.
5. red-adapter scheduling: `EventResubmitSchedulingTest` (ApplicationContextRunner wiring test) committed disabled at `f2dbec4`.
6. green-adapter scheduling: implemented, hit the Modulith firing bug → **stopped here**.
