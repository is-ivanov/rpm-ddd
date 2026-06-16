package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.iam.user.infrastructure.web.LoginRequest;
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

    //  ==== current user info ====

    public AssertionResponse me(SessionContext session) {
        return get(BASE_URI + "/me", session);
    }

    //  ==== logout ====

    public AssertionResponse logout(SessionContext session) {
        return post(BASE_URI + "/logout", "", session);
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

    public AssertionResponse login(LoginRequest request, String csrfToken) {
        return post(loginUri(), request, csrfToken);
    }

    public AssertionResponse login(String login, String password, String csrfToken) {
        return login(new LoginRequest(login, password), csrfToken);
    }

    //  ==== activation token validation ====

    private String activateUri() {
        return BASE_URI + "/activate";
    }

    public AssertionResponse validateActivationToken(String token) {
        return get(activateUri() + "?token=" + token);
    }

    //  ==== account activation ====

    public AssertionResponse activate(Object body, String csrfToken) {
        return post(activateUri(), body, csrfToken);
    }

    public AssertionResponse activate(String jsonBody) {
        return post(activateUri(), jsonBody);
    }
}
