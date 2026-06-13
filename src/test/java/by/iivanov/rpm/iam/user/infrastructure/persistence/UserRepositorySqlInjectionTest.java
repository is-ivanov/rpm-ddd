package by.iivanov.rpm.iam.user.infrastructure.persistence;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.testing.DbTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@DbTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Execution(ExecutionMode.SAME_THREAD)
class UserRepositorySqlInjectionTest {

    private static final Login SEEDED_LOGIN = new Login("admin");
    private static final Login TAUTOLOGY_INJECTION = new Login("admin' OR '1'='1");

    private final SpringDataJpaUserRepository userRepository;

    UserRepositorySqlInjectionTest(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("SQL injection tautology in login is bound as a literal parameter, matching no user")
    void should_returnEmpty_when_loginIsSqlInjectionTautology() {
        then(userRepository.findByLogin(TAUTOLOGY_INJECTION)).isEmpty();
    }

    @Test
    @DisplayName("Control: the exact seeded login is found, proving the query works and the user exists")
    void should_findSeededUser_when_loginMatchesExactly() {
        then(userRepository.findByLogin(SEEDED_LOGIN))
                .get()
                .extracting(User::getLogin)
                .isEqualTo(SEEDED_LOGIN);
    }
}
