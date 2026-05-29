package by.iivanov.rpm.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import by.iivanov.rpm.shared.infrastructure.web.fixtures.SpaApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class SpaServingIntegrationTest extends AbstractApplicationIntegrationTest {

    private final SpaApi spaApi;

    SpaServingIntegrationTest(SpaApi spaApi) {
        this.spaApi = spaApi;
    }

    @ParameterizedTest(name = "GET {0} returns the SPA index shell (200, text/html)")
    @ValueSource(strings = {"/", "/login"})
    void shouldServeSpaShell(String path) {
        spaApi.getPage(path)
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
