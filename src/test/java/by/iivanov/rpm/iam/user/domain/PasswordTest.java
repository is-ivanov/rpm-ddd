package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PasswordTest {

    private static final String VALID_HASH = "$2a$10$abcdefghijklmnopqrstuvwxABCDEFGHIJ1234567890abcdefghi";
    private static final String ENCODED_PASSWORD = "{bcrypt}" + VALID_HASH;

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("invalidValues")
        @DisplayName("WHEN invalid value EXPECT DomainValidationException with message")
        void when_invalidValue_expect_exception(String value, String expectedMessage) {
            // GIVEN:
            // WHEN:
            var caughtException = catchException(() -> new Password(value));
            // THEN:
            then(caughtException).isInstanceOf(DomainValidationException.class).hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidValues() {
            return Stream.of(
                    argumentSet("null value", null, "Password must not be blank"),
                    argumentSet("blank value", "  \t  ", "Password must not be blank"),
                    argumentSet("empty string", "", "Password must not be blank"));
        }

        @ParameterizedTest
        @MethodSource("validValues")
        @DisplayName("WHEN valid value EXPECT Password created")
        void when_validValue_expect_passwordCreated(String value, String expected) {
            // GIVEN:
            // WHEN:
            var password = new Password(value);
            // THEN:
            then(password.hash()).isEqualTo(expected);
        }

        static Stream<Arguments> validValues() {
            return Stream.of(
                    argumentSet("BCrypt encoded", ENCODED_PASSWORD, ENCODED_PASSWORD),
                    argumentSet("with leading/trailing spaces", "  " + ENCODED_PASSWORD + "  ", ENCODED_PASSWORD),
                    argumentSet("NoOpPassword in tests", "{noop}admin", "{noop}admin"),
                    argumentSet("plain hash", VALID_HASH, VALID_HASH),
                    argumentSet("simple password", "admin123", "admin123"));
        }
    }
}
