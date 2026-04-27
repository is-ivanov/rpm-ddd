package by.iivanov.rpm.iam.auth;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.ApplicationIntegrationTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApplicationIntegrationTest
class AuthLoginIntegrationTest {

    private final RestTestClient restClient;
    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    AuthLoginIntegrationTest(RestTestClient restClient, AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.restClient = restClient;
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    /**
     * Intentionally uses RestTestClient directly to keep the HTTP flow explicit in this test.
     */
    @Test
    @DisplayName("WHEN login with valid credentials EXPECT 200 OK and JSESSIONID cookie")
    void when_loginWithValidCredentials_expect_okAndJsessionidCookie() {
        // GIVEN: a client requests a CSRF token
        var csrfResponse = restClient
                .get()
                .uri("/api/auth/csrf")
                .accept(APPLICATION_JSON)
                .exchange()
                .returnResult();
        var csrfToken = authSessionFactory.extractCsrfToken(csrfResponse);

        // WHEN: client sends login request with CSRF token
        // AND: client provides valid credentials
        // THEN: client receives 200 OK and JSESSIONID cookie
        restClient
                .post()
                .uri("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .cookie("XSRF-TOKEN", csrfToken)
                .header("X-XSRF-TOKEN", csrfToken)
                .body("""
                    {
                      "login": "admin",
                      "password": "admin"
                    }
                    """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectCookie()
                .exists("JSESSIONID");
    }

    @Test
    @DisplayName("WHEN login with wrong password EXPECT 401 Unauthorized")
    void when_loginWithWrongPassword_expect_unauthorized() {
        // GIVEN: csrf token
        var csrfToken = authSessionFactory.getCsrfToken();
        @Language("JSON")
        String invalidRequest = """
            {
              "login": "admin",
              "password": "invalidPassword"
            }
            """;

        // WHEN: client sends request with wrong password
        var response = authApi.login(invalidRequest, csrfToken);

        // THEN: 401 response without JSESSIONID cookie
        response.unwrap()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus()
                .isUnauthorized()
                .expectCookie()
                .doesNotExist("JSESSIONID")
                .expectBody()
                .json("""
                    {
                      "detail": "Bad credentials",
                      "instance": "/api/auth/login",
                      "status": 401,
                      "title": "Unauthorized",
                      "type": "https://www.rpm-ddd.my/problem/bad-credentials"
                    }
                    """);
    }
}
