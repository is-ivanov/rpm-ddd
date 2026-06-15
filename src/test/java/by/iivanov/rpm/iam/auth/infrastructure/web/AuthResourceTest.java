package by.iivanov.rpm.iam.auth.infrastructure.web;

import static by.iivanov.rpm.iam.user.fixtures.UserBuilder.aUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.user.application.ActivationService;
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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.http.HttpStatus;

@WebTest
@Execution(ExecutionMode.SAME_THREAD)
class AuthResourceTest {

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
            response.assertBindingError("__files/iam/auth/web/login_beanValidation_out.json");
        }
    }

    @Nested
    @DisplayName("test GET '/auth/activate' endpoint")
    @Execution(ExecutionMode.SAME_THREAD)
    class ValidateActivationTokenTest {

        @Test
        @DisplayName("Valid activation token returns user info")
        void should_returnUserInfo_when_validActivationToken() {
            givenUserWithActivationToken();

            var response = authApi.validateActivationToken("valid-token");

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
            given(activationService.validateToken("valid-token")).willReturn(user);
        }

        @Test
        @DisplayName("Invalid activation token returns 422")
        void should_return422_when_invalidActivationToken() {
            givenTokenValidationFails("invalid-token", new MalformedJwtException("Invalid token"));
            var response = authApi.validateActivationToken("invalid-token");
            assertUnprocessable(response, "Invalid token");
        }

        @Test
        @DisplayName("Expired activation token returns 422")
        void should_return422_when_expiredActivationToken() {
            givenTokenValidationFails("expired-token", new ExpiredJwtException(null, null, "Token expired"));
            var response = authApi.validateActivationToken("expired-token");
            assertUnprocessable(response, "Token expired");
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
                            .property("token")
                            .message("must not be blank")
                            .rejectedValue(null)
                            .path("token"),
                    FieldError.size()
                            .property("password")
                            .message("size must be between 12 and 128")
                            .rejectedValue(password)
                            .path("password"));
        }

        @Test
        @DisplayName("Tampered activation token (bad signature) returns 422 with Invalid activation token")
        void should_return422_when_tamperedActivationToken() {
            givenActivationFailsSignature();

            var response = authApi.activate("""
                {
                  "token": "some.jwt.token",
                  "password": "ValidPass12!@"
                }
                """);

            assertUnprocessable(response, "Invalid activation token");
        }

        private void givenActivationFailsSignature() {
            willThrow(new SignatureException("JWT signature does not match"))
                    .given(activationService)
                    .activate(anyString(), anyString());
        }

        @Test
        @DisplayName("Expired activation token returns 422 with Activation token has expired")
        @ExpectedToFail(
                value = "Security 5.5: no messages override for ExpiredJwtException; detail is raw jjwt message",
                withExceptions = AssertionError.class)
        void should_return422_when_expiredActivationToken() {
            givenActivationFailsExpired();

            var response = authApi.activate("""
                {
                  "token": "some.jwt.token",
                  "password": "ValidPass12!@"
                }
                """);

            assertUnprocessable(response, "Activation token has expired");
        }

        private void givenActivationFailsExpired() {
            willThrow(new ExpiredJwtException(null, null, "JWT expired"))
                    .given(activationService)
                    .activate(anyString(), anyString());
        }
    }
}
