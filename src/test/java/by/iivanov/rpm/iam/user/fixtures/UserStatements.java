package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import by.iivanov.rpm.iam.user.infrastructure.InMemoryUserRepository;
import org.instancio.Instancio;

public class UserStatements {

    public final InMemoryUserRepository userRepository;

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

    /** Saves a user instance with the provided login and status. */
    public void givenUserWithLoginAndStatus(String login, UserStatus status) {
        User existingUser = Instancio.of(User.class)
                .set(field(User::getLogin), new Login(login))
                .set(field(User::getStatus), status)
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
}
