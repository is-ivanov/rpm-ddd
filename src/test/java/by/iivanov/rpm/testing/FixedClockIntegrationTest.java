package by.iivanov.rpm.testing;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FixedClockIntegrationTest extends AbstractApplicationIntegrationTest {

    @Test
    @DisplayName("WHEN integration test context starts EXPECT fixed clock bean")
    void when_contextStarts_expect_fixedClockBean() {
        then(clock.instant()).isEqualTo("2026-01-05T10:23:56.632Z");
    }
}
