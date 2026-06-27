package by.iivanov.rpm.iam.user.domain;

public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(EmailAddress email) {
        super("Email already exists: " + email.email());
        this.email = email.email();
    }

    /**
     * The email value that already exists, for surfacing as a field error.
     */
    public String email() {
        return email;
    }
}
