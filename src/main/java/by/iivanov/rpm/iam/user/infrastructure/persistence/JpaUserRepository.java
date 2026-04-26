package by.iivanov.rpm.iam.user.infrastructure.persistence;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.UuidGenerator;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaUserRepository implements UserRepository {

    private final SpringDataJpaUserRepository springDataJpaUserRepository;

    JpaUserRepository(SpringDataJpaUserRepository springDataJpaUserRepository) {
        this.springDataJpaUserRepository = springDataJpaUserRepository;
    }

    @Override
    public User save(User user) {
        return springDataJpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataJpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByLogin(Login login) {
        return springDataJpaUserRepository.findByLogin(login);
    }

    @Override
    public UserId nextId() {
        return new UserId(UuidGenerator.generateEntityId());
    }

    @Override
    public boolean isLoginUnique(Login login) {
        return !springDataJpaUserRepository.existsByLogin(login);
    }

    @Override
    public boolean isEmailUnique(EmailAddress email) {
        return !springDataJpaUserRepository.existsByEmail(email);
    }
}
