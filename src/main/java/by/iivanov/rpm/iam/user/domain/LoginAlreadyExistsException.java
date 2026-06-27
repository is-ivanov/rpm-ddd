package by.iivanov.rpm.iam.user.domain;

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
