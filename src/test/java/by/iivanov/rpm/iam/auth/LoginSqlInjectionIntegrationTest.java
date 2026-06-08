package by.iivanov.rpm.iam.auth;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.auth.fixtures.AuthApi;
import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoginSqlInjectionIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String ACTIVE_USER_LOGIN = "admin";

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;

    LoginSqlInjectionIntegrationTest(AuthApi authApi, AuthSessionFactory authSessionFactory) {
        this.authApi = authApi;
        this.authSessionFactory = authSessionFactory;
    }

    static Stream<Arguments> sqlInjectionPayloads() {
        return Stream.of(
                argumentSet("SQL injection in login field is treated as literal text", "admin' OR '1'='1", "anything"),
                argumentSet(
                        "SQL injection in password field is treated as literal text",
                        ACTIVE_USER_LOGIN,
                        "' OR '1'='1 --"));
    }

    @ParameterizedTest
    @MethodSource("sqlInjectionPayloads")
    void should_return401_when_loginContainsSqlInjectionPayload(String login, String password) {
        var csrfToken = authSessionFactory.getCsrfToken();
        // language=JSON
        String loginRequest = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(login, password);

        var response = authApi.login(loginRequest, csrfToken);

        response.unwrap().expectStatus().isUnauthorized().expectCookie().doesNotExist("JSESSIONID");
    }
}
