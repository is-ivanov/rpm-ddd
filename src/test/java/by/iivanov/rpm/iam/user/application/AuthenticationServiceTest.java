package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.UserAuthenticationService;
import by.iivanov.rpm.iam.user.domain.UserNotActivatedException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationServiceTest {

    private static final String PENDING_LOGIN = "pending_user";
    private static final String PENDING_PASSWORD = "Pending@123";

    private AuthenticationService sut;
    private UserStatements userStatements;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        userStatements = new UserStatements();
        passwordEncoder = NoOpPasswordEncoder.getInstance();
        sut = new AuthenticationService(new UserAuthenticationService(userStatements.userRepository), passwordEncoder);
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTest {

        @Test
        @DisplayName("WHEN user status is PENDING EXPECT UserNotActivatedException with 'Account not activated'")
        void when_userStatusIsPending_expect_exception() {
            userStatements.givenUserWithLoginPasswordAndStatus(PENDING_LOGIN, PENDING_PASSWORD, UserStatus.PENDING);

            var command = new AuthenticateUserCommand(new Login(PENDING_LOGIN), PENDING_PASSWORD);

            // WHEN:
            Exception caughtException = catchException(() -> sut.authenticate(command));

            // THEN:
            then(caughtException).isInstanceOf(UserNotActivatedException.class).hasMessage("Account not activated");
        }
    }
}
