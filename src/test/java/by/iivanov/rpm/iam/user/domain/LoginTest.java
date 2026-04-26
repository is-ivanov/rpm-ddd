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

class LoginTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("invalidValues")
        @DisplayName("WHEN invalid value EXPECT DomainValidationException with message")
        void when_invalidValue_expect_exception(String value, String expectedMessage) {
            // GIVEN:
            // WHEN:
            var caughtException = catchException(() -> new Login(value));
            // THEN:
            then(caughtException).isInstanceOf(DomainValidationException.class).hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidValues() {
            return Stream.of(
                    argumentSet("null value", null, "Login must not be blank"),
                    argumentSet("blank value", "  \t  ", "Login must not be blank"),
                    argumentSet("empty string", "", "Login must not be blank"),
                    argumentSet(
                            "exceeds max size 50", "a".repeat(51), "Login must not exceed 50 characters, but was 51"));
        }

        @ParameterizedTest
        @MethodSource("validValues")
        @DisplayName("WHEN valid value EXPECT Login created")
        void when_validValue_expect_loginCreated(String value, String expected) {
            // GIVEN:
            // WHEN:
            var login = new Login(value);
            // THEN:
            then(login.login()).isEqualTo(expected);
        }

        static Stream<Arguments> validValues() {
            return Stream.of(
                    argumentSet("simple login", "ivanov", "ivanov"),
                    argumentSet("with leading/trailing spaces", "  ivanov  ", "ivanov"),
                    argumentSet("max length 50 chars", "a".repeat(50), "a".repeat(50)));
        }
    }
}
