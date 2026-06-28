package by.iivanov.rpm.iam.user.infrastructure.web;

import static by.iivanov.rpm.testing.ConstraintViolationCases.blankCase;
import static by.iivanov.rpm.testing.ConstraintViolationCases.invalidField;
import static by.iivanov.rpm.testing.ConstraintViolationCases.size;
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

class ActivateAccountRequestTest {

    // Contract limits pinned as literals on purpose: a boundary test must FAIL (not silently follow)
    // if the production limit changes. Mirrors PasswordPolicy.MIN_LENGTH (12) and MAX_LENGTH (128).
    private static final int PASSWORD_MIN = 12;
    private static final int PASSWORD_MAX = 128;

    private static final Selector TOKEN = field(ActivateAccountRequest::token);
    private static final Selector PASSWORD = field(ActivateAccountRequest::password);

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final Model<ActivateAccountRequest> validModel = Instancio.of(ActivateAccountRequest.class)
            .set(TOKEN, "NotBlankToken")
            .set(PASSWORD, "Str0ng!Pass#9")
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
        // A blank password of exactly PASSWORD_MIN spaces is length-valid, so only @NotBlank fires.
        String blankPassword = " ".repeat(PASSWORD_MIN);
        String shortPassword = "a".repeat(PASSWORD_MIN - 1);
        String longPassword = "a".repeat(PASSWORD_MAX + 1);
        return Stream.of(
                blankCase("token", TOKEN, ConstraintViolationCases.BLANK),
                blankCase("password", PASSWORD, blankPassword),
                invalidField(
                        "Invalid password: too short",
                        PASSWORD,
                        shortPassword,
                        size("password", shortPassword, PASSWORD_MIN, PASSWORD_MAX)),
                invalidField(
                        "Invalid password: too long",
                        PASSWORD,
                        longPassword,
                        size("password", longPassword, PASSWORD_MIN, PASSWORD_MAX)));
    }
}
