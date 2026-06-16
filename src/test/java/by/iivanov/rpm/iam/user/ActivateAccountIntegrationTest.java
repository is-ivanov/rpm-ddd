package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.user.fixtures.AuthApi;
import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.infrastructure.web.LoginRequest;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivateAccountIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;
    private final ActivationTokenFixture activationTokenFixture;

    ActivateAccountIntegrationTest(
            AuthApi authApi, AuthSessionFactory authSessionFactory, ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Activate with valid token and password succeeds")
    void should_return200AndUserCanLogin_when_validTokenAndPassword() {
        // GIVEN:
        var registration = activationTokenFixture.registerPendingUserWithToken();
        var csrfToken = authSessionFactory.getCsrfToken();
        String password = "Str0ng!Pass#9";
        // WHEN:
        var response = authApi.activate("""
            {
              "token": "%s",
              "password": "%s"
            }
            """.formatted(registration.token(), password), csrfToken);
        // THEN:
        response.assertOk();
        //  AND: user now activated and can log in with this password
        authApi.login(new LoginRequest(registration.login(), password), csrfToken)
                .assertOk()
                .unwrap()
                .expectCookie()
                .exists("JSESSIONID");
    }
}
