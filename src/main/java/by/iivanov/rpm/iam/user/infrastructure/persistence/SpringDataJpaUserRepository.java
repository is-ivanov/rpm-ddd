package by.iivanov.rpm.iam.user.infrastructure.persistence;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataJpaUserRepository extends JpaRepository<User, UserId> {

    boolean existsByLogin(Login login);

    boolean existsByEmail(EmailAddress email);

    Optional<User> findByLogin(Login login);
}
