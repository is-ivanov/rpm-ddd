package by.iivanov.rpm.iam.user.fixtures;

import static org.instancio.Select.field;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import org.instancio.Instancio;
import org.instancio.InstancioApi;

public class UserBuilder {

    private final InstancioApi<User> builder = Instancio.of(User.class);

    public static UserBuilder anUser() {
        return new UserBuilder();
    }

    public UserBuilder withEmail(String email) {
        builder.set(field(User::getEmail), new EmailAddress(email));
        return this;
    }

    public UserBuilder withLogin(String login) {
        builder.set(field(User::getLogin), new Login(login));
        return this;
    }

    public UserBuilder withPassword(String password) {
        builder.set(field(User::getPassword), new Password(password));
        return this;
    }

    public UserBuilder withStatus(UserStatus status) {
        builder.set(field(User::getStatus), status);
        return this;
    }

    public User build() {
        return builder.create();
    }
}
