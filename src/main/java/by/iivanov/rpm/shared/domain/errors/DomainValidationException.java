package by.iivanov.rpm.shared.domain.errors;

/**
 * Thrown when a value object validation fails.
 */
@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
