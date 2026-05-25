package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("TDD Red Phase - Not yet implemented")
class ActivateAccountValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;
    private final ActivationTokenFixture activationTokenFixture;

    ActivateAccountValidationIntegrationTest(
            AuthApi authApi, AuthSessionFactory authSessionFactory, ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Activate with password violating policy returns validation errors")
    void should_return422WithValidationErrors_when_passwordViolatesPolicy() {
        var registration = activationTokenFixture.registerPendingUserWithToken();
        var csrfToken = authSessionFactory.getCsrfToken();

        var response = authApi.activate(weakPasswordRequest(registration.token()), csrfToken);

        response.assertBindingError("""
                {
                  "detail": "Validation failed for object='activateAccountRequest'. Error count: 1",
                  "instance": "/api/auth/activate",
                  "status": 422,
                  "title": "Unprocessable Content",
                  "type": "https://www.rpm-ddd.my/problem/validation-failed",
                  "fieldErrors": [
                    {
                      "code": "SIZE",
                      "property": "password",
                      "message": "size must be between 12 and 128",
                      "rejectedValue": "weak",
                      "path": "password"
                    }
                  ]
                }
                """);
    }

    private static String weakPasswordRequest(String token) {
        return """
                {
                  "token": "%s",
                  "password": "weak"
                }
                """.formatted(token);
    }
}
