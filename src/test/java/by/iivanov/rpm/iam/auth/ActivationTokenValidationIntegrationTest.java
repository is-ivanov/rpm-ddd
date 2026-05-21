package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("TDD Red Phase - Not yet implemented")
class ActivationTokenValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final ActivationTokenFixture activationTokenFixture;

    ActivationTokenValidationIntegrationTest(
            AuthApi authApi,
            AuthSessionFactory authSessionFactory,
            UserApi userApi,
            ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Valid activation token returns user info")
    void should_returnUserInfo_when_validActivationToken() {
        // GIVEN: a pending user with a valid activation token
        var admin = authSessionFactory.loginAsAdmin();
        var uniqueSuffix = UUID.randomUUID().toString();
        var login = "activate_user_" + uniqueSuffix;
        var email = login + "@example.com";
        var request = new RegisterUserRequest("Test", null, "User", login, email);
        var registerResponse = userApi.registerUser(request, admin);
        var userId = registerResponse.extractCreatedId("/api/admin/users/");
        var userIdObj = new UserId(UUID.fromString(userId));
        var token = activationTokenFixture.generateValidToken(userIdObj);

        // WHEN: the activation token is validated
        var response = authApi.validateActivationToken(token);

        // THEN: the response status is 200
        // AND the response contains the user's login and email
        response.assertOk("""
                {
                  "login": "%s",
                  "email": "%s"
                }
                """.formatted(login, email));
    }
}
