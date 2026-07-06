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

    private static final String FIRST_NAME = "Ivan";
    private static final String MIDDLE_NAME = "Ivanovich";
    private static final String LAST_NAME = "Ivanov";
    private static final String BLANK = "  \t  ";
    private static final String FIRST_NAME_BLANK = "First name must not be blank";
    private static final String LAST_NAME_BLANK = "Last name must not be blank";

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
                    argumentSet("blank firstName, rest valid", BLANK, MIDDLE_NAME, LAST_NAME, FIRST_NAME_BLANK),
                    argumentSet("null firstName, rest valid", null, MIDDLE_NAME, LAST_NAME, FIRST_NAME_BLANK),
                    argumentSet("blank lastName, rest valid", FIRST_NAME, MIDDLE_NAME, BLANK, LAST_NAME_BLANK),
                    argumentSet("null lastName, rest valid", FIRST_NAME, MIDDLE_NAME, null, LAST_NAME_BLANK),
                    argumentSet(
                            "firstName exceeds 255, rest valid",
                            "a".repeat(256),
                            MIDDLE_NAME,
                            LAST_NAME,
                            "First name must not exceed 255 characters, but was 256"),
                    argumentSet(
                            "lastName exceeds 255, rest valid",
                            FIRST_NAME,
                            MIDDLE_NAME,
                            "a".repeat(256),
                            "Last name must not exceed 255 characters, but was 256"),
                    argumentSet(
                            "middleName exceeds 255, rest valid",
                            FIRST_NAME,
                            "a".repeat(256),
                            LAST_NAME,
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
                    argumentSet(
                            "all fields, no trim",
                            FIRST_NAME,
                            MIDDLE_NAME,
                            LAST_NAME,
                            FIRST_NAME,
                            MIDDLE_NAME,
                            LAST_NAME),
                    argumentSet(
                            "all fields, with spaces",
                            "  Ivan  ",
                            "  Ivanovich  ",
                            "  Ivanov  ",
                            FIRST_NAME,
                            MIDDLE_NAME,
                            LAST_NAME),
                    argumentSet("null middleName", FIRST_NAME, null, LAST_NAME, FIRST_NAME, null, LAST_NAME),
                    argumentSet(
                            "blank middleName becomes null", FIRST_NAME, BLANK, LAST_NAME, FIRST_NAME, null, LAST_NAME),
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
