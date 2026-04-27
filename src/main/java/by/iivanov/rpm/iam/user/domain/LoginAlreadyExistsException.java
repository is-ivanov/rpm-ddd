package by.iivanov.rpm.iam.user.domain;

public class LoginAlreadyExistsException extends RuntimeException {

    public LoginAlreadyExistsException(Login login) {
        super("Login already exists: " + login.login());
    }
}
