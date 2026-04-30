package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.EmailAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserRegistrationPolicy;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@ApplicationService
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserRegistrationPolicy registrationPolicy;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    /** Constructor. */
    public UserRegistrationService(
            UserRepository userRepository,
            UserRegistrationPolicy registrationPolicy,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.userRepository = userRepository;
        this.registrationPolicy = registrationPolicy;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /**
     * Registers a new user in the system using the provided registration command and the identifier of the user
     * who initiated the creation.
     *
     * @param command the {@code RegisterUserCommand} containing details of the user to be registered,
     *                including username, login, and email.
     * @param createdBy the identifier of the user who initiated the registration process.
     * @return the unique identifier of the newly registered user.
     * @throws LoginAlreadyExistsException if the provided login is already in use.
     * @throws EmailAlreadyExistsException if the provided email address is already in use.
     * @throws NullPointerException if any required parameter in the registration process is null.
     */
    @Transactional
    public UserId registerUser(RegisterUserCommand command, UserId createdBy) {
        var personName = command.userName();
        var login = command.login();
        var email = command.email();

        registrationPolicy.verifyCanRegister(login, email);

        var placeholderHash =
                Objects.requireNonNull(passwordEncoder.encode(UUID.randomUUID().toString()));
        var user = User.register(
                userRepository.nextId(),
                personName,
                email,
                login,
                new Password(placeholderHash),
                createdBy,
                Instant.now(clock));

        return userRepository.save(user).getId();
    }
}
