package by.iivanov.rpm.iam.user.domain;

import java.util.List;

/**
 * Thrown when a password does not meet complexity requirements.
 */
@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(List<String> violations) {
        super("Password validation failed: " + violations);
    }
}
