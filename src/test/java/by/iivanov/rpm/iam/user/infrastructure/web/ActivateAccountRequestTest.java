package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
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

class ActivateAccountRequestTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final Model<ActivateAccountRequest> validModel = Instancio.of(ActivateAccountRequest.class)
            .set(field(ActivateAccountRequest::token), "NotBlankToken")
            .set(field(ActivateAccountRequest::password), "Str0ng!Pass#9")
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
        String blankToken = " \t \n";
        String blankPassword = " ".repeat(PasswordPolicy.MIN_LENGTH);
        String shortPassword = "a".repeat(PasswordPolicy.MIN_LENGTH - 1);
        String longPassword = "a".repeat(PasswordPolicy.MAX_LENGTH + 1);
        return Stream.of(
                // token
                argumentSet(
                        "Invalid token: blank",
                        field(ActivateAccountRequest::token),
                        blankToken,
                        List.of(ConstraintViolationAssert.violationOf(NotBlank.class)
                                .withProperty("token")
                                .withMessage("must not be blank")
                                .withInvalidValue(blankToken))),

                // password
                argumentSet(
                        "Invalid password: blank",
                        field(ActivateAccountRequest::password),
                        blankPassword,
                        List.of(ConstraintViolationAssert.violationOf(NotBlank.class)
                                .withProperty("password")
                                .withMessage("must not be blank")
                                .withInvalidValue(blankPassword))),
                argumentSet(
                        "Invalid password: too short",
                        field(ActivateAccountRequest::password),
                        shortPassword,
                        List.of(ConstraintViolationAssert.violationOf(Size.class)
                                .withProperty("password")
                                .withMessage("size must be between 12 and 128")
                                .withInvalidValue(shortPassword))),
                argumentSet(
                        "Invalid password: too long",
                        field(ActivateAccountRequest::password),
                        longPassword,
                        List.of(ConstraintViolationAssert.violationOf(Size.class)
                                .withProperty("password")
                                .withMessage("size must be between 12 and 128")
                                .withInvalidValue(longPassword))));
    }
}
