package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import io.qameta.allure.Issue;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthCsrfIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;

    AuthCsrfIntegrationTest(AuthApi authApi) {
        this.authApi = authApi;
    }

    @Test
    @DisplayName("WHEN request EXPECT 200 OK with csrf token in cookie")
    void shouldReturnCsrfToken() {
        authApi.csrf().unwrap().expectStatus().isOk().expectCookie().exists("XSRF-TOKEN");
    }

    @Issue("130")
    @Test
    @DisplayName("WHEN POST /api/auth/login without CSRF token EXPECT 403 RFC-9457 ProblemDetail body")
    void should_return403ProblemDetail_when_loginWithoutCsrfToken() {
        // GIVEN: a login request sent without any CSRF token
        @Language("JSON")
        String loginRequest = """
                {
                  "login": "admin",
                  "password": "admin"
                }
                """;

        // WHEN: client posts to /api/auth/login without a CSRF token
        var response = authApi.login(loginRequest);

        // THEN: 403 with an RFC-9457 ProblemDetail body, not the legacy {code,message} shape
        response.unwrap()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus()
                .isForbidden()
                .expectBody()
                .json("""
                    {
                      "detail": "Access denied: a valid CSRF token is required for this request.",
                      "instance": "/api/auth/login",
                      "status": 403,
                      "title": "Forbidden",
                      "type": "https://www.rpm-ddd.my/problem/access-denied"
                    }
                    """);
    }
}
