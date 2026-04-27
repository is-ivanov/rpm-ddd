package by.iivanov.rpm.testing.assertj;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.ConstraintViolationAssert.ViolationExpectation;

/**
 * AssertJ assertions for {@link ConstraintViolationException}.
 *
 * <pre>{@code
 * assertThat(throwable)
 *     .hasViolationForProperty("login")
 *     .withConstraint(NotBlank.class)
 *     .withMessage("must not be blank");
 * }</pre>
 */
public class ConstraintViolationExceptionAssert
        extends AbstractThrowableAssert<ConstraintViolationExceptionAssert, ConstraintViolationException> {

    protected ConstraintViolationExceptionAssert(ConstraintViolationException actual) {
        super(actual, ConstraintViolationExceptionAssert.class);
    }

    public static ConstraintViolationExceptionAssert assertThat(ConstraintViolationException exception) {
        return new ConstraintViolationExceptionAssert(exception);
    }

    /**
     * Verifies that the exception contains only the expected violations for the given property.
     *
     * @param propertyName the property name to check
     * @param expectedViolations the expected violations
     * @return this assertion object
     */
    public ConstraintViolationExceptionAssert hasOnlyViolations(
            String propertyName, List<ViolationExpectation> expectedViolations) {
        isNotNull();
        Set<ConstraintViolation<?>> actualViolations = actual.getConstraintViolations();
        ConstraintViolationAssert.assertThat(actualViolations)
                .containsOnlyViolations(expectedViolations.toArray(new ViolationExpectation[0]));
        return this;
    }

    /**
     * Verifies that the exception contains only the expected violations.
     *
     * @param expectedViolations the expected violations
     * @return this assertion object
     */
    public ConstraintViolationExceptionAssert hasOnlyViolations(ViolationExpectation... expectedViolations) {
        isNotNull();
        Set<ConstraintViolation<?>> actualViolations = actual.getConstraintViolations();
        ConstraintViolationAssert.assertThat(actualViolations).containsOnlyViolations(expectedViolations);
        return this;
    }

    /**
     * Starts a fluent assertion chain for a violation on the given property.
     */
    public ViolationAssert hasViolationForProperty(String propertyName) {
        isNotNull();
        Set<ConstraintViolation<?>> violations = actual.getConstraintViolations();
        var matching = violations.stream()
                .filter(v -> propertyName.equals(extractPropertyName(v.getPropertyPath())))
                .collect(Collectors.toSet());

        if (matching.isEmpty()) {
            String availableProps = violations.stream()
                    .map(v -> extractPropertyName(v.getPropertyPath()))
                    .collect(Collectors.joining(", "));
            failWithMessage(
                    "Expected violation for property '%s' but found violations for: [%s]",
                    propertyName, availableProps);
        }

        if (matching.size() > 1) {
            failWithMessage(
                    "Expected exactly one violation for property '%s' but found %d", propertyName, matching.size());
        }

        return new ViolationAssert(matching.iterator().next());
    }

    /**
     * Verifies the exception has exactly the given number of violations.
     */
    public ConstraintViolationExceptionAssert hasViolationsCount(int count) {
        isNotNull();
        int actualCount = actual.getConstraintViolations().size();
        if (actualCount != count) {
            failWithMessage("Expected <%d> violations but was <%d>", count, actualCount);
        }
        return this;
    }

    private String extractPropertyName(Path propertyPath) {
        // propertyPath is like "value" for Login, "firstName" for PersonName, etc.
        String pathStr = propertyPath.toString();
        int lastDot = pathStr.lastIndexOf('.');
        return lastDot >= 0 ? pathStr.substring(lastDot + 1) : pathStr;
    }

    /**
     * Fluent assertions for a single constraint violation.
     */
    public static class ViolationAssert extends AbstractAssert<ViolationAssert, ConstraintViolation<?>> {

        private ViolationAssert(ConstraintViolation<?> actual) {
            super(actual, ViolationAssert.class);
        }

        /**
         * Verifies that the violation has the expected constraint type.
         *
         * @param constraintType the expected constraint annotation type
         * @return this assertion object
         */
        public ViolationAssert withConstraint(Class<? extends Annotation> constraintType) {
            isNotNull();
            Class<?> actualType =
                    actual.getConstraintDescriptor().getAnnotation().annotationType();
            Assertions.assertThat(actualType)
                    .as("constraint type for property '%s'", extractPropertyName(actual.getPropertyPath()))
                    .isEqualTo(constraintType);
            return this;
        }

        /**
         * Verifies that the violation message equals the expected message.
         *
         * @param expectedMessage the expected message
         * @return this assertion object
         */
        public ViolationAssert withMessage(String expectedMessage) {
            isNotNull();
            Assertions.assertThat(actual.getMessage())
                    .as("message for property '%s'", extractPropertyName(actual.getPropertyPath()))
                    .isEqualTo(expectedMessage);
            return this;
        }

        /**
         * Verifies that the violation message contains the expected substring.
         *
         * @param substring the substring to search for
         * @return this assertion object
         */
        public ViolationAssert withMessageContaining(String substring) {
            isNotNull();
            Assertions.assertThat(actual.getMessage())
                    .as("message for property '%s'", extractPropertyName(actual.getPropertyPath()))
                    .contains(substring);
            return this;
        }

        private String extractPropertyName(Path propertyPath) {
            String pathStr = propertyPath.toString();
            int lastDot = pathStr.lastIndexOf('.');
            return lastDot >= 0 ? pathStr.substring(lastDot + 1) : pathStr;
        }
    }
}
