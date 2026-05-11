package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.infrastructure.DomainService;

@DomainService
public class UserAuthenticationService {

    private final UserRepository userRepository;

    public UserAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(Login login) {
        User user = userRepository
                .findByLogin(login)
                .orElseThrow(() -> new UserNotActivatedException("Account not activated"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActivatedException("Account not activated");
        }
        return user;
    }
}
