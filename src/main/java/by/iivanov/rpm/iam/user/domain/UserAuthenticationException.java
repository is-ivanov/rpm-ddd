package by.iivanov.rpm.iam.user.domain;

@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
public class UserAuthenticationException extends RuntimeException {

    public UserAuthenticationException(String message) {
        super(message);
    }
}
