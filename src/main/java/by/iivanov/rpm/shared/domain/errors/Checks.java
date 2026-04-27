package by.iivanov.rpm.shared.domain.errors;

import org.jspecify.annotations.Nullable;

public final class Checks {

    private Checks() {}

    /**
     * Validates that the given object is not null.
     *
     * @param <T>     the type of the object
     * @param object  the object to check
     * @param message the error message if the object is null
     * @return the object if not null
     * @throws DomainValidationException if the object is null
     */
    public static <T> T notNull(@Nullable T object, String message) {
        if (object == null) {
            throw new DomainValidationException(message);
        }
        return object;
    }

    /**
     * Validates that the given string is not null and not blank.
     *
     * @param value   the string to check for null or blank
     * @param message the error message if the string is null or blank
     * @return the string if it is not null and not blank
     * @throws DomainValidationException if the string is null or blank
     */
    public static String notBlank(@Nullable String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
        return value;
    }

    /**
     * Validates that the given string does not exceed the specified maximum length.
     *
     * @param value     the string to check
     * @param maxLength the maximum allowed length for the string
     * @param fieldName the name of the field being validated, used in the error message
     * @return the original string if it does not exceed the maximum length
     * @throws DomainValidationException if the string exceeds the specified maximum length
     */
    public static String maxLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new DomainValidationException(
                    "%s must not exceed %d characters, but was %d".formatted(fieldName, maxLength, value.length()));
        }
        return value;
    }

    /**
     * Validates that the given string is exactly the specified length.
     *
     * @param value          the string to validate
     * @param expectedLength the exact length the string must match
     * @param fieldName      the name of the field being validated, used in the error message
     * @return the original string if it matches the expected length
     * @throws DomainValidationException if the string does not match the expected length
     */
    public static String exactlyLength(String value, int expectedLength, String fieldName) {
        if (value.length() != expectedLength) {
            throw new DomainValidationException("%s must be exactly %d characters, but was %d"
                    .formatted(fieldName, expectedLength, value.length()));
        }
        return value;
    }
}
