package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

import by.iivanov.rpm.iam.user.application.UserRegistrationService;
import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.EmailAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.WebTest;
import by.iivanov.rpm.testing.api.FieldError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@WebTest
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
        private static final String EXISTING_EMAIL = "alice@example.com";
        private static final String ALREADY_EXISTS = "ALREADY_EXISTS";
        private static final String LOGIN_FIELD = "login";
        private static final String EMAIL_FIELD = "email";

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
                    .code(ALREADY_EXISTS)
                    .property(LOGIN_FIELD)
                    .message("Login already exists")
                    .rejectedValue(EXISTING_LOGIN)
                    .path(LOGIN_FIELD));
        }

        @Test
        @DisplayName("Create with a duplicate email returns a field-level 422: "
                + "WHEN email already exists EXPECT 422 with an email field error")
        void should_return422WithEmailFieldError_when_emailAlreadyExists() {
            givenEmailAlreadyExists();

            var response = userApi.registerUser(validRegistrationRequest());

            response.assertBindingError(FieldError.builder()
                    .code(ALREADY_EXISTS)
                    .property(EMAIL_FIELD)
                    .message("Email already exists")
                    .rejectedValue(EXISTING_EMAIL)
                    .path(EMAIL_FIELD));
        }

        private void givenLoginAlreadyExists() {
            willThrow(new LoginAlreadyExistsException(new Login(EXISTING_LOGIN)))
                    .given(userRegistrationService)
                    .registerUser(any(), any());
        }

        private void givenEmailAlreadyExists() {
            willThrow(new EmailAlreadyExistsException(new EmailAddress(EXISTING_EMAIL)))
                    .given(userRegistrationService)
                    .registerUser(any(), any());
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
