package by.iivanov.rpm.shared.infrastructure.scheduling;

import by.iivanov.rpm.shared.infrastructure.events.EventResubmitProperties;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application-wide scheduling configuration. Owns {@code @EnableScheduling},
 * {@code @EnableSchedulerLock}, and the {@link LockProvider} bean, gated by
 * {@code rpm.scheduler.enabled} (defaults true). When the flag is {@code false} — as in the test
 * profile — this configuration is absent, so no {@code @Scheduled} method fires and the
 * {@code @SchedulerLock} aspect stays inactive.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@ConditionalOnProperty(name = "rpm.scheduler.enabled", matchIfMissing = true)
@EnableConfigurationProperties(EventResubmitProperties.class)
public class SchedulingConfiguration {

    /**
     * Provides the ShedLock {@link LockProvider} backed by the application data source, so scheduled
     * jobs annotated with {@code @SchedulerLock} run at most once across instances.
     *
     * @param dataSource the application data source backing the {@code shedlock} table
     * @return a JDBC-template-backed lock provider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
