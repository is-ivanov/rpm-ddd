package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.instancio.Select.field;

import by.iivanov.rpm.iam.user.application.ActivationService;
import by.iivanov.rpm.iam.user.application.AuthenticationService;
import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.InvalidPasswordException;
import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserNotFoundException;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.infrastructure.InMemoryUserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.instancio.Instancio;

public class UserStatements {

    public final InMemoryUserRepository userRepository;
    private Throwable thrownException;

    @SuppressWarnings("NullAway.Init")
    public UserStatements() {
        this.userRepository = new InMemoryUserRepository();
    }

    /** Saves a user instance with the provided login. */
    public void givenUserWithLogin(String login) {
        User existingUser = Instancio.of(User.class)
                .set(field(User::getLogin), new Login(login))
                .create();
        userRepository.save(existingUser);
    }

    /** Saves a user instance with the provided email. */
    public void givenUserWithEmail(String email) {
        User existingUser = Instancio.of(User.class)
                .set(field(User::getEmail), new EmailAddress(email))
                .create();
        userRepository.save(existingUser);
    }

    /** Saves a user instance with the provided login, password, and status. */
    public void givenUserWithLoginPasswordAndStatus(String login, String password, UserStatus status) {
        User existingUser = Instancio.of(User.class)
                .set(field(User::getLogin), new Login(login))
                .set(field(User::getPassword), new Password(password))
                .set(field(User::getStatus), status)
                .create();
        userRepository.save(existingUser);
    }

    /** Saves a user with ACTIVE status and returns it. */
    public User givenActiveUser() {
        User user = Instancio.of(User.class)
                .set(field(User::getStatus), UserStatus.ACTIVE)
                .create();
        return userRepository.save(user);
    }

    /** Saves a PENDING user with the provided login and email. */
    public User givenPendingUserWithLoginAndEmail(String login, String email) {
        User user = Instancio.of(User.class)
                .set(field(User::getLogin), new Login(login))
                .set(field(User::getEmail), new EmailAddress(email))
                .set(field(User::getStatus), UserStatus.PENDING)
                .create();
        return userRepository.save(user);
    }

    /** Asserts that the validated user matches the expected user in all domain fields. */
    public void assertValidatedUser(User actual, User expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*domainEvents", ".*clearingMark")
                .isEqualTo(expected);
    }

    /** Generates a user ID that does not correspond to any saved user. */
    public UserId givenUnknownUserId() {
        return userRepository.nextId();
    }

    /** Asserts that the captured exception is a UserNotFoundException. */
    public void assertUserNotFound() {
        assertThat(thrownException)
                .as("Should throw UserNotFoundException when user not found")
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    /** Calls getCurrentUser on the given service, capturing any thrown exception. */
    public void getCurrentUser(AuthenticationService service, UserId userId) {
        thrownException = catchThrowable(() -> service.getCurrentUser(userId));
    }

    /** Calls validateToken on the given service, capturing any thrown exception. */
    public void validateToken(ActivationService service, String token) {
        thrownException = catchThrowable(() -> service.validateToken(token));
    }

    /** Asserts that the captured exception is an ExpiredJwtException. */
    public void assertThrownExpiredJwtException() {
        assertThat(thrownException)
                .as("Should throw ExpiredJwtException for expired token")
                .isInstanceOf(ExpiredJwtException.class);
    }

    /** Asserts that the captured exception is a MalformedJwtException. */
    public void assertThrownMalformedJwtException() {
        assertThat(thrownException)
                .as("Should throw MalformedJwtException for malformed token")
                .isInstanceOf(MalformedJwtException.class);
    }

    /** Calls activate on the given service, capturing any thrown exception. */
    public void activate(ActivationService service, String token, String plainPassword) {
        thrownException = catchThrowable(() -> service.activate(token, plainPassword));
    }

    /** Generates an activation token for the given user using the provided token generator. */
    public String generateActivationToken(JwtActivationTokenGenerator tokenGenerator, User user) {
        return tokenGenerator.generateToken(user.getId(), JtiGenerator.generate());
    }

    /** Creates an ActivationService with the given token generator, password policy, and user repository. */
    public ActivationService createActivationService(
            JwtActivationTokenGenerator tokenGenerator, PasswordPolicy passwordPolicy) {
        return new ActivationService(userRepository, tokenGenerator, passwordPolicy);
    }

    /** Asserts that the captured exception is an InvalidPasswordException with specific complexity violations. */
    public void assertThrownInvalidPasswordException() {
        assertThat(thrownException)
                .as("Should throw InvalidPasswordException for password that fails complexity rules")
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password must contain 1 or more uppercase characters.")
                .hasMessageContaining("Password must contain 1 or more digit characters.")
                .hasMessageContaining("Password must contain 1 or more special characters.");
    }
}
