package by.iivanov.rpm.iam.user.fixtures;

import static by.iivanov.rpm.iam.user.fixtures.UserBuilder.aUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import by.iivanov.rpm.iam.user.application.AuthenticateUserCommand;
import by.iivanov.rpm.iam.user.application.AuthenticationService;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.TooManyLoginAttemptsException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.infrastructure.InMemoryUserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;

/** Statements for the login rate-limiting (throttle) scenarios on AuthenticationService. */
public class LoginThrottleStatements {

    private static final int LOCKOUT_THRESHOLD = 5;
    private static final String THROTTLE_LOGIN = "throttle_user";
    private static final String THROTTLE_PASSWORD = "Throttle@123";
    private static final String WRONG_PASSWORD = "Wrong@000";

    private final InMemoryUserRepository userRepository;
    private @Nullable Throwable thrownException;

    public LoginThrottleStatements(InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Saves an ACTIVE user used for the login throttle scenarios. */
    public void givenActiveUserForThrottling() {
        userRepository.save(aUser().withLogin(THROTTLE_LOGIN)
                .withPassword(THROTTLE_PASSWORD)
                .withStatus(UserStatus.ACTIVE)
                .build());
    }

    /** Drives the service through failed logins just below the lockout threshold (threshold - 1 wrong). */
    public void givenFailedAttemptsJustBelowThreshold(AuthenticationService service) {
        attemptWrongPasswordLogins(service, LOCKOUT_THRESHOLD - 1);
    }

    /** Drives the service through the full lockout threshold of failed logins so the account is locked. */
    public void givenAccountLockedByFailedAttempts(AuthenticationService service) {
        attemptWrongPasswordLogins(service, LOCKOUT_THRESHOLD);
    }

    private void attemptWrongPasswordLogins(AuthenticationService service, int times) {
        var wrongCommand = wrongPasswordCommand();
        for (int attempt = 0; attempt < times; attempt++) {
            thrownException = catchThrowable(() -> service.authenticate(wrongCommand));
        }
    }

    /** Performs a single login with the wrong password, capturing any thrown exception. */
    public void whenLoginWithWrongPassword(AuthenticationService service) {
        thrownException = catchThrowable(() -> service.authenticate(wrongPasswordCommand()));
    }

    /** Performs a single login with the correct password, capturing any thrown exception. */
    public void whenLoginWithCorrectPassword(AuthenticationService service) {
        var correctCommand = new AuthenticateUserCommand(new Login(THROTTLE_LOGIN), THROTTLE_PASSWORD);
        thrownException = catchThrowable(() -> service.authenticate(correctCommand));
    }

    private static AuthenticateUserCommand wrongPasswordCommand() {
        return new AuthenticateUserCommand(new Login(THROTTLE_LOGIN), WRONG_PASSWORD);
    }

    /** Asserts that the captured exception is a TooManyLoginAttemptsException with the lockout message. */
    public void assertRateLimited() {
        assertThat(thrownException)
                .as("Should throw TooManyLoginAttemptsException once the account is locked")
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .hasMessage("Too many failed attempts");
    }

    /** Asserts the last attempt was rejected as bad credentials, not rate-limited (accounts still open). */
    public void assertNotRateLimited() {
        assertThat(thrownException)
                .as("Account should still accept attempts (not locked) — bad credentials, not rate-limited")
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Asserts the failure counter was reset by the prior successful login: a fresh full threshold run of failures is
     * required to lock again. The next (threshold - 1) wrong passwords stay below the lock, and only the threshold-th
     * one locks the account again.
     */
    public void thenFreshThresholdRunIsRequiredToRelock(AuthenticationService service) {
        attemptWrongPasswordLogins(service, LOCKOUT_THRESHOLD - 1);
        assertNotRateLimited();
        whenLoginWithWrongPassword(service);
        assertRateLimited();
    }
}
