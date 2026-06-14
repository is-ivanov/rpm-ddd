package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LoginThrottleTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @Test
        @DisplayName("WHEN failedAttempts is negative EXPECT DomainValidationException with message")
        void when_negativeFailedAttempts_expect_exception() {
            // GIVEN:
            // WHEN:
            var caughtException = catchException(() -> new LoginThrottle(-1, null));

            // THEN:
            then(caughtException)
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessage("Failed login attempts must not be negative, but was -1");
        }
    }
}
