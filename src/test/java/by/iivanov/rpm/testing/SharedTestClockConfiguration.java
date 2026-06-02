package by.iivanov.rpm.testing;

import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.threeten.extra.MutableClock;

/**
 * Registers the controllable test clock as a {@link MutableClock} bean so any component can inject it
 * directly — no cast. The same instance also backs the {@code clock} bean ({@code Clock} type) that
 * the base test overrides, so advancing it through {@link MutableClock} is observed by production code
 * that reads the {@code Clock}. Imported on {@link AbstractApplicationIntegrationTest} so every full
 * integration context shares this single configuration.
 */
@TestConfiguration(proxyBeanMethods = false)
public class SharedTestClockConfiguration {

    static final Instant FIXED_INSTANT = Instant.parse("2026-01-05T10:23:56.632Z");
    static final MutableClock CLOCK = MutableClock.of(FIXED_INSTANT, ZoneOffset.UTC);

    @Bean
    MutableClock mutableClock() {
        return CLOCK;
    }

    /** Rewinds the shared test clock to its fixed baseline so each test starts from the same instant. */
    static void resetToFixed() {
        CLOCK.setInstant(FIXED_INSTANT);
    }
}
