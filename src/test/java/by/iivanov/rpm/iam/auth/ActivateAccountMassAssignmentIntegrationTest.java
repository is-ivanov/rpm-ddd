package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.ActivationTokenFixture;
import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivateAccountMassAssignmentIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String PASSWORD = "Str0ng!Pass#9";

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;
    private final ActivationTokenFixture activationTokenFixture;

    ActivateAccountMassAssignmentIntegrationTest(
            AuthApi authApi, AuthSessionFactory authSessionFactory, ActivationTokenFixture activationTokenFixture) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
        this.activationTokenFixture = activationTokenFixture;
    }

    @Test
    @DisplayName("Activate request with extra fields does not modify user state beyond activation: "
            + "WHEN POST /api/auth/activate carries extra \"role\":\"ADMIN\" and \"status\":\"LOCKED\" fields "
            + "EXPECT 200, activation from normal flow (status ACTIVE, login works), role not elevated")
    void should_ignoreExtraFields_when_activateRequestCarriesRoleAndStatus() {
        // GIVEN: a registered PENDING user with a valid activation token
        var registration = activationTokenFixture.registerPendingUserWithToken();
        var csrfToken = authSessionFactory.getCsrfToken();

        // Inject "status":"LOCKED" — a value the normal activation flow NEVER produces.
        // If Jackson mass-assigned it, the user would be LOCKED and the login below would fail
        // with "Account locked". A successful login proving status ACTIVE therefore proves the
        // injected status was ignored and the ACTIVE state came from the activation flow alone.
        @Language("JSON")
        String activateRequest = """
                {
                  "token": "%s",
                  "password": "%s",
                  "role": "ADMIN",
                  "status": "LOCKED"
                }
                """.formatted(registration.token(), PASSWORD);

        // WHEN: the client activates with extra "role" and "status" JSON fields
        var response = authApi.activate(activateRequest, csrfToken);

        // THEN: activation succeeds with 200 (extra fields silently ignored)
        response.assertOk();

        // AND: the user can log in (proves status is ACTIVE, not the injected LOCKED) and every
        // /me field matches the normal flow — role was not elevated (roles stays empty).
        var session = authSessionFactory.loginAs(registration.login(), PASSWORD);
        authApi.me(session).assertOk("""
                {
                  "userId": "%s",
                  "login": "%s",
                  "email": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "status": "ACTIVE",
                  "roles": []
                }
                """.formatted(
                        registration.userId().id(),
                        registration.login(),
                        registration.email(),
                        registration.firstName(),
                        registration.lastName()));
    }
}
