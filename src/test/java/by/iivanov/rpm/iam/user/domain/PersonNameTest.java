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

class PersonNameTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("invalidValues")
        @DisplayName("WHEN invalid property EXPECT DomainValidationException with message")
        void when_invalidProperty_expect_exception(
                String firstName, String middleName, String lastName, String expectedMessagePart) {
            // GIVEN:
            // WHEN:
            var caughtException = catchException(() -> new PersonName(firstName, middleName, lastName));
            // THEN:
            then(caughtException).isInstanceOf(DomainValidationException.class).hasMessage(expectedMessagePart);
        }

        static Stream<Arguments> invalidValues() {
            return Stream.of(
                    argumentSet(
                            "blank firstName, rest valid",
                            "  \t  ",
                            "Ivanovich",
                            "Ivanov",
                            "First name must not be blank"),
                    argumentSet(
                            "null firstName, rest valid", null, "Ivanovich", "Ivanov", "First name must not be blank"),
                    argumentSet(
                            "blank lastName, rest valid", "Ivan", "Ivanovich", "  \t  ", "Last name must not be blank"),
                    argumentSet("null lastName, rest valid", "Ivan", "Ivanovich", null, "Last name must not be blank"),
                    argumentSet(
                            "firstName exceeds 255, rest valid",
                            "a".repeat(256),
                            "Ivanovich",
                            "Ivanov",
                            "First name must not exceed 255 characters, but was 256"),
                    argumentSet(
                            "lastName exceeds 255, rest valid",
                            "Ivan",
                            "Ivanovich",
                            "a".repeat(256),
                            "Last name must not exceed 255 characters, but was 256"),
                    argumentSet(
                            "middleName exceeds 255, rest valid",
                            "Ivan",
                            "a".repeat(256),
                            "Ivanov",
                            "Middle name must not exceed 255 characters, but was 256"));
        }

        @ParameterizedTest
        @MethodSource("validValues")
        @DisplayName("WHEN valid values EXPECT PersonName created with trimmed values")
        void when_validValues_expect_personNameCreated(
                String firstName,
                String middleName,
                String lastName,
                String expectedFirst,
                String expectedMiddle,
                String expectedLast) {
            // GIVEN:
            // WHEN:
            var name = new PersonName(firstName, middleName, lastName);
            // THEN:
            then(name.firstName()).isEqualTo(expectedFirst);
            then(name.middleName()).isEqualTo(expectedMiddle);
            then(name.lastName()).isEqualTo(expectedLast);
        }

        static Stream<Arguments> validValues() {
            return Stream.of(
                    argumentSet("all fields, no trim", "Ivan", "Ivanovich", "Ivanov", "Ivan", "Ivanovich", "Ivanov"),
                    argumentSet(
                            "all fields, with spaces",
                            "  Ivan  ",
                            "  Ivanovich  ",
                            "  Ivanov  ",
                            "Ivan",
                            "Ivanovich",
                            "Ivanov"),
                    argumentSet("null middleName", "Ivan", null, "Ivanov", "Ivan", null, "Ivanov"),
                    argumentSet("blank middleName becomes null", "Ivan", "  \t  ", "Ivanov", "Ivan", null, "Ivanov"),
                    argumentSet(
                            "max length fields",
                            "a".repeat(255),
                            "b".repeat(255),
                            "c".repeat(255),
                            "a".repeat(255),
                            "b".repeat(255),
                            "c".repeat(255)));
        }
    }
}
