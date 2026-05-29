package by.iivanov.rpm.shared.infrastructure.web.fixtures;

import by.iivanov.rpm.testing.api.AbstractApi;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.api.WebApi;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Raw transport API for browser-style page requests (Accept: text/html).
 *
 * <p>Used to assert that the Spring Boot backend serves the bundled single-page application
 * shell for both the root path and client-side deep links.
 */
@WebApi
public class SpaApi extends AbstractApi {

    public SpaApi(RestTestClient restClient) {
        super(restClient);
    }

    /**
     * Performs a browser-style GET (Accept: text/html) against the given URI.
     *
     * @param uri the path to request
     * @return the assertion-friendly response wrapper
     */
    public AssertionResponse getPage(String uri) {
        return new AssertionResponse(
                restClient.get().uri(uri).accept(MediaType.TEXT_HTML).exchange());
    }
}
