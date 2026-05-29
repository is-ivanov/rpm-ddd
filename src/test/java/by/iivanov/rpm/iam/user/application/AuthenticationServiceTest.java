package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.UserAuthenticationException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
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
