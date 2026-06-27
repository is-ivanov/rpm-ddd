package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    private static final int NAME_MAX = 255;
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
        String tooLongLogin = "a".repeat(Login.MAX_LENGTH + 1);
        String longLocalPart = "a".repeat(EmailAddress.MAX_LENGTH) + "@example.com";
        String tooLongTimeZone = "a".repeat(TIME_ZONE_MAX + 1);
        return Stream.of(
                blankCase("firstName", field(RegisterUserRequest::firstName), blank),
                sizeCase("firstName", field(RegisterUserRequest::firstName), tooLongName, NAME_MAX),
                sizeCase("middleName", field(RegisterUserRequest::middleName), tooLongName, NAME_MAX),
                blankCase("lastName", field(RegisterUserRequest::lastName), blank),
                sizeCase("lastName", field(RegisterUserRequest::lastName), tooLongName, NAME_MAX),
                blankCase("login", field(RegisterUserRequest::login), blank),
                sizeCase("login", field(RegisterUserRequest::login), tooLongLogin, Login.MAX_LENGTH),
                // A non-empty blank email is also malformed, so @NotBlank and @Email both fire.
                argumentSet(
                        "Invalid email: blank",
                        field(RegisterUserRequest::email),
                        blank,
                        List.of(notBlank("email", blank), emailFormat(blank))),
                argumentSet(
                        "Invalid email: malformed",
                        field(RegisterUserRequest::email),
                        "not-an-email",
                        List.of(emailFormat("not-an-email"))),
                // A 254-char local part exceeds @Size and is rejected by @Email, so both fire.
                argumentSet(
                        "Invalid email: too long",
                        field(RegisterUserRequest::email),
                        longLocalPart,
                        List.of(size("email", longLocalPart, EmailAddress.MAX_LENGTH), emailFormat(longLocalPart))),
                blankCase("timeZone", field(RegisterUserRequest::timeZone), blank),
                sizeCase("timeZone", field(RegisterUserRequest::timeZone), tooLongTimeZone, TIME_ZONE_MAX));
    }

    private static Arguments blankCase(String property, Selector field, String blankValue) {
        return argumentSet(
                "Invalid %s: blank".formatted(property), field, blankValue, List.of(notBlank(property, blankValue)));
    }

    private static Arguments sizeCase(String property, Selector field, String tooLongValue, int max) {
        return argumentSet(
                "Invalid %s: too long".formatted(property),
                field,
                tooLongValue,
                List.of(size(property, tooLongValue, max)));
    }

    private static ConstraintViolationAssert.ViolationExpectation notBlank(String property, String invalidValue) {
        return ConstraintViolationAssert.violationOf(NotBlank.class)
                .withProperty(property)
                .withMessage("must not be blank")
                .withInvalidValue(invalidValue);
    }

    private static ConstraintViolationAssert.ViolationExpectation size(String property, String invalidValue, int max) {
        return ConstraintViolationAssert.violationOf(Size.class)
                .withProperty(property)
                .withMessage("size must be between 0 and %d".formatted(max))
                .withInvalidValue(invalidValue);
    }

    private static ConstraintViolationAssert.ViolationExpectation emailFormat(String invalidValue) {
        return ConstraintViolationAssert.violationOf(Email.class)
                .withProperty("email")
                .withMessage("must be a well-formed email address")
                .withInvalidValue(invalidValue);
    }
}
