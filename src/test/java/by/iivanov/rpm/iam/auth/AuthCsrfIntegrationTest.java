package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.testing.ApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ApplicationIntegrationTest
class AuthCsrfIntegrationTest {

    private final AuthApi authApi;

    AuthCsrfIntegrationTest(AuthApi authApi) {
        this.authApi = authApi;
    }

    @Test
    @DisplayName("WHEN request EXPECT 200 OK with csrf token in cookie")
    void shouldReturnCsrfToken() {
        authApi.csrf().unwrap().expectStatus().isOk().expectCookie().exists("XSRF-TOKEN");
    }
}
