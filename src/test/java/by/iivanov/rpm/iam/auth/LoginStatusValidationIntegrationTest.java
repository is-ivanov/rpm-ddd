package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginStatusValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String PENDING_USER_LOGIN = "pending_user";
    private static final String PENDING_USER_PASSWORD = "Pending@123";
    private static final String LOCKED_USER_LOGIN = "locked_user";
    private static final String LOCKED_USER_PASSWORD = "Locked@123";

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    LoginStatusValidationIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Login with PENDING user returns 401 with activation message")
    void should_return401_when_loginWithPendingUser() {
        assertLoginRejected(
                PENDING_USER_LOGIN, PENDING_USER_PASSWORD, "Account not activated", "authentication-failed");
    }

    @Test
    @Disabled("TDD Red Phase - Not yet implemented")
    @DisplayName("Login with LOCKED user returns 401 with locked message")
    void should_return401_when_loginWithLockedUser() {
        assertLoginRejected(LOCKED_USER_LOGIN, LOCKED_USER_PASSWORD, "Account locked", "authentication-failed");
    }

    private void assertLoginRejected(String login, String password, String expectedDetail, String expectedType) {
        var csrfToken = authSessionFactory.getCsrfToken();
        String loginRequest = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(login, password);
        var response = authApi.login(loginRequest, csrfToken);
        response.unwrap().expectStatus().isUnauthorized().expectBody().json("""
                        {
                          "detail": "%s",
                          "instance": "/api/auth/login",
                          "status": 401,
                          "title": "Unauthorized",
                          "type": "https://www.rpm-ddd.my/problem/%s"
                        }
                        """.formatted(
                        expectedDetail, expectedType));
    }
}
