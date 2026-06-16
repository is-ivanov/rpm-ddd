package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ActivateAccountCsrfIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final ActivationTokenFixture activationTokenFixture;

    ActivateAccountCsrfIntegrationTest(AuthApi authApi, ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Activate endpoint rejects request missing CSRF token: WHEN POST /api/auth/activate"
            + " without a CSRF token EXPECT 403 RFC-9457 ProblemDetail body")
    void should_return403ProblemDetail_when_activateWithoutCsrfToken() {
        // GIVEN: a registered PENDING user with a valid activation token
        var registration = activationTokenFixture.registerPendingUserWithToken();

        @Language("JSON")
        String activateRequest = """
                {
                  "token": "%s",
                  "password": "Str0ng!Pass#9"
                }
                """.formatted(registration.token());

        // WHEN: client posts to /api/auth/activate without a CSRF token
        var response = authApi.activate(activateRequest);

        // THEN: 403 with an RFC-9457 ProblemDetail body, not the legacy {code,message} shape
        response.assertStatus(HttpStatus.FORBIDDEN).assertProblemJson().assertBodyMatches("""
                {
                  "detail": "Access denied: a valid CSRF token is required for this request.",
                  "instance": "/api/auth/activate",
                  "status": 403,
                  "title": "Forbidden",
                  "type": "https://www.rpm-ddd.my/problem/access-denied"
                }
                """);
    }
}
