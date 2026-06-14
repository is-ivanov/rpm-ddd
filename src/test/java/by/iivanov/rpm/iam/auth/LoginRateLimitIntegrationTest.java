package by.iivanov.rpm.iam.auth;

import by.iivanov.rpm.iam.auth.fixtures.LoginRateLimitStatements;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

class LoginRateLimitIntegrationTest extends AbstractApplicationIntegrationTest {

    private final LoginRateLimitStatements statements;

    LoginRateLimitIntegrationTest(LoginRateLimitStatements statements) {
        this.statements = statements;
    }

    @Test
    @DisplayName("Account is temporarily locked after 5 consecutive failed login attempts")
    @ExpectedToFail(
            value = "Rate limiting not implemented: 5th failed login returns 401, not 429",
            withExceptions = AssertionError.class)
    void when_fiveConsecutiveFailedLogins_expect_rateLimitedEvenWithCorrectPassword() {
        // given the account is one failed attempt away from lockout
        var csrfToken = statements.givenFailedAttemptsJustBelowLockoutThreshold();

        // when a further login with a wrong password is sent
        var lockingAttempt = statements.attemptLoginWithWrongPassword(csrfToken);

        // then that attempt is rate-limited
        statements.assertRateLimited(lockingAttempt);

        // and a subsequent login with the correct password is still rate-limited within the window
        var correctPasswordAttempt = statements.attemptLoginWithCorrectPassword(csrfToken);
        statements.assertRateLimited(correctPasswordAttempt);
    }
}
