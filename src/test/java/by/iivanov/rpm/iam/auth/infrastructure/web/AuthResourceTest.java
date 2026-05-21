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
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    }
}
