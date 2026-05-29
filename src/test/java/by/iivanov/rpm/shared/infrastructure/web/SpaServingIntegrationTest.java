package by.iivanov.rpm.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import by.iivanov.rpm.shared.infrastructure.web.fixtures.SpaApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@Disabled("RED: SPA not served yet — / and /login return 401 (security denyAll, no SPA fallback)")
class SpaServingIntegrationTest extends AbstractApplicationIntegrationTest {

    private final SpaApi spaApi;

    SpaServingIntegrationTest(SpaApi spaApi) {
        this.spaApi = spaApi;
    }

    @Test
    @DisplayName("GET / returns the SPA index shell (200, text/html)")
    void shouldServeSpaShellAtRoot() {
        spaApi.getPage("/")
                .unwrap()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("<div id=\"app\">"));
    }

    @Test
    @DisplayName("GET a client-side deep link (/login) returns the SPA index shell (200, text/html)")
    void shouldServeSpaShellForDeepLink() {
        spaApi.getPage("/login")
                .unwrap()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("<div id=\"app\">"));
    }

    @Test
    @DisplayName("GET /api/** without a session stays protected (401)")
    void shouldKeepApiProtected() {
        spaApi.getPage("/api/auth/me").unwrap().expectStatus().isUnauthorized();
    }
}
