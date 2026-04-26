package by.iivanov.rpm.iam.auth.infrastructure;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import by.iivanov.rpm.iam.user.infrastructure.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class RpmUserDetailsServiceTest {

    private RpmUserDetailsService sut;
    private UserStatements userStatements;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = new InMemoryUserRepository();
        userStatements = new UserStatements();
        sut = new RpmUserDetailsService(userRepository);
    }

    @Test
    @DisplayName("WHEN user not found EXPECT UsernameNotFoundException")
    void when_userNotFound_expect_exception() {
        // GIVEN:
        userStatements.givenUserWithLogin("existing");
        // WHEN:
        Exception thrownException = catchException(() -> sut.loadUserByUsername("unknown"));
        // THEN:
        then(thrownException).isInstanceOf(UsernameNotFoundException.class).hasMessage("User not found: unknown");
    }
}
