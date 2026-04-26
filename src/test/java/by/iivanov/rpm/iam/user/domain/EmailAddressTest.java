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

class EmailAddressTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("invalidValues")
        @DisplayName("WHEN invalid value EXPECT DomainValidationException with message")
        void when_invalidValue_expect_exception(String value, String expectedMessagePart) {
            // GIVEN:
            // WHEN:
            var caughtException = catchException(() -> new EmailAddress(value));
            // THEN:
            then(caughtException).isInstanceOf(DomainValidationException.class).hasMessage(expectedMessagePart);
        }

        static Stream<Arguments> invalidValues() {
            return Stream.of(
                    argumentSet("null value", null, "Email must not be blank"),
                    argumentSet("blank value", "  \t  ", "Email must not be blank"),
                    argumentSet("empty string", "", "Email must not be blank"),
                    argumentSet(
                            "missing @ symbol",
                            "notAnEmail",
                            "Email must be a well-formed email address, but was: notanemail"),
                    argumentSet(
                            "missing local part",
                            "@example.com",
                            "Email must be a well-formed email address, but was: @example.com"),
                    argumentSet(
                            "exceeds max size", "a".repeat(255), "Email must not exceed 254 characters, but was 255"));
        }

        @ParameterizedTest
        @MethodSource("validValues")
        @DisplayName("WHEN valid value EXPECT EmailAddress created and lowercased")
        void when_validValue_expect_createdAndLowercased(String value, String expected) {
            // GIVEN:
            // WHEN:
            var email = new EmailAddress(value);
            // THEN:
            then(email.email()).isEqualTo(expected);
        }

        static Stream<Arguments> validValues() {
            return Stream.of(
                    argumentSet("simple email", "test@example.com", "test@example.com"),
                    argumentSet("uppercase gets lowercased", "  TEST@EXAMPLE.COM  ", "test@example.com"));
        }
    }
}
