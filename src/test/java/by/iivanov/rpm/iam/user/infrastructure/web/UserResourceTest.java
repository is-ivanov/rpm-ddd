package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.WebTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@WebTest
class UserResourceTest {

    private final UserApi userApi;

    UserResourceTest(UserApi userApi) {
        this.userApi = userApi;
    }

    @Nested
    @DisplayName("test POST '/users' endpoint")
    class RegisterUserTest {

        @Test
        void beanValidationTest_shouldReturn422AndProblemJson() {
            var response = userApi.registerUser("{}");
            response.assertBindingError("__files/iam/user/web/registerUser_beanValidation_out.json");
        }
    }
}
