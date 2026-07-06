package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.http.MediaType;

class UnauthorizedEntryPointIntegrationTest extends AbstractApplicationIntegrationTest {

    private final UserApi userApi;

    UnauthorizedEntryPointIntegrationTest(UserApi userApi) {
        this.userApi = userApi;
    }

    @Issue("138")
    @Test
    @ExpectedToFail(
            value = "Legacy UnauthorizedEntryPoint emits {code,message}; RFC-9457 ProblemDetail not yet wired",
            withExceptions = AssertionError.class)
    @DisplayName("WHEN unauthenticated GET /api/admin/users EXPECT 401 RFC-9457 ProblemDetail body")
    void should_return401ProblemDetail_when_unauthenticatedRequestToProtectedEndpoint() {
        // WHEN: client sends GET /api/admin/users with no session / no authentication
        var response = userApi.listUsers();

        // THEN: 401 with an RFC-9457 ProblemDetail body, not the legacy {code,message} shape
        response.unwrap()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .json("""
                    {
                      "detail": "Authentication is required to access this resource.",
                      "instance": "/api/admin/users",
                      "status": 401,
                      "title": "Unauthorized",
                      "type": "https://www.rpm-ddd.my/problem/unauthorized"
                    }
                    """);
    }
}
