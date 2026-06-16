package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.user.fixtures.AuthApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivationTokenValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final ActivationTokenFixture activationTokenFixture;

    ActivationTokenValidationIntegrationTest(AuthApi authApi, ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Valid activation token returns user info")
    void should_returnUserInfo_when_validActivationToken() {
        var registration = activationTokenFixture.registerPendingUserWithToken();

        var response = authApi.validateActivationToken(registration.token());

        response.assertOk("""
                {
                  "login": "%s",
                  "email": "%s"
                }
                """.formatted(registration.login(), registration.email()));
    }
}
