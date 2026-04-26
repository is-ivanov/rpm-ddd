package by.iivanov.rpm.iam.user.domain;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(EmailAddress email) {
        super("Email already exists: " + email.email());
    }
}
