package by.iivanov.rpm.shared.domain.errors;

/**
 * Thrown when a value object validation fails.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
