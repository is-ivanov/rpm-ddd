package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserNotActivatedException;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ApplicationService
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void authenticate(AuthenticateUserCommand command) {
        User user = userRepository
                .findByLogin(command.login())
                .orElseThrow(() -> new UserNotActivatedException("Account not activated"));
        user.authenticate(new Password(command.password()), passwordEncoder);
    }
}
