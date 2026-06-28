package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.instancio.Select.field;

import by.iivanov.rpm.testing.ConstraintViolationCases;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.stream.Stream;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Selector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RegisterUserRequestTest {

    // Contract limits pinned as literals on purpose: a boundary test must FAIL (not silently follow)
    // if the production limit changes. Mirrors @RequiredString/@Size(255), Login.MAX_LENGTH (50),
    // EmailAddress.MAX_LENGTH (254), and RegisterUserRequest.timeZone @Size(64).
    private static final int NAME_MAX = 255;
    private static final int LOGIN_MAX = 50;
    private static final int EMAIL_MAX = 254;
    private static final int TIME_ZONE_MAX = 64;

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final Model<RegisterUserRequest> validModel = Instancio.of(RegisterUserRequest.class)
            .set(field(RegisterUserRequest::firstName), "John")
            .set(field(RegisterUserRequest::middleName), "Quincy")
            .set(field(RegisterUserRequest::lastName), "Doe")
            .set(field(RegisterUserRequest::login), "john_doe")
            .set(field(RegisterUserRequest::email), "john.doe@example.com")
            .set(field(RegisterUserRequest::timeZone), "America/New_York")
            .toModel();

    @Test
    @DisplayName("Valid request")
    void should_noViolations_when_allFieldsAreValid() {
        // GIVEN: valid request
        var validRequest = Instancio.of(validModel).create();
        // WHEN:
        var actualViolations = validator.validate(validRequest);
        // THEN:
        ConstraintViolationAssert.assertThat(actualViolations).isEmpty();
    }

    @Test
    @DisplayName("Valid request: absent middle name is allowed")
    void should_noViolations_when_middleNameIsNull() {
        // GIVEN: a request without a middle name
        var request = Instancio.of(validModel)
                .set(field(RegisterUserRequest::middleName), null)
                .create();
        // WHEN:
        var actualViolations = validator.validate(request);
        // THEN:
        ConstraintViolationAssert.assertThat(actualViolations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    @DisplayName("Invalid request")
    void should_reportViolation_when_invalidField(
            Selector field,
            Object fieldValue,
            List<ConstraintViolationAssert.ViolationExpectation> expectedViolations) {
        // GIVEN:
        var request = Instancio.of(validModel).set(field, fieldValue).create();
        // WHEN:
        var violations = validator.validate(request);
        // THEN:
        ConstraintViolationAssert.assertThat(violations)
                .containsOnlyViolations(
                        expectedViolations.toArray(new ConstraintViolationAssert.ViolationExpectation[0]));
    }

    static Stream<Arguments> invalidFields() {
        String blank = " \t \n";
        String tooLongName = "a".repeat(NAME_MAX + 1);
        String tooLongLogin = "a".repeat(LOGIN_MAX + 1);
        String longLocalPart = "a".repeat(EMAIL_MAX) + "@example.com";
        String tooLongTimeZone = "a".repeat(TIME_ZONE_MAX + 1);
        return Stream.of(
                ConstraintViolationCases.blankCase("firstName", field(RegisterUserRequest::firstName), blank),
                ConstraintViolationCases.tooLongCase(
                        "firstName", field(RegisterUserRequest::firstName), tooLongName, NAME_MAX),
                ConstraintViolationCases.tooLongCase(
                        "middleName", field(RegisterUserRequest::middleName), tooLongName, NAME_MAX),
                ConstraintViolationCases.blankCase("lastName", field(RegisterUserRequest::lastName), blank),
                ConstraintViolationCases.tooLongCase(
                        "lastName", field(RegisterUserRequest::lastName), tooLongName, NAME_MAX),
                ConstraintViolationCases.blankCase("login", field(RegisterUserRequest::login), blank),
                ConstraintViolationCases.tooLongCase(
                        "login", field(RegisterUserRequest::login), tooLongLogin, LOGIN_MAX),
                // A non-empty blank email is also malformed, so @NotBlank and @Email both fire.
                ConstraintViolationCases.invalidField(
                        "Invalid email: blank",
                        field(RegisterUserRequest::email),
                        blank,
                        ConstraintViolationCases.notBlank("email", blank),
                        ConstraintViolationCases.email("email", blank)),
                ConstraintViolationCases.invalidField(
                        "Invalid email: malformed",
                        field(RegisterUserRequest::email),
                        "not-an-email",
                        ConstraintViolationCases.email("email", "not-an-email")),
                // A 254-char local part exceeds @Size and is rejected by @Email, so both fire.
                ConstraintViolationCases.invalidField(
                        "Invalid email: too long",
                        field(RegisterUserRequest::email),
                        longLocalPart,
                        ConstraintViolationCases.size("email", longLocalPart, 0, EMAIL_MAX),
                        ConstraintViolationCases.email("email", longLocalPart)),
                ConstraintViolationCases.blankCase("timeZone", field(RegisterUserRequest::timeZone), blank),
                ConstraintViolationCases.tooLongCase(
                        "timeZone", field(RegisterUserRequest::timeZone), tooLongTimeZone, TIME_ZONE_MAX));
    }
}
