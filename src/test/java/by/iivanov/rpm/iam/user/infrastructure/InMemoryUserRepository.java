package by.iivanov.rpm.iam.user.infrastructure;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> store = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByLogin(Login login) {
        return store.values().stream().filter(u -> u.getLogin().equals(login)).findFirst();
    }

    @Override
    public UserId nextId() {
        return new UserId(UUID.randomUUID());
    }

    @Override
    public boolean isLoginUnique(Login login) {
        return store.values().stream().noneMatch(u -> u.getLogin().equals(login));
    }

    @Override
    public boolean isEmailUnique(EmailAddress email) {
        return store.values().stream().noneMatch(u -> u.getEmail().equals(email));
    }
}
