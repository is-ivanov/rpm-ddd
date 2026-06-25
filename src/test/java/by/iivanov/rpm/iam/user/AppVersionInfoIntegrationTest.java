package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.ActuatorApi;
import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AppVersionInfoIntegrationTest extends AbstractApplicationIntegrationTest {

    private final ActuatorApi actuatorApi;
    private final AuthSessionFactory authSessionFactory;

    AppVersionInfoIntegrationTest(ActuatorApi actuatorApi, AuthSessionFactory authSessionFactory) {
        this.actuatorApi = actuatorApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Authenticated user retrieves deployed app version info")
    void should_returnAppVersionInfo_when_authenticated() {
        // GIVEN: an authenticated admin user
        var adminSession = authSessionFactory.loginAsAdmin();
        // WHEN: the user requests the actuator info endpoint
        var response = actuatorApi.info(adminSession);
        // THEN: 200 with the full info payload — build metadata and git commit (simple mode)
        response.assertOk("""
                {
                  "git": {
                    "branch": "${json-unit.any-string}",
                    "commit": {
                      "id": "${json-unit.any-string}",
                      "time": "${json-unit.any-string}"
                    }
                  },
                  "build": {
                    "artifact": "rpm-ddd",
                    "name": "rpm-ddd",
                    "time": "${json-unit.any-string}",
                    "version": "0.0.1-SNAPSHOT",
                    "group": "by.iivanov.rpm"
                  }
                }
                """);
    }

    @Test
    @DisplayName("Anonymous request for app version info is rejected")
    void should_rejectWith401_when_anonymous() {
        // GIVEN: no authenticated session
        // WHEN: an anonymous client requests the actuator info endpoint
        var response = actuatorApi.info();
        // THEN: the request is rejected with 401 Unauthorized
        response.assertStatus(HttpStatus.UNAUTHORIZED);
    }
}
