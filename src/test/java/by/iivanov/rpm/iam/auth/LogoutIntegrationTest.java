package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@Disabled("TDD Red Phase - Not yet implemented")
class LogoutIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    LogoutIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Logout invalidates session")
    void should_invalidateSession_when_userLogsOut() {
        // GIVEN: an authenticated user with an active session
        var session = authSessionFactory.loginAsAdmin();

        // WHEN: the user logs out
        // THEN: the response status is 200
        authApi.logout(session).assertOk();

        // AND: subsequent requests with the same session cookie return 401
        authApi.me(session).assertStatus(HttpStatus.UNAUTHORIZED);
    }
}
