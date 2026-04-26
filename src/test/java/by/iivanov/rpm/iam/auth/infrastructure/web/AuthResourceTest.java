package by.iivanov.rpm.iam.auth.infrastructure.web;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.testing.WebTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@WebTest
class AuthResourceTest {

    private final AuthApi authApi;

    AuthResourceTest(AuthApi authApi) {
        this.authApi = authApi;
    }

    @Nested
    @DisplayName("test POST '/auth/login' endpoint")
    class LoginTest {

        @Test
        @DisplayName("WHEN empty body EXPECT 422 with field errors")
        void beanValidationTest_shouldReturn422() {
            var response = authApi.login("{}");
            response.assertBindingError("__files/iam/auth/web/login_beanValidation_out.json");
        }
    }
}
