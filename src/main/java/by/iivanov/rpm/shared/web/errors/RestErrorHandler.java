package by.iivanov.rpm.shared.web.errors;

import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
class RestErrorHandler extends ResponseEntityExceptionHandler {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation errors: {}", ex.getMessage());
        var errorStatus = HttpStatus.UNPROCESSABLE_CONTENT;
        var body = createProblemDetail(ex, errorStatus, "One or more fields are invalid", null, null, request);
        body.setType(getMappedType(ex));
        body.setTitle("Validation failed");
        body.setProperty(FIELD_ERRORS_KEY, getFieldErrors(ex));
        return handleExceptionInternal(ex, body, headers, errorStatus, request);
    }

    private @Unmodifiable List<FieldErrorDto> getFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String objectName = error.getObjectName().replaceFirst("DTO$", "");
                    String message = StringUtils.isNotBlank(error.getDefaultMessage())
                            ? error.getDefaultMessage()
                            : error.getCode();
                    return new FieldErrorDto(objectName, error.getField(), message);
                })
                .toList();
    }

    private URI getMappedType(Throwable err) {
        return switch (err) {
            case MethodArgumentNotValidException _ -> ErrorConstants.CONSTRAINT_VIOLATION_TYPE;
            default -> ErrorConstants.DEFAULT_TYPE;
        };
    }
}
