package by.iivanov.rpm.iam.user.domain;

@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
public class LoginAlreadyExistsException extends RuntimeException {

    private final String login;

    public LoginAlreadyExistsException(Login login) {
        super("Login already exists: " + login.login());
        this.login = login.login();
    }

    /**
     * The login value that already exists, for surfacing as a field error.
     */
    public String login() {
        return login;
    }
}
