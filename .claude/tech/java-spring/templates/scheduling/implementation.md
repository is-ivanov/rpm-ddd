# Scheduling Implementation Template (Spring Boot · modular monolith)

Binds the universal "Scheduled / Recurring Jobs" rule (`tdd-rules.md`) to this stack. Jobs live in the
infrastructure ring of the owning subdomain, or in `shared.infrastructure.events` when they operate
across the whole application's event registry.

## Rules

- **Schedule comes from a required property.** Annotate with `@Scheduled(cron = "${prop.cron}")` or
  `@Scheduled(fixedRateString = "${prop.interval}")` — never a hardcoded literal. A missing property
  then fails context startup (placeholder cannot resolve) instead of silently never firing.
- **`@EnableScheduling` is application-wide.** It enables every `@Scheduled` method in the context, so
  it belongs on one general application configuration that is always loaded — not on a per-job config.
- **Gate each job independently** with `@ConditionalOnProperty(name = "<job>.enabled", matchIfMissing =
  true)` on the job component. Integration tests set the flag to `false` (in `application-test.yml`) so
  the job never auto-fires during tests; its logic is exercised by invoking the method directly. Other
  jobs keep firing because scheduling itself stays enabled.
- **Type the configuration.** Bind related schedule values into one `@ConfigurationProperties` record
  (`@Validated`, `@NotNull`/`@DefaultValue` per field) — never inject each value with a bare `@Value`.
  See the config-grouping rule in `coding-rules.md`.
- **Multi-instance safety: ShedLock.** The backend runs as multiple instances, so an unguarded
  `@Scheduled` job fires on every instance concurrently. Add `@SchedulerLock` on the job method, with
  `@EnableSchedulerLock` + a JDBC `LockProvider` bean on the shared scheduling configuration, and a
  Liquibase migration for the `shedlock` table. ShedLock is **not yet a dependency** in `pom.xml` — adding
  it (`net.javacrumbs.shedlock:shedlock-spring` + `shedlock-provider-jdbc-template`) is part of the job's
  implementation step.
- **Jobs delegate; no business logic in the job body.** A job resolves its schedule, acquires its lock,
  and calls into the domain/application or framework collaborator — nothing more.

## Pattern (per this repo)

```java
// 1. Typed schedule config — one record per job concern
@ConfigurationProperties("rpm.events.resubmit")
@Validated
public record EventResubmitProperties(@DefaultValue("true") boolean enabled, @NotNull Duration interval) {}

// 2. Application-wide scheduling switch — always loaded, enables ALL @Scheduled methods
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
class SchedulingConfiguration {
    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(/* … shedlock table … */);
    }
}

// 3. The job — gated independently, schedule + lock from properties
@InfrastructureComponent
@ConditionalOnProperty(name = "rpm.events.resubmit.enabled", matchIfMissing = true)
class ResubmitIncompletePublicationsJob {

    @Scheduled(fixedRateString = "${rpm.events.resubmit.interval}")
    @SchedulerLock(name = "resubmitIncompletePublications")
    public void resubmit() { /* delegate */ }
}
```

## Configuration

`application.yml` (production default):
```yaml
rpm:
  events:
    resubmit:
      interval: 5s        # required — bound by @Scheduled(fixedRateString)
      # enabled defaults to true
```

`application-test.yml` (integration tests disable auto-firing; logic tested by direct invocation):
```yaml
rpm:
  events:
    resubmit:
      enabled: false
```

## Key Paths (this project)

- Cross-cutting job + scheduling config: `src/main/java/by/iivanov/rpm/shared/infrastructure/events/`
- Subdomain-specific job: `…/{context}/{subdomain}/infrastructure/events/`
- ShedLock migration: `src/main/resources/db/changelog/…`
- Properties: co-located with the job; registered via `@ConfigurationPropertiesScan` or `@EnableConfigurationProperties`
