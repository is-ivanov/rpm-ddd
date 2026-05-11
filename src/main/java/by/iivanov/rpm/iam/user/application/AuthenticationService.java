package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.UserAuthenticationService;
import by.iivanov.rpm.iam.user.domain.UserNotActivatedException;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ApplicationService
public class AuthenticationService {

    private final UserAuthenticationService userAuthenticationService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserAuthenticationService userAuthenticationService, PasswordEncoder passwordEncoder) {
        this.userAuthenticationService = userAuthenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    public void authenticate(AuthenticateUserCommand command) {
        var user = userAuthenticationService.authenticate(command.login());
        if (!passwordEncoder.matches(command.password(), user.getPassword().hash())) {
            throw new UserNotActivatedException("Account not activated");
        }
    }
}
