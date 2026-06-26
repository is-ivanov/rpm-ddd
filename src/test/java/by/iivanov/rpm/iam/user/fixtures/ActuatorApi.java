package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.testing.api.AbstractApi;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.WebApi;
import by.iivanov.rpm.testing.session.SessionContext;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Raw transport API for the Spring Boot Actuator {@code info} endpoint.
 *
 * <p>This bean stays singleton and transport-only. It exposes the shared {@code /actuator/info}
 * path so tests can request the deployed application version metadata, with or without an
 * authenticated session.
 */
@WebApi
public class ActuatorApi extends AbstractApi {

    private static final String BASE_URI = "/actuator/info";

    public ActuatorApi(RestTestClient restClient) {
        super(restClient);
    }

    public AssertionResponse info() {
        return get(BASE_URI);
    }

    public AssertionResponse info(SessionContext session) {
        return get(BASE_URI, session);
    }
}
