package by.iivanov.rpm.iam.auth.fixtures;

import by.iivanov.rpm.testing.api.AssertionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Statements for the login account-status validation scenarios (non-ACTIVE users rejected at login). */
@Component
public final class LoginStatusValidationStatements {

    private static final String UNAUTHORIZED_PROBLEM = """
            {
              "detail": "%s",
              "instance": "/api/auth/login",
              "status": 401,
              "title": "Unauthorized",
              "type": "https://www.rpm-ddd.my/problem/authentication-failed"
            }
            """;

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    public LoginStatusValidationStatements(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    /** Fetches a CSRF token for the login request. */
    public String givenCsrfToken() {
        return authSessionFactory.getCsrfToken();
    }

    /** Sends a login with the given credentials and returns the response. */
    public AssertionResponse attemptLogin(String login, String password, String csrfToken) {
        return authApi.login(login, password, csrfToken);
    }

    /** Asserts the login was rejected as 401 with the account-status Problem Detail message. */
    public void assertRejectedAsUnauthorized(AssertionResponse response, String expectedDetail) {
        response.assertStatus(HttpStatus.UNAUTHORIZED)
                .assertProblemJson()
                .assertBodyMatches(UNAUTHORIZED_PROBLEM.formatted(expectedDetail));
    }
}
