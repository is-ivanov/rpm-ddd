package by.iivanov.rpm.iam.user.domain;

@SuppressWarnings("serial") // domain exceptions serialize to RFC 9457 JSON, never Java binary serialization
public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
