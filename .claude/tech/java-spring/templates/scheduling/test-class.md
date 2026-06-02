# Scheduling Test Template (Spring Boot · modular monolith)

Binds the universal "Scheduled / Recurring Jobs" rule (`tdd-rules.md`) to this stack — the *why* (a job
has separable **logic** and **wiring** concerns, and wiring is the part that silently rots) lives there.
This template is the Spring *how*: which test type covers each concern in this repo.

| Concern | How to test | Where |
|---------|-------------|-------|
| **Logic** — what the job does each run | Call the method directly (deterministic); assert side effects. Auto-firing is disabled under the `test` profile so the scheduler never races the test. | Integration/usecase scenarios (e.g. resubmit 6.1/7.1) |
| **Wiring** — that it is actually scheduled in production | Fast `ApplicationContextRunner` test under the production configuration: assert the context starts (schedule resolved + `@EnableScheduling` active + `LockProvider` present) and the schedule value is correct. | A dedicated wiring test |

## Wiring test — preferred pattern (`ApplicationContextRunner`)

Fast, no container, no awaiting a real tick. Booting the production schedule proves the `@Scheduled`
placeholder resolved (a forgotten/blank schedule fails `hasNotFailed()`); parsing the expression proves
it *means* what the requirement says.

Two constraints make this accurate for our setup:
- **Load the production schedule, not the `test` overlay.** The interval/`enabled=true` live in base
  `application.yml` (`ConfigDataApplicationContextInitializer` loads it). Do **not** activate the `prod`
  profile — `application-prod.yml` only adds datasource/secret env-var placeholders (`${SPRING_DATASOURCE_URL}`),
  which are unresolved in a sliced test and would fail the context for the wrong reason. And do **not**
  activate the `test` profile — it sets `rpm.events.resubmit.enabled=false`, removing the gated job.
- **ShedLock's `LockProvider` needs a `DataSource`.** Supply a throwaway one so the bean-wiring assertion
  reflects production wiring without a full container.

```java
class EventResubmitSchedulingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())   // base application.yml, no profile
            .withUserConfiguration(SchedulingConfiguration.class, ResubmitIncompletePublicationsJob.class)
            .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class))
            .withPropertyValues("spring.datasource.url=jdbc:h2:mem:shedlock-wiring");   // LockProvider backing

    @Test
    @DisplayName("Production scheduling config boots: scheduler enabled, resubmit interval wired, lock provider present")
    void productionSchedulingConfig_bootstraps_withConfiguredInterval() {
        contextRunner.run(context -> {
            then(context).hasNotFailed();                       // @Scheduled placeholder resolved
            then(context).hasSingleBean(ResubmitIncompletePublicationsJob.class);
            then(context).hasSingleBean(LockProvider.class);    // ShedLock wired
            then(context.getBean(EventResubmitProperties.class).interval()).isEqualTo(Duration.ofSeconds(5));
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
| Job not gated / wrong enable flag | Wrong bean presence between the base (production) config and the `test` overlay |
| Wrong cron expression | `CronExpression.next(...)` ≠ expected fire time |
| Missing `LockProvider` / `@EnableSchedulerLock` | `then(context).hasSingleBean(LockProvider.class)` red |
| ShedLock table missing from Liquibase | Context refresh fails on first lock acquisition |

## Rules (Spring specifics)

The universal logic-vs-wiring split is in `tdd-rules.md`; the points below are the stack-specific mechanics.

- The wiring test boots the **production** schedule from base `application.yml` (`ConfigDataApplicationContextInitializer`)
  with no profile active — never the `test` overlay (which disables the job) and never the `prod` profile
  (whose datasource/secret env-var placeholders would fail the sliced context for the wrong reason).
- Mock/stub the job's collaborator in the wiring test when present — wiring tests verify wiring, not logic.
- Keep the wiring test out of the shared full-context cache: it is a sliced/partial context by design
  (`ApplicationContextRunner` / focused `@SpringBootTest(classes = …)`), which `tdd-rules.md` "Single Full
  Application Context" exempts.

## Key Paths (this project)

- Wiring tests: `src/test/java/by/iivanov/rpm/…/infrastructure/events/`
- Production schedule under test: `src/main/resources/application.yml` (base — the production default); the `test` overlay (`src/test/resources/application-test.yml`) is the one that disables the job
