package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Surfaces a duplicate-login domain violation as a field-level validation error,
 * matching the bean-validation response shape (422, type validation-failed, fieldErrors).
 */
@Component
class LoginAlreadyExistsExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof LoginAlreadyExistsException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        String login = ((LoginAlreadyExistsException) exception).login();
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "VALIDATION_FAILED",
                "Validation failed for object='registerUserRequest'. Error count: 1");
        response.addFieldError(new ApiFieldError("ALREADY_EXISTS", "login", "Login already exists", login, "login"));
        return response;
    }
}
