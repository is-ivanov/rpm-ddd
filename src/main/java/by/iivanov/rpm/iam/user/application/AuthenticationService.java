package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserAuthenticationException;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ApplicationService
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param command the command containing the login and password for authentication
     * @return an authenticated user
     * @throws BadCredentialsException if the provided password does not match the stored password
     */
    public User authenticate(AuthenticateUserCommand command) {
        var user = userRepository
                .findByLogin(command.login())
                .orElseThrow(() -> new UserAuthenticationException("Account not activated"));
        user.validateActiveForAuthentication();
        if (!passwordEncoder.matches(command.password(), user.getPassword().hash())) {
            throw new BadCredentialsException("Bad credentials");
        }
        return user;
    }
}
