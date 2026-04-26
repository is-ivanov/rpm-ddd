package by.iivanov.rpm.iam.auth.fixtures;

import by.iivanov.rpm.testing.api.AbstractApi;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.WebApi;
import by.iivanov.rpm.testing.session.SessionContext;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Raw transport API for {@code /api/auth}.
 *
 * <p>This bean stays singleton and transport-only. Session orchestration lives in
 * {@link AuthSessionFactory}; tests can use this class directly when they want the HTTP flow to
 * stay explicit.
 */
@WebApi
public class AuthApi extends AbstractApi {

    private static final String BASE_URI = "/api/auth";

    public AuthApi(RestTestClient restClient) {
        super(restClient);
    }

    //  ==== csrf ====

    private String csrfUri() {
        return BASE_URI + "/csrf";
    }

    public AssertionResponse csrf() {
        return get(csrfUri());
    }

    //  ==== login ====

    private String loginUri() {
        return BASE_URI + "/login";
    }

    public AssertionResponse login(String jsonBody, SessionContext sessionContext) {
        return post(loginUri(), jsonBody, sessionContext);
    }

    public AssertionResponse login(String jsonBody) {
        return post(loginUri(), jsonBody);
    }

    public AssertionResponse login(String jsonBody, String csrfToken) {
        return post(loginUri(), jsonBody, csrfToken);
    }
}
