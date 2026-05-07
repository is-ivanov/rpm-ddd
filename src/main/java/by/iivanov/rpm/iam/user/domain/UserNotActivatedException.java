package by.iivanov.rpm.iam.user.domain;

public class UserNotActivatedException extends RuntimeException {

    public UserNotActivatedException(String message) {
        super(message);
    }
}
