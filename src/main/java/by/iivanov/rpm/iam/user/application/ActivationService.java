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
        var userId = tokenGenerator.parseActivationClaim(token);
        var optionalUser = userRepository.findById(userId);
        return optionalUser.orElseThrow(UserNotFoundException::new);
    }

    public void activate(String token, String plainPassword) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
