package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserNotFoundException;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;

@ApplicationService
public class ActivationService {

    private final UserRepository userRepository;
    private final JwtActivationTokenGenerator tokenGenerator;

    public ActivationService(UserRepository userRepository, JwtActivationTokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
    }

    public User validateToken(String token) {
        var userId = tokenGenerator.parseActivationClaim(token);
        var optionalUser = userRepository.findById(userId);
        return optionalUser.orElseThrow(UserNotFoundException::new);
    }
}
