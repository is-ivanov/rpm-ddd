package by.iivanov.rpm.iam.user.domain;

import java.util.List;

/**
 * Thrown when a password does not meet complexity requirements.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(List<String> violations) {
        super("Password validation failed: " + violations);
    }
}
