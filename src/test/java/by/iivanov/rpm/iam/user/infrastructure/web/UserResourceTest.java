package by.iivanov.rpm.iam.user.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import by.iivanov.rpm.iam.user.application.UserRegistrationService;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.WebTest;
import by.iivanov.rpm.testing.api.AssertionResponse;
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
        @ExpectedToFail(withExceptions = AssertionError.class)
        @DisplayName("WHEN login already exists EXPECT 422 with a login field error")
        void should_return422WithLoginFieldError_when_loginAlreadyExists() {
            givenLoginAlreadyExists();

            var response = userApi.registerUser(validRegistrationRequest());

            assertLoginAlreadyExistsError(response);
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
                      "email": "alice@example.com"
                    }
                    """.formatted(EXISTING_LOGIN);
        }

        private void assertLoginAlreadyExistsError(AssertionResponse response) {
            response.assertStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                    .assertProblemJson()
                    .assertBodyMatches(
                            """
                            {
                              "status": 422,
                              "title": "Unprocessable Content",
                              "type": "https://www.rpm-ddd.my/problem/validation-failed",
                              "fieldErrors": [
                                {
                                  "code": "ALREADY_EXISTS",
                                  "property": "login",
                                  "message": "Login already exists",
                                  "rejectedValue": "%s",
                                  "path": "login"
                                }
                              ]
                            }
                            """.formatted(EXISTING_LOGIN), Option.IGNORING_EXTRA_FIELDS, Option.IGNORING_ARRAY_ORDER);
        }
    }
}
