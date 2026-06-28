package by.iivanov.rpm.iam.user.infrastructure.web;

import static by.iivanov.rpm.testing.ConstraintViolationCases.blankCase;
import static by.iivanov.rpm.testing.ConstraintViolationCases.email;
import static by.iivanov.rpm.testing.ConstraintViolationCases.invalidField;
import static by.iivanov.rpm.testing.ConstraintViolationCases.notBlank;
import static by.iivanov.rpm.testing.ConstraintViolationCases.size;
import static by.iivanov.rpm.testing.ConstraintViolationCases.tooLongCase;
import static org.instancio.Select.field;

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
                blankCase("firstName", field(RegisterUserRequest::firstName), blank),
                tooLongCase("firstName", field(RegisterUserRequest::firstName), tooLongName, NAME_MAX),
                tooLongCase("middleName", field(RegisterUserRequest::middleName), tooLongName, NAME_MAX),
                blankCase("lastName", field(RegisterUserRequest::lastName), blank),
                tooLongCase("lastName", field(RegisterUserRequest::lastName), tooLongName, NAME_MAX),
                blankCase("login", field(RegisterUserRequest::login), blank),
                tooLongCase("login", field(RegisterUserRequest::login), tooLongLogin, LOGIN_MAX),
                // A non-empty blank email is also malformed, so @NotBlank and @Email both fire.
                invalidField(
                        "Invalid email: blank",
                        field(RegisterUserRequest::email),
                        blank,
                        notBlank("email", blank),
                        email("email", blank)),
                invalidField(
                        "Invalid email: malformed",
                        field(RegisterUserRequest::email),
                        "not-an-email",
                        email("email", "not-an-email")),
                // A 254-char local part exceeds @Size and is rejected by @Email, so both fire.
                invalidField(
                        "Invalid email: too long",
                        field(RegisterUserRequest::email),
                        longLocalPart,
                        size("email", longLocalPart, 0, EMAIL_MAX),
                        email("email", longLocalPart)),
                blankCase("timeZone", field(RegisterUserRequest::timeZone), blank),
                tooLongCase("timeZone", field(RegisterUserRequest::timeZone), tooLongTimeZone, TIME_ZONE_MAX));
    }
}
