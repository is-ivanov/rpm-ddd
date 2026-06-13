package by.iivanov.rpm.iam.auth.fixtures;

import by.iivanov.rpm.testing.api.AssertionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Statements for the login rate-limiting security scenario.
 *
 * <p>Owns the loop over consecutive failed login attempts and the Problem Detail assertions for the
 * lockout response. Tests stay pure DSL and never see the attempt count or the wrong-password
 * literal.
 */
@Component
public final class LoginRateLimitStatements {

    private static final int LOCKOUT_THRESHOLD = 5;
    private static final String ACTIVE_LOGIN = "admin";
    private static final String CORRECT_PASSWORD = "admin";
    private static final String WRONG_PASSWORD = "wrong-password";

    private static final String TOO_MANY_ATTEMPTS_PROBLEM = """
            {
              "detail": "Too many failed attempts",
              "instance": "/api/auth/login",
              "status": 429,
              "title": "Too Many Requests",
              "type": "https://www.rpm-ddd.my/problem/too-many-login-attempts"
            }
            """;

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    public LoginRateLimitStatements(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    /**
     * Establishes one reusable CSRF token (as a real SPA client does) and brings the account to one
     * failed attempt away from lockout, returning that token for the remaining attempts.
     */
    public String givenFailedAttemptsJustBelowLockoutThreshold() {
        var csrfToken = authSessionFactory.getCsrfToken();
        for (int attempt = 0; attempt < LOCKOUT_THRESHOLD - 1; attempt++) {
            authApi.login(ACTIVE_LOGIN, WRONG_PASSWORD, csrfToken);
        }
        return csrfToken;
    }

    /** Sends a single login with a wrong password and returns the response for assertion. */
    public AssertionResponse attemptLoginWithWrongPassword(String csrfToken) {
        return authApi.login(ACTIVE_LOGIN, WRONG_PASSWORD, csrfToken);
    }

    /** Sends a single login with the correct password and returns the response for assertion. */
    public AssertionResponse attemptLoginWithCorrectPassword(String csrfToken) {
        return authApi.login(ACTIVE_LOGIN, CORRECT_PASSWORD, csrfToken);
    }

    /** Asserts the response is rate limited (429) with the Problem Detail lockout body. */
    public void assertRateLimited(AssertionResponse response) {
        response.assertStatus(HttpStatus.TOO_MANY_REQUESTS)
                .assertProblemJson()
                .assertBodyMatches(TOO_MANY_ATTEMPTS_PROBLEM);
    }
}
