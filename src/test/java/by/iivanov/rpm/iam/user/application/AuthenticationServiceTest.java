package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.UserAuthenticationException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationServiceTest {

    private static final String PENDING_LOGIN = "pending_user";
    private static final String PENDING_PASSWORD = "Pending@123";
    private static final String LOCKED_LOGIN = "locked_user";
    private static final String LOCKED_PASSWORD = "Locked@123";

    private AuthenticationService sut;
    private UserStatements userStatements;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        userStatements = new UserStatements();
        passwordEncoder = NoOpPasswordEncoder.getInstance();
        sut = new AuthenticationService(userStatements.userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTest {

        @Test
        @DisplayName("WHEN user status is PENDING EXPECT UserAuthenticationException with 'Account not activated'")
        void when_userStatusIsPending_expect_exception() {
            userStatements.givenUserWithLoginPasswordAndStatus(PENDING_LOGIN, PENDING_PASSWORD, UserStatus.PENDING);

            var command = new AuthenticateUserCommand(new Login(PENDING_LOGIN), PENDING_PASSWORD);

            // WHEN:
            Exception caughtException = catchException(() -> sut.authenticate(command));

            // THEN:
            then(caughtException)
                    .isInstanceOf(UserAuthenticationException.class)
                    .hasMessage("Account not activated");
        }

        @Test
        @DisplayName("Login with LOCKED user returns 401 with locked message")
        void when_userStatusIsLocked_expect_accountLockedMessage() {
            userStatements.givenUserWithLoginPasswordAndStatus(LOCKED_LOGIN, LOCKED_PASSWORD, UserStatus.LOCKED);

            var command = new AuthenticateUserCommand(new Login(LOCKED_LOGIN), LOCKED_PASSWORD);

            // WHEN:
            Exception caughtException = catchException(() -> sut.authenticate(command));

            // THEN:
            then(caughtException)
                    .isInstanceOf(UserAuthenticationException.class)
                    .hasMessage("Account locked");
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTest {

        @Test
        @DisplayName("Authenticated user retrieves own info")
        @Disabled("TDD Red Phase - Not yet implemented")
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
        @Disabled("TDD Red Phase - Not yet implemented")
        void when_userNotFound_expect_userNotFoundException() {
            // GIVEN:
            var unknownId = userStatements.givenUnknownUserId();

            // WHEN:
            userStatements.getCurrentUser(sut, unknownId);

            // THEN:
            userStatements.assertUserNotFound();
        }
    }
}
