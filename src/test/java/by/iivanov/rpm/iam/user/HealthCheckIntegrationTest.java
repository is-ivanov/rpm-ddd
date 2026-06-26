package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.ActuatorApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@Issue("214")
class HealthCheckIntegrationTest extends AbstractApplicationIntegrationTest {

    private final ActuatorApi actuatorApi;

    HealthCheckIntegrationTest(ActuatorApi actuatorApi) {
        this.actuatorApi = actuatorApi;
    }

    @Test
    @DisplayName("Anonymous health probe is permitted for the Render health check")
    void should_returnOk_when_anonymousRequestsHealth() {
        // GIVEN: no authenticated session
        // WHEN: an anonymous client requests the actuator health endpoint
        var response = actuatorApi.health();
        // THEN: 200 with the simple health payload that Render's probe expects
        response.assertOk("""
                {
                  "status": "UP"
                }
                """);
    }

    @Test
    @DisplayName("Other actuator endpoints stay closed to anonymous clients")
    void should_rejectWith401_when_anonymousRequestsClosedActuatorEndpoint() {
        // GIVEN: no authenticated session
        // WHEN: an anonymous client requests a non-health actuator endpoint
        var response = actuatorApi.env();
        // THEN: the request is rejected with 401 Unauthorized
        response.assertStatus(HttpStatus.UNAUTHORIZED);
    }
}
