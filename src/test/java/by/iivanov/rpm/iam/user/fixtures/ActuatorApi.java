package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.testing.api.AbstractApi;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.WebApi;
import by.iivanov.rpm.testing.session.SessionContext;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Raw transport API for the Spring Boot Actuator endpoints.
 */
@WebApi
public class ActuatorApi extends AbstractApi {

    private static final String INFO_URI = "/actuator/info";
    private static final String HEALTH_URI = "/actuator/health";
    private static final String ENV_URI = "/actuator/env";

    public ActuatorApi(RestTestClient restClient) {
        super(restClient);
    }

    public AssertionResponse info() {
        return get(INFO_URI);
    }

    public AssertionResponse info(SessionContext session) {
        return get(INFO_URI, session);
    }

    public AssertionResponse health() {
        return get(HEALTH_URI);
    }

    public AssertionResponse env() {
        return get(ENV_URI);
    }
}
