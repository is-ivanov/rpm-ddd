package by.iivanov.rpm.iam.auth;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import by.iivanov.rpm.testing.api.AssertionResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class LoginStatusValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String PENDING_USER_LOGIN = "pending_user";
    private static final String PENDING_USER_PASSWORD = "Pending@123";
    private static final String LOCKED_USER_LOGIN = "locked_user";
    private static final String LOCKED_USER_PASSWORD = "Locked@123";
    private static final String INACTIVE_USER_LOGIN = "inactive_user";
    private static final String INACTIVE_USER_PASSWORD = "Inactive@123";

    private static final String UNAUTHORIZED_PROBLEM = """
            {
              "detail": "%s",
              "instance": "/api/auth/login",
              "status": 401,
              "title": "Unauthorized",
              "type": "https://www.rpm-ddd.my/problem/authentication-failed"
            }
            """;

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    LoginStatusValidationIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    static Stream<Arguments> nonActiveUsers() {
        return Stream.of(
                argumentSet("PENDING user", PENDING_USER_LOGIN, PENDING_USER_PASSWORD, "Account not activated"),
                argumentSet("LOCKED user", LOCKED_USER_LOGIN, LOCKED_USER_PASSWORD, "Account locked"),
                argumentSet("INACTIVE user", INACTIVE_USER_LOGIN, INACTIVE_USER_PASSWORD, "Account deactivated"));
    }

    @ParameterizedTest
    @MethodSource("nonActiveUsers")
    @DisplayName("Login with a non-ACTIVE user returns 401 with the account-status message")
    void should_return401_when_loginWithNonActiveUser(String login, String password, String expectedDetail) {
        // given a CSRF token for the login request
        var csrfToken = authSessionFactory.getCsrfToken();

        // when logging in with the non-ACTIVE user's credentials
        var response = authApi.login(login, password, csrfToken);

        // then the login is rejected as 401 with the status-specific message
        assertRejectedAsUnauthorized(response, expectedDetail);
    }

    private void assertRejectedAsUnauthorized(AssertionResponse response, String expectedDetail) {
        response.assertStatus(HttpStatus.UNAUTHORIZED)
                .assertProblemJson()
                .assertBodyMatches(UNAUTHORIZED_PROBLEM.formatted(expectedDetail));
    }
}
