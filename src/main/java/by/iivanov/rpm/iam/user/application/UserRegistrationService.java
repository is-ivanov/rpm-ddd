package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.EmailAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.InvalidPasswordException;
import by.iivanov.rpm.iam.user.domain.LoginAlreadyExistsException;
import by.iivanov.rpm.iam.user.domain.PasswordGenerator;
import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserRegistrationPolicy;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import org.springframework.transaction.annotation.Transactional;

@ApplicationService
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserRegistrationPolicy registrationPolicy;
    private final PasswordPolicy passwordPolicy;
    private final PasswordGenerator passwordGenerator;

    /** Constructor. */
    public UserRegistrationService(
            UserRepository userRepository,
            UserRegistrationPolicy registrationPolicy,
            PasswordPolicy passwordPolicy,
            PasswordGenerator passwordGenerator) {
        this.userRepository = userRepository;
        this.registrationPolicy = registrationPolicy;
        this.passwordPolicy = passwordPolicy;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Registers a new user in the system using the provided registration details.
     * Automatically generates a temporary password for the user, verifies registration
     * policies, hashes the password for security, and persists the user.
     *
     * @param command    the details required to register the user, including the user's name, login, and email
     * @param createdBy  the identifier of the user performing the registration
     * @return           the unique identifier of the newly registered user
     * @throws LoginAlreadyExistsException if the specified login is already in use
     * @throws EmailAlreadyExistsException if the specified email is already in use
     * @throws InvalidPasswordException    if the generated password does not meet complexity requirements
     */
    @Transactional
    public UserId registerUser(RegisterUserCommand command, UserId createdBy) {
        var personName = command.userName();
        var login = command.login();
        var email = command.email();
        var plainPassword = passwordGenerator.generate();

        registrationPolicy.verifyCanRegister(login, email);

        var hashedPassword = passwordPolicy.hashPlain(plainPassword);
        var user = User.register(
                userRepository.nextId(), personName, email, login, hashedPassword, plainPassword, createdBy);

        return userRepository.save(user).getId();
    }
}
