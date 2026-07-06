package by.iivanov.rpm.iam.user.infrastructure.web;

import static by.iivanov.rpm.iam.user.fixtures.UserBuilder.aUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import by.iivanov.rpm.iam.user.application.ActivationService;
import by.iivanov.rpm.iam.user.fixtures.AuthApi;
import by.iivanov.rpm.testing.WebTest;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.FieldError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@WebTest
class AuthResourceTest {

    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    private static final String EXPIRED_TOKEN = "expired-token";
    private static final String JWT_EXPIRED = "JWT expired";
    private static final String EXPIRED_DETAIL = "Activation token has expired";
    private static final String TOKEN_FIELD = "token";
    private static final String PASSWORD_FIELD = "password";
    private static final String VALID_ACTIVATE_REQUEST = """
            {
              "token": "some.jwt.token",
              "password": "ValidPass12!@"
            }
            """;

    private final AuthApi authApi;
    private final ActivationService activationService;

    AuthResourceTest(AuthApi authApi, ActivationService activationService) {
        this.authApi = authApi;
        this.activationService = activationService;
    }

    private void assertUnprocessable(AssertionResponse response, String detail) {
        response.assertStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        response.assertBodyMatches("""
                {
                  "status": 422,
                  "title": "Unprocessable Content",
                  "detail": "%s",
                  "instance": "/api/auth/activate"
                }
                """.formatted(detail), Option.IGNORING_EXTRA_FIELDS);
    }

    @Nested
    @DisplayName("test POST '/auth/login' endpoint")
    class LoginTest {

        @Test
        @DisplayName("WHEN empty body EXPECT 422 with field errors")
        void should_return422WithFieldErrors_when_emptyBody() {
            var response = authApi.login("{}");
            response.assertBindingError("__files/iam/user/web/login_beanValidation_out.json");
        }
    }

    @Nested
    @DisplayName("test GET '/auth/activate' endpoint")
    class ValidateActivationTokenTest {

        @Test
        @DisplayName("Valid activation token returns user info")
        void should_returnUserInfo_when_validActivationToken() {
            givenUserWithActivationToken();

            var response = authApi.validateActivationToken(VALID_TOKEN);

            response.assertOk("""
                {
                    "login": "testuser",
                    "email": "test@example.com"
                }
                """);
        }

        private void givenUserWithActivationToken() {
            var user =
                    aUser().withLogin("testuser").withEmail("test@example.com").build();
            given(activationService.validateToken(VALID_TOKEN)).willReturn(user);
        }

        @Test
        @DisplayName("Invalid activation token returns 422")
        void should_return422_when_invalidActivationToken() {
            givenTokenValidationFails(INVALID_TOKEN, new MalformedJwtException(INVALID_TOKEN_MESSAGE));
            var response = authApi.validateActivationToken(INVALID_TOKEN);
            assertUnprocessable(response, INVALID_TOKEN_MESSAGE);
        }

        @Test
        @DisplayName("Expired activation token returns 422")
        void should_return422_when_expiredActivationToken() {
            givenTokenValidationFails(EXPIRED_TOKEN, new ExpiredJwtException(null, null, JWT_EXPIRED));
            var response = authApi.validateActivationToken(EXPIRED_TOKEN);
            assertUnprocessable(response, EXPIRED_DETAIL);
        }

        private void givenTokenValidationFails(String token, RuntimeException exception) {
            given(activationService.validateToken(token)).willThrow(exception);
        }
    }

    @Nested
    @DisplayName("test POST '/auth/activate' endpoint")
    class ActivateAccountTest {

        @Test
        @DisplayName("WHEN password too short and token is blank EXPECT 422 with SIZE validation errors")
        void should_return422WithSizeError_when_requestInvalid() {
            // GIVEN: short password
            String token = "   ";
            String password = "weak";
            // WHEN:
            var response = authApi.activate("""
                {
                  "token": "%s",
                  "password": "%s"
                }
                """.formatted(token, password));
            // THEN:
            response.assertBindingError(
                    FieldError.notBlank()
                            .property(TOKEN_FIELD)
                            .message("must not be blank")
                            .rejectedValue(null)
                            .path(TOKEN_FIELD),
                    FieldError.size()
                            .property(PASSWORD_FIELD)
                            .message("size must be between 12 and 128")
                            .rejectedValue(password)
                            .path(PASSWORD_FIELD));
        }

        @Test
        @DisplayName("Tampered activation token (bad signature) returns 422 with Invalid activation token")
        void should_return422_when_tamperedActivationToken() {
            givenActivationFailsSignature();

            var response = authApi.activate(VALID_ACTIVATE_REQUEST);

            assertUnprocessable(response, "Invalid activation token");
        }

        private void givenActivationFailsSignature() {
            willThrow(new SignatureException("JWT signature does not match"))
                    .given(activationService)
                    .activate(anyString(), anyString());
        }

        @Test
        @DisplayName("Expired activation token returns 422 with Activation token has expired")
        void should_return422_when_expiredActivationToken() {
            givenActivationFailsExpired();

            var response = authApi.activate(VALID_ACTIVATE_REQUEST);

            assertUnprocessable(response, EXPIRED_DETAIL);
        }

        private void givenActivationFailsExpired() {
            willThrow(new ExpiredJwtException(null, null, JWT_EXPIRED))
                    .given(activationService)
                    .activate(anyString(), anyString());
        }
    }
}
