package by.iivanov.rpm.iam.user.infrastructure.web;

import static by.iivanov.rpm.testing.ConstraintViolationCases.blankCase;
import static by.iivanov.rpm.testing.ConstraintViolationCases.email;
import static by.iivanov.rpm.testing.ConstraintViolationCases.invalidField;
import static by.iivanov.rpm.testing.ConstraintViolationCases.notBlank;
import static by.iivanov.rpm.testing.ConstraintViolationCases.size;
import static by.iivanov.rpm.testing.ConstraintViolationCases.tooLongCase;
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

    private static final Selector FIRST_NAME = field(RegisterUserRequest::firstName);
    private static final Selector MIDDLE_NAME = field(RegisterUserRequest::middleName);
    private static final Selector LAST_NAME = field(RegisterUserRequest::lastName);
    private static final Selector LOGIN = field(RegisterUserRequest::login);
    private static final Selector EMAIL = field(RegisterUserRequest::email);
    private static final Selector TIME_ZONE = field(RegisterUserRequest::timeZone);

    private static final String FIRST_NAME_FIELD = "firstName";
    private static final String LAST_NAME_FIELD = "lastName";
    private static final String LOGIN_FIELD = "login";
    private static final String EMAIL_FIELD = "email";
    private static final String TIME_ZONE_FIELD = "timeZone";
    private static final String MALFORMED_EMAIL = "not-an-email";

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final Model<RegisterUserRequest> validModel = Instancio.of(RegisterUserRequest.class)
            .set(FIRST_NAME, "John")
            .set(MIDDLE_NAME, "Quincy")
            .set(LAST_NAME, "Doe")
            .set(LOGIN, "john_doe")
            .set(EMAIL, "john.doe@example.com")
            .set(TIME_ZONE, "America/New_York")
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
        var request = Instancio.of(validModel).set(MIDDLE_NAME, null).create();
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
        String blank = ConstraintViolationCases.BLANK;
        String tooLongName = "a".repeat(NAME_MAX + 1);
        String tooLongLogin = "a".repeat(LOGIN_MAX + 1);
        String longLocalPart = "a".repeat(EMAIL_MAX) + "@example.com";
        String tooLongTimeZone = "a".repeat(TIME_ZONE_MAX + 1);
        return Stream.of(
                blankCase(FIRST_NAME_FIELD, FIRST_NAME, blank),
                tooLongCase(FIRST_NAME_FIELD, FIRST_NAME, tooLongName, NAME_MAX),
                tooLongCase("middleName", MIDDLE_NAME, tooLongName, NAME_MAX),
                blankCase(LAST_NAME_FIELD, LAST_NAME, blank),
                tooLongCase(LAST_NAME_FIELD, LAST_NAME, tooLongName, NAME_MAX),
                blankCase(LOGIN_FIELD, LOGIN, blank),
                tooLongCase(LOGIN_FIELD, LOGIN, tooLongLogin, LOGIN_MAX),
                // A non-empty blank email is also malformed, so @NotBlank and @Email both fire.
                invalidField(
                        "Invalid email: blank", EMAIL, blank, notBlank(EMAIL_FIELD, blank), email(EMAIL_FIELD, blank)),
                invalidField("Invalid email: malformed", EMAIL, MALFORMED_EMAIL, email(EMAIL_FIELD, MALFORMED_EMAIL)),
                // A 254-char local part exceeds @Size and is rejected by @Email, so both fire.
                invalidField(
                        "Invalid email: too long",
                        EMAIL,
                        longLocalPart,
                        size(EMAIL_FIELD, longLocalPart, 0, EMAIL_MAX),
                        email(EMAIL_FIELD, longLocalPart)),
                blankCase(TIME_ZONE_FIELD, TIME_ZONE, blank),
                tooLongCase(TIME_ZONE_FIELD, TIME_ZONE, tooLongTimeZone, TIME_ZONE_MAX));
    }
}
