package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.AuthApi;
import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurrentUserInfoIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    CurrentUserInfoIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Authenticated user retrieves own info")
    void should_returnOwnUserInfo_when_authenticated() {
        // GIVEN: an authenticated user with ACTIVE status
        var adminSession = authSessionFactory.loginAsAdmin();
        // WHEN: the user requests their own info
        var response = authApi.me(adminSession);
        // THEN: the response status is 200 and contains user info
        response.assertOk("""
                {
                  "userId": "019b76da-a800-7000-a957-f5fb8061a532",
                  "login": "admin",
                  "email": "admin@localhost.com",
                  "firstName": "System",
                  "lastName": "System",
                  "status": "ACTIVE",
                  "timeZone": "UTC",
                  "roles": []
                }
                """);
    }
}
