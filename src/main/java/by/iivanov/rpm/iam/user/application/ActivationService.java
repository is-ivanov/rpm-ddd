package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserNotFoundException;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;

@ApplicationService
public class ActivationService {

    private final UserRepository userRepository;
    private final JwtActivationTokenGenerator tokenGenerator;
    private final PasswordPolicy passwordPolicy;

    public ActivationService(
            UserRepository userRepository, JwtActivationTokenGenerator tokenGenerator, PasswordPolicy passwordPolicy) {
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
        this.passwordPolicy = passwordPolicy;
    }

    public User validateToken(String token) {
        return findUserByToken(token);
    }

    /**
     * Activates a user account by validating the token and setting the password.
     *
     * @throws UserNotFoundException if the user referenced by the token does not exist
     */
    public void activate(String token, String plainPassword) {
        var user = findUserByToken(token);
        var hashedPassword = passwordPolicy.hashPlain(plainPassword);
        user.activate(hashedPassword);
        userRepository.save(user);
    }

    private User findUserByToken(String token) {
        var userId = tokenGenerator.parseActivationClaim(token);
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
