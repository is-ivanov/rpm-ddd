package by.iivanov.rpm.testing.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import by.iivanov.rpm.testing.session.SessionContext;
import org.springframework.test.web.servlet.client.RestTestClient;

public abstract class AbstractApi {

    protected final RestTestClient restClient;

    protected AbstractApi(RestTestClient restClient) {
        this.restClient = restClient;
    }

    protected AssertionResponse get(String uri) {
        return new AssertionResponse(
                restClient.get().uri(uri).accept(APPLICATION_JSON).exchange());
    }

    protected AssertionResponse get(String uri, SessionContext session) {
        return new AssertionResponse(withSession(restClient.get().uri(uri).accept(APPLICATION_JSON), session)
                .exchange());
    }

    /**
     * Sends a POST request to the specified URI with the provided request body.
     * This method uses JSON as the content type for the request.
     *
     * @param uri the target endpoint for the POST request
     * @param body the request body to be sent in the POST request
     * @return an {@code AssertionResponse} object representing the response from the server
     */
    protected AssertionResponse post(String uri, Object body) {
        return new AssertionResponse(restClient
                .post()
                .uri(uri)
                .contentType(APPLICATION_JSON)
                .body(body)
                .exchange());
    }

    protected AssertionResponse post(String uri, Object body, SessionContext session) {
        return new AssertionResponse(withSession(restClient.post().uri(uri).contentType(APPLICATION_JSON), session)
                .body(body)
                .exchange());
    }

    protected AssertionResponse post(String uri, Object body, String csrfToken) {
        return new AssertionResponse(withCsrf(restClient.post().uri(uri).contentType(APPLICATION_JSON), csrfToken)
                .body(body)
                .exchange());
    }

    private <S extends RestTestClient.RequestHeadersSpec<?>> S withSession(S spec, SessionContext session) {
        spec.cookie("JSESSIONID", session.sessionId());
        return withCsrf(spec, session.csrfToken());
    }

    private <S extends RestTestClient.RequestHeadersSpec<?>> S withCsrf(S spec, String csrfToken) {
        spec.cookie("XSRF-TOKEN", csrfToken);
        spec.header("X-XSRF-TOKEN", csrfToken);
        return spec;
    }
}
