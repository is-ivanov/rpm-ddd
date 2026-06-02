package by.iivanov.rpm.shared.infrastructure.scheduling;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

/**
 * Guards that the shared test application context has scheduling fully disabled, so no {@code @Scheduled}
 * job (e.g. the resubmit scheduler) fires in the background during tests and races deterministic
 * assertions.
 *
 * <p>The {@link ScheduledAnnotationBeanPostProcessor} is the bean that processes every {@code @Scheduled}
 * method; it is registered only by {@code @EnableScheduling}. Under the {@code test} profile our
 * {@code SchedulingConfiguration} is excluded ({@code rpm.scheduler.enabled=false}) and Spring Modulith
 * Moments is excluded from the classpath, so nothing contributes {@code @EnableScheduling} and the
 * processor must be absent. If anyone re-adds Moments or an application-wide {@code @EnableScheduling},
 * this test fails loudly.
 */
class SchedulingDisabledInTestsIntegrationTest extends AbstractApplicationIntegrationTest {

    private final ApplicationContext applicationContext;

    SchedulingDisabledInTestsIntegrationTest(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Test
    @DisplayName("Test context has scheduling disabled — no @Scheduled job fires in the background")
    void testContext_hasNoScheduledAnnotationProcessor() {
        then(applicationContext.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class))
                .as("ScheduledAnnotationBeanPostProcessor must be absent so no @Scheduled method is processed in tests")
                .isEmpty();
    }
}
