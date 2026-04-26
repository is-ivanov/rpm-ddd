package by.iivanov.rpm.iam.user.fixtures;

import static org.instancio.Select.field;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
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
}
