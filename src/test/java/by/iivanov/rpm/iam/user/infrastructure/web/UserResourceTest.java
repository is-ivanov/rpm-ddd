package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import by.iivanov.rpm.iam.user.application.UserRegistrationService;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.WebTest;
import by.iivanov.rpm.testing.api.FieldError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@WebTest
@Execution(ExecutionMode.SAME_THREAD)
class UserResourceTest {

    private final UserApi userApi;
    private final UserRegistrationService userRegistrationService;

    UserResourceTest(UserApi userApi, UserRegistrationService userRegistrationService) {
        this.userApi = userApi;
        this.userRegistrationService = userRegistrationService;
    }

    @Nested
    @DisplayName("test POST '/users' endpoint")
    class RegisterUserTest {

        private static final String EXISTING_LOGIN = "alice";

        @Test
        void beanValidationTest_shouldReturn422AndProblemJson() {
            var response = userApi.registerUser("{}");
            response.assertBindingError("__files/iam/user/web/registerUser_beanValidation_out.json");
        }

        @Test
        @DisplayName("WHEN login already exists EXPECT 422 with a login field error")
        void should_return422WithLoginFieldError_when_loginAlreadyExists() {
            givenLoginAlreadyExists();

            var response = userApi.registerUser(validRegistrationRequest());

            response.assertBindingError(FieldError.builder()
                    .code("ALREADY_EXISTS")
                    .property("login")
                    .message("Login already exists")
                    .rejectedValue(EXISTING_LOGIN)
                    .path("login"));
        }

        private void givenLoginAlreadyExists() {
            given(userRegistrationService.registerUser(any(), any()))
                    .willThrow(new LoginAlreadyExistsException(new Login(EXISTING_LOGIN)));
        }

        private String validRegistrationRequest() {
            return """
                    {
                      "firstName": "Alice",
                      "lastName": "Anderson",
                      "login": "%s",
                      "email": "alice@example.com",
                      "timeZone": "UTC"
                    }
                    """.formatted(EXISTING_LOGIN);
        }
    }
}
