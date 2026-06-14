package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.UserAuthenticationException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.fixtures.LoginThrottleStatements;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationServiceTest {

    private static final String PENDING_LOGIN = "pending_user";
    private static final String PENDING_PASSWORD = "Pending@123";
    private static final String LOCKED_LOGIN = "locked_user";
    private static final String LOCKED_PASSWORD = "Locked@123";

    private AuthenticationService sut;
    private UserStatements userStatements;
    private LoginThrottleStatements throttleStatements;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        userStatements = new UserStatements();
        throttleStatements = new LoginThrottleStatements(userStatements.userRepository);
        passwordEncoder = NoOpPasswordEncoder.getInstance();
        var fixedClock = Clock.fixed(Instant.parse("2026-06-14T12:00:00Z"), ZoneOffset.UTC);
        sut = new AuthenticationService(userStatements.userRepository, passwordEncoder, fixedClock);
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTest {

        @ParameterizedTest
        @MethodSource("nonActiveStatuses")
        @DisplayName("WHEN user status is not ACTIVE EXPECT UserAuthenticationException")
        void when_nonActiveStatus_expect_exception(
                String login, String password, UserStatus status, String expectedMessage) {
            // GIVEN:
            userStatements.givenUserWithLoginPasswordAndStatus(login, password, status);
            var command = new AuthenticateUserCommand(new Login(login), password);

            // WHEN:
            var caughtException = catchException(() -> sut.authenticate(command));

            // THEN:
            then(caughtException)
                    .isInstanceOf(UserAuthenticationException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> nonActiveStatuses() {
            return Stream.of(
                    argumentSet(
                            "PENDING user",
                            PENDING_LOGIN,
                            PENDING_PASSWORD,
                            UserStatus.PENDING,
                            "Account not activated"),
                    argumentSet("LOCKED user", LOCKED_LOGIN, LOCKED_PASSWORD, UserStatus.LOCKED, "Account locked"));
        }

        @Test
        @DisplayName("WHEN the threshold-th consecutive wrong password is reached EXPECT TooManyLoginAttemptsException")
        void when_thresholdConsecutiveWrongPasswords_expect_rateLimited() {
            // GIVEN:
            throttleStatements.givenActiveUserForThrottling();
            throttleStatements.givenFailedAttemptsJustBelowThreshold(sut);

            // WHEN:
            throttleStatements.whenLoginWithWrongPassword(sut);

            // THEN:
            throttleStatements.assertRateLimited();
        }

        @Test
        @DisplayName("WHEN the correct password is used while locked EXPECT TooManyLoginAttemptsException")
        void when_correctPasswordWhileLocked_expect_rateLimited() {
            // GIVEN:
            throttleStatements.givenActiveUserForThrottling();
            throttleStatements.givenAccountLockedByFailedAttempts(sut);

            // WHEN:
            throttleStatements.whenLoginWithCorrectPassword(sut);

            // THEN:
            throttleStatements.assertRateLimited();
        }

        @Test
        @DisplayName("WHEN a successful login resets the counter EXPECT a fresh threshold run is required to lock")
        // Assertions live in the THEN Statements method (3-tier DSL), which the inspection cannot see.
        @SuppressWarnings("java:S2699")
        void when_successfulLoginResetsCounter_expect_freshThresholdRelocks() {
            // GIVEN:
            throttleStatements.givenActiveUserForThrottling();
            throttleStatements.givenFailedAttemptsJustBelowThreshold(sut);

            // WHEN:
            throttleStatements.whenLoginWithCorrectPassword(sut);

            // THEN:
            throttleStatements.thenFreshThresholdRunIsRequiredToRelock(sut);
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTest {

        @Test
        @DisplayName("Authenticated user retrieves own info")
        void when_userExists_expect_userReturned() {
            // GIVEN:
            var savedUser = userStatements.givenActiveUser();

            // WHEN:
            var actual = sut.getCurrentUser(savedUser.getId());

            // THEN:
            userStatements.assertValidatedUser(actual, savedUser);
        }

        @Test
        @DisplayName("WHEN user not found EXPECT UserNotFoundException")
        void when_userNotFound_expect_userNotFoundException() {
            // GIVEN:
            // WHEN:
            userStatements.getCurrentUser(sut, userStatements.givenUnknownUserId());

            // THEN:
            userStatements.assertUserNotFound();
        }
    }
}
