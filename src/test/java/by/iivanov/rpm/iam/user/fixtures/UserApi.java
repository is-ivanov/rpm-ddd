package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.testing.api.AbstractApi;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.WebApi;
import by.iivanov.rpm.testing.session.SessionContext;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebApi
public class UserApi extends AbstractApi {

    private static final String BASE_URI = "/api/admin/users";
    private static final String PATH_PREFIX = BASE_URI + "/";

    public UserApi(RestTestClient restClient) {
        super(restClient);
    }

    public AssertionResponse registerUser(Object request) {
        return post(BASE_URI, request);
    }

    public AssertionResponse registerUser(Object request, SessionContext session) {
        return post(BASE_URI, request, session);
    }

    public AssertionResponse listUsers(SessionContext session) {
        return get(BASE_URI, session);
    }

    public AssertionResponse listUsers() {
        return get(BASE_URI);
    }

    public String extractCreatedUserId(AssertionResponse response) {
        return response.extractCreatedId(PATH_PREFIX);
    }
}
