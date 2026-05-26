package by.iivanov.rpm.iam.auth.infrastructure.web;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.user.application.ActivationService;
import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.testing.WebTest;
import by.iivanov.rpm.testing.api.AssertionResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import net.javacrumbs.jsonunit.core.Option;
import org.instancio.Instancio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpStatus;

@WebTest
class AuthResourceTest {

    private final AuthApi authApi;
    private final ActivationService activationService;

    AuthResourceTest(AuthApi authApi, ActivationService activationService) {
        this.authApi = authApi;
        this.activationService = activationService;
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
            var user = Instancio.of(User.class)
                    .set(field(User::getLogin), new Login("testuser"))
                    .set(field(User::getEmail), new EmailAddress("test@example.com"))
                    .create();
            given(activationService.validateToken(eq("valid-token"))).willReturn(user);
        }

        private void assertUnprocessableContent(AssertionResponse response) {
            response.assertStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            response.assertBodyMatches("""
                    {
                      "status": 422,
                      "detail": "${json-unit.any-string}",
                      "instance": "/api/auth/activate"
                    }
                    """, Option.IGNORING_EXTRA_FIELDS);
        }

        @Test
        @DisplayName("Invalid activation token returns 422")
        void should_return422_when_invalidActivationToken() {
            given(activationService.validateToken(eq("invalid-token")))
                    .willThrow(new MalformedJwtException("Invalid token"));

            var response = authApi.validateActivationToken("invalid-token");

            assertUnprocessableContent(response);
        }

        @Test
        @DisplayName("Expired activation token returns 422")
        void should_return422_when_expiredActivationToken() {
            given(activationService.validateToken(eq("expired-token")))
                    .willThrow(new ExpiredJwtException(null, null, "Token expired"));

            var response = authApi.validateActivationToken("expired-token");

            assertUnprocessableContent(response);
        }
    }

    @Nested
    @DisplayName("test POST '/auth/activate' endpoint")
    class ActivateAccountTest {

        @Test
        @Disabled("TDD Red Phase — @Size annotation not yet on ActivateAccountRequest.password")
        @DisplayName("WHEN password too short EXPECT 422 with SIZE validation error")
        void should_return422WithSizeError_when_passwordTooShort() {
            var response = authApi.activate(weakPasswordRequest());

            response.assertBindingError("""
                    {
                      "detail": "Validation failed for object='activateAccountRequest'. Error count: 1",
                      "instance": "/api/auth/activate",
                      "status": 422,
                      "title": "Unprocessable Content",
                      "type": "https://www.rpm-ddd.my/problem/validation-failed",
                      "fieldErrors": [
                    {
                      "code": "SIZE",
                      "property": "password",
                      "message": "size must be between 12 and 128",
                      "rejectedValue": "weak",
                      "path": "password"
                    }
                  ]
                    }
                    """);
        }

        private static String weakPasswordRequest() {
            return """
                    {
                      "token": "some-valid-token",
                      "password": "weak"
                    }
                    """;
        }
    }
}
