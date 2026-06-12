package by.iivanov.rpm.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import by.iivanov.rpm.shared.infrastructure.web.fixtures.SpaApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.http.MediaType;

class SpaServingIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String SPA_SHELL = """
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <title>RPM</title>
              </head>
              <body>
                <div id="app"></div>
              </body>
            </html>
            """;

    private final SpaApi spaApi;

    SpaServingIntegrationTest(SpaApi spaApi) {
        this.spaApi = spaApi;
    }

    @ParameterizedTest(name = "GET {0} returns the SPA index shell (200, text/html)")
    @ValueSource(strings = {"/", "/login"})
    void shouldServeSpaShell(String path) {
        assertServesSpaShell(path);
    }

    @Test
    @DisplayName("GET /api/** without a session stays protected (401)")
    void shouldKeepApiProtected() {
        spaApi.getPage("/api/auth/me").unwrap().expectStatus().isUnauthorized();
    }

    @Test
    @Issue("162")
    @ExpectedToFail(
            value = "TDD Red Phase - /dashboard not in SpaForwardingController allow-list, "
                    + "denyAll returns 401 instead of the SPA shell",
            withExceptions = AssertionError.class)
    @DisplayName("Deep-link GET /dashboard returns the SPA index shell (200, text/html)")
    void shouldForwardUnknownNonApiDeepLinkToSpaShell() {
        assertServesSpaShell("/dashboard");
    }

    @Test
    @Issue("162")
    @DisplayName("GET /api/<unknown> without a session stays protected (401), never forwarded to the SPA shell")
    void shouldKeepUnknownApiPathProtected() {
        spaApi.getPage("/api/nonexistent").unwrap().expectStatus().isUnauthorized();
    }

    @Test
    @Issue("162")
    @DisplayName("GET /assets/** serves the asset itself (200), never the SPA shell")
    void shouldServeAssetUnchanged() {
        spaApi.getAsset("/assets/app.js")
                .assertOk()
                .unwrap()
                .expectHeader()
                .contentType(MediaType.parseMediaType("text/javascript"))
                .expectBody(String.class)
                .value(body -> assertThat(body).as("asset body").isEqualTo("console.log(\"rpm-test-asset\");"));
    }

    private void assertServesSpaShell(String path) {
        spaApi.getPage(path)
                .unwrap()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .expectBody(String.class)
                .value(body -> assertThat(body).as("SPA index shell").isEqualTo(SPA_SHELL));
    }
}
