package by.iivanov.rpm.testing;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.ConstraintViolationAssert.ViolationExpectation;
import org.instancio.Selector;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Concise builders for request-DTO bean-validation test cases.
 *
 * <p>Every request DTO with {@code jakarta.validation} constraints gets a unit test that drives a
 * valid Instancio model through one invalid field at a time and asserts the produced violations with
 * {@link ConstraintViolationAssert}. This utility removes the per-test boilerplate by providing
 * {@link #notBlank}/{@link #size}/{@link #email} violation expectations and {@code argumentSet} case
 * builders for the common single-violation cases, while {@link #invalidField} composes the combined
 * cases (e.g. a blank or over-length {@code @Email} field fires both {@code @NotBlank}/{@code @Size}
 * and {@code @Email}).
 *
 * <p>Boundary limits stay pinned as literals at the call site (the production constant named only in
 * a comment), so a limit change fails the test instead of silently following it.
 */
public final class ConstraintViolationCases {

    /**
     * A non-empty but blank value — mixed whitespace (space, tab, newline). {@code @NotBlank} trims
     * before testing, so it rejects this; a plain emptiness/length check would wrongly accept it. Shared
     * across request-DTO tests so every {@code @NotBlank} field is exercised with the same representative.
     */
    public static final String BLANK = " \t \n";

    private ConstraintViolationCases() {}

    /** A {@code @NotBlank} violation expectation for {@code property} rejecting {@code invalidValue}. */
    public static ViolationExpectation notBlank(String property, Object invalidValue) {
        return ConstraintViolationAssert.violationOf(NotBlank.class)
                .withProperty(property)
                .withMessage("must not be blank")
                .withInvalidValue(invalidValue);
    }

    /**
     * A {@code @Size} violation expectation: message {@code "size must be between {min} and {max}"}.
     *
     * <p>Both bounds are explicit on purpose — a single-{@code int} overload would be ambiguous at the
     * call site ({@code min} vs {@code max}) and would permanently occupy the {@code (String, Object, int)}
     * signature, blocking a future min-only builder. For a {@code @Size(max)} field pass {@code 0} as min.
     */
    public static ViolationExpectation size(String property, Object invalidValue, int min, int max) {
        return ConstraintViolationAssert.violationOf(Size.class)
                .withProperty(property)
                .withMessage("size must be between %d and %d".formatted(min, max))
                .withInvalidValue(invalidValue);
    }

    /** An {@code @Email} violation expectation: message {@code "must be a well-formed email address"}. */
    public static ViolationExpectation email(String property, Object invalidValue) {
        return ConstraintViolationAssert.violationOf(Email.class)
                .withProperty(property)
                .withMessage("must be a well-formed email address")
                .withInvalidValue(invalidValue);
    }

    /** An invalid-field {@code argumentSet}: display name, the field selector, its value, and expected violations. */
    public static Arguments invalidField(
            String displayName, Selector field, Object value, ViolationExpectation... violations) {
        return argumentSet(displayName, field, value, List.of(violations));
    }

    /** A "blank" case: a non-empty whitespace value reported as a single {@code @NotBlank} violation. */
    public static Arguments blankCase(String property, Selector field, String blankValue) {
        return invalidField("Invalid %s: blank".formatted(property), field, blankValue, notBlank(property, blankValue));
    }

    /** A "too long" case: an over-{@code max} value reported as a single {@code @Size(max)} violation. */
    public static Arguments tooLongCase(String property, Selector field, String tooLongValue, int max) {
        return invalidField(
                "Invalid %s: too long".formatted(property), field, tooLongValue, size(property, tooLongValue, 0, max));
    }
}
