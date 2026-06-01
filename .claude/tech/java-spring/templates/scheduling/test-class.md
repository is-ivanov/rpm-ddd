# Scheduling Test Template (Spring Boot · modular monolith)

A scheduled job needs **two** kinds of coverage. Keep them separate:

| Concern | How to test | Where |
|---------|-------------|-------|
| **Logic** — what the job does each run | Call the method directly (deterministic); assert side effects. Auto-firing is disabled under the `test` profile so the scheduler never races the test. | Integration/usecase scenarios (e.g. resubmit 6.1/7.1) |
| **Wiring** — that it is actually scheduled in production | Fast `ApplicationContextRunner` test under the production configuration: assert the context starts (schedule resolved + `@EnableScheduling` active + `LockProvider` present) and the schedule value is correct. | A dedicated wiring test |

## Wiring test — preferred pattern (`ApplicationContextRunner`)

Fast, no container, no awaiting a real tick. Booting the production config proves the `@Scheduled`
placeholder resolved (a forgotten/blank schedule fails `hasNotFailed()`); parsing the expression proves
it *means* what the requirement says.

```java
class EventResubmitSchedulingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchedulingConfiguration.class, ResubmitIncompletePublicationsJob.class)
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    @DisplayName("Production scheduling config boots: scheduler enabled, resubmit interval wired")
    void productionSchedulingConfig_bootstraps_withConfiguredInterval() {
        contextRunner
                .withPropertyValues("spring.profiles.active=prod")
                .run(context -> {
                    then(context).hasNotFailed();                       // @Scheduled placeholder resolved
                    then(context).hasSingleBean(ResubmitIncompletePublicationsJob.class);
                    then(context).hasSingleBean(LockProvider.class);    // ShedLock wired
                    var props = context.getBean(EventResubmitProperties.class);
                    then(props.interval()).isEqualTo(Duration.ofSeconds(5));
                });
    }
}
```

For a **cron** schedule, additionally pin the next-fire times with a parameterized test —
`CronExpression.parse(cron).next(baseDateTime)` against a `@CsvSource` of `base,expected` rows — so the
expression's *meaning* (not just its presence) is asserted.

## Wiring-failure modes this catches

| Current code | Wiring test failure |
|--------------|---------------------|
| Missing `@Scheduled` / blank schedule property | Context fails — placeholder unresolved (`hasNotFailed()` red) |
| Missing `@EnableScheduling` (or not application-wide) | No scheduling active; job never fires in prod (assert config present) |
| Job not gated / wrong enable flag | Wrong bean presence under prod vs test profile |
| Wrong cron expression | `CronExpression.next(...)` ≠ expected fire time |
| Missing `LockProvider` / `@EnableSchedulerLock` | `then(context).hasSingleBean(LockProvider.class)` red |
| ShedLock table missing from Liquibase | Context refresh fails on first lock acquisition |

## Rules

- The wiring test boots the **production** scheduling configuration — it must not disable scheduling.
- The logic scenarios run under the `test` profile with the job's `enabled` flag `false`, and invoke the
  method directly — never await a real tick (slow, flaky).
- Mock/stub the job's collaborator in the wiring test when present — wiring tests verify wiring, not logic.
- Keep the wiring test out of the shared full-context cache: it is a sliced/partial context by design
  (`ApplicationContextRunner` / focused `@SpringBootTest(classes = …)`), which the single-context rule exempts.

## Key Paths (this project)

- Wiring tests: `src/test/java/by/iivanov/rpm/…/infrastructure/events/`
- Production config under test: `src/main/resources/application.yml` (+ `application-prod.yml`)
