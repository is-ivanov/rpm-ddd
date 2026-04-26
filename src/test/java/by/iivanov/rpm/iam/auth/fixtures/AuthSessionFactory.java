package by.iivanov.rpm.iam.auth.fixtures;

import by.iivanov.rpm.testing.session.SessionContext;
import java.util.Objects;
import org.intellij.lang.annotations.Language;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.client.ExchangeResult;

/**
 * Test-only helper that performs the auth login flow and returns a reusable session snapshot.
 *
 * <p>Keep this class focused on login orchestration. It should not expose controller methods
 * beyond the scenarios it needs to build authenticated state.
 */
@Component
public final class AuthSessionFactory {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final AuthApi authApi;

    public AuthSessionFactory(AuthApi authApi) {
        this.authApi = authApi;
    }

    /** Logs in as the test admin user and returns the resulting session state. */
    public SessionContext loginAsAdmin() {
        var csrfToken = getCsrfToken();
        var loginResult = authApi.login(adminLoginRequest(), csrfToken).unwrap().returnResult();
        var sessionCookie = loginResult.getResponseCookies().getFirst("JSESSIONID");
        var sessionId = Objects.requireNonNull(sessionCookie).getValue();
        return new SessionContext(sessionId, csrfToken);
    }

    public String getCsrfToken() {
        ExchangeResult result = authApi.csrf().unwrap().returnResult();
        return extractCsrfToken(result);
    }

    public String extractCsrfToken(ExchangeResult result) {
        ResponseCookie csrfCookie = result.getResponseCookies().getFirst("XSRF-TOKEN");
        return Objects.requireNonNull(csrfCookie).getValue();
    }

    private String adminLoginRequest() {
        @Language("JSON")
        String loginRequestTemplate = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """;
        return loginRequestTemplate.formatted(ADMIN_LOGIN, ADMIN_PASSWORD);
    }
}
