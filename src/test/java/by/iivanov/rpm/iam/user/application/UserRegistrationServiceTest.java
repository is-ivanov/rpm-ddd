package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.instancio.Select.field;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.EmailAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserRegistrationPolicy;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

class UserRegistrationServiceTest {

    private static final UserId CREATED_BY = new UserId(UUID.randomUUID());
    private static final String EXISTING_LOGIN = "existing_login";
    private static final String EXISTING_EMAIL = "existing@example.com";

    private UserRegistrationService sut;
    private UserStatements userStatements;

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() {
        userStatements = new UserStatements();
        var userRepository = userStatements.userRepository;
        sut = new UserRegistrationService(
                userRepository,
                new UserRegistrationPolicy(userRepository),
                NoOpPasswordEncoder.getInstance(),
                Clock.fixed(Instant.parse("2026-04-30T12:34:17.482Z"), ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("registerUser()")
    class RegisterUserTest {

        @Test
        @DisplayName("WHEN login already exists EXPECT LoginAlreadyExistsException")
        void when_loginAlreadyExists_expect_exception() {
            // GIVEN:
            userStatements.givenUserWithLogin(EXISTING_LOGIN);
            var command = Instancio.of(RegisterUserCommand.class)
                    .set(field(Login::login), EXISTING_LOGIN)
                    .create();
            // WHEN:
            Exception caughtException = catchException(() -> sut.registerUser(command, CREATED_BY));
            // THEN:
            then(caughtException)
                    .isInstanceOf(LoginAlreadyExistsException.class)
                    .hasMessage("Login already exists: existing_login");
        }

        @Test
        @DisplayName("WHEN email already exists EXPECT EmailAlreadyExistsException")
        void when_emailAlreadyExists_expect_exception() {
            // GIVEN:
            userStatements.givenUserWithEmail(EXISTING_EMAIL);
            var command = Instancio.of(RegisterUserCommand.class)
                    .set(field(RegisterUserCommand::email), new EmailAddress(EXISTING_EMAIL))
                    .create();
            // WHEN:
            Exception caughtException = catchException(() -> sut.registerUser(command, CREATED_BY));
            // THEN:
            then(caughtException)
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already exists: existing@example.com");
        }

        @Test
        @DisplayName("Create user with a timezone succeeds and appears in the grid: "
                + "WHEN registerUser with a time zone EXPECT the stored user keeps that zone")
        @ExpectedToFail(
                value = "TDD Red Phase - User stores a placeholder zone, not command.timeZone()",
                withExceptions = AssertionError.class)
        void when_commandHasTimeZone_expect_storedUserKeepsZone() {
            // GIVEN:
            var newYork = ZoneId.of("America/New_York");
            var command = Instancio.of(RegisterUserCommand.class)
                    .set(field(RegisterUserCommand::timeZone), newYork)
                    .create();
            // WHEN:
            UserId registeredId = sut.registerUser(command, CREATED_BY);
            // THEN:
            userStatements.assertStoredUserTimeZone(registeredId, newYork);
        }
    }
}
