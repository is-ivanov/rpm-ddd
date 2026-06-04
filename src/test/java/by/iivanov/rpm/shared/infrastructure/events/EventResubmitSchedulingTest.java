package by.iivanov.rpm.shared.infrastructure.events;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

import by.iivanov.rpm.shared.infrastructure.scheduling.SchedulingConfiguration;
import java.time.Clock;
import java.time.Duration;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.TaskScheduler;

/**
 * Wiring test for the production resubmit scheduler. Boots {@link SchedulingConfiguration} and
 * {@link ResubmitIncompletePublicationsJob} against the base {@code application.yml} (no profile) and
 * asserts the scheduling is wired: the context starts, a {@link LockProvider} is present, the job bean
 * exists, and the resubmit interval resolves to the configured value.
 */
class EventResubmitSchedulingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(
                    SchedulingConfiguration.class, ResubmitIncompletePublicationsJob.class, JobCollaborators.class);

    @Test
    @DisplayName("The resubmit scheduler is wired, scheduled, and lock-guarded in production")
    void productionSchedulingConfig_bootstraps_withConfiguredInterval() {
        contextRunner.run(context -> {
            then(context).hasNotFailed();
            then(context).hasSingleBean(LockProvider.class);
            then(context).hasSingleBean(ResubmitIncompletePublicationsJob.class);
            then(context.getBean(EventResubmitProperties.class).interval()).isEqualTo(Duration.ofSeconds(5));
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class JobCollaborators {

        @Bean
        IncompleteEventPublications incompleteEventPublications() {
            return mock(IncompleteEventPublications.class);
        }

        @Bean
        Clock systemClock() {
            return Clock.systemUTC();
        }

        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }

        /**
         * Mock scheduler so {@code @EnableScheduling} registers the job against it without ever
         * executing it. The real job fires almost immediately on a fixed delay, and its
         * {@code @SchedulerLock} advice would then race the {@link ApplicationContextRunner}'s
         * immediate context shutdown — lazily building ShedLock's {@code ImportAware} config after
         * Spring's internal {@code importRegistry} bean is gone, logging a spurious scheduled-task
         * error. A no-op scheduler keeps this a pure wiring test.
         */
        @Bean
        TaskScheduler taskScheduler() {
            return mock(TaskScheduler.class);
        }
    }
}
