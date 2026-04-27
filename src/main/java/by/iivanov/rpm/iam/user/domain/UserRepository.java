package by.iivanov.rpm.iam.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByLogin(Login login);

    UserId nextId();

    boolean isLoginUnique(Login login);

    boolean isEmailUnique(EmailAddress email);
}
