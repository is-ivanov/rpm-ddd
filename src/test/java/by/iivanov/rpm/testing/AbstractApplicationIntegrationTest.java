package by.iivanov.rpm.testing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.threeten.extra.MutableClock;

@ApplicationIntegrationTest
public abstract class AbstractApplicationIntegrationTest {

    @SuppressWarnings("NullAway.Init")
    @TestBean(name = "clock", enforceOverride = true)
    protected Clock clock;

    static Clock clock() {
        return MutableClock.of(Instant.parse("2026-01-05T10:23:56.632Z"), ZoneOffset.UTC);
    }

    protected MutableClock mutableClock() {
        return (MutableClock) clock;
    }
}
