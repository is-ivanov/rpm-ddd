package by.iivanov.rpm.iam.user.domain;

@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
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
