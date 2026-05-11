package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginStatusValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String PENDING_USER_LOGIN = "pending_user";
    private static final String PENDING_USER_PASSWORD = "Pending@123";

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    LoginStatusValidationIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Login with PENDING user returns 401 with activation message")
    void should_return401_when_loginWithPendingUser() {
        var csrfToken = authSessionFactory.getCsrfToken();

        @Language("JSON")
        String loginRequest = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(PENDING_USER_LOGIN, PENDING_USER_PASSWORD);

        var response = authApi.login(loginRequest, csrfToken);

        response.unwrap().expectStatus().isUnauthorized().expectBody().json("""
                        {
                          "detail": "Account not activated",
                          "instance": "/api/auth/login",
                          "status": 401,
                          "title": "Unauthorized",
                          "type": "https://www.rpm-ddd.my/problem/account-not-activated"
                        }
                        """);
    }
}
