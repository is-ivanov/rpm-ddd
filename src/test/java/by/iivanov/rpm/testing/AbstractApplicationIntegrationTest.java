package by.iivanov.rpm.testing;

import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.convention.TestBean;

@ApplicationIntegrationTest
@SharedSpies
@Import(SharedTestClockConfiguration.class)
public abstract class AbstractApplicationIntegrationTest {

    @SuppressWarnings("NullAway.Init")
    @TestBean(name = "clock", enforceOverride = true)
    protected Clock clock;

    static Clock clock() {
        return SharedTestClockConfiguration.CLOCK;
    }

    @BeforeEach
    void resetTestClock() {
        SharedTestClockConfiguration.resetToFixed();
    }
}
