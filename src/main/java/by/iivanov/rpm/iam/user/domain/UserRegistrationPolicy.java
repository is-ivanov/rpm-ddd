package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.infrastructure.DomainService;

@DomainService
public class UserRegistrationPolicy {

    private final UserRepository userRepository;

    public UserRegistrationPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verifies if a user can be registered with the specified login and email address.
     * Throws an exception if the login or email is already in use.
     *
     * @param login the login being verified for uniqueness
     * @param email the email address being verified for uniqueness
     * @throws LoginAlreadyExistsException if the login is already in use
     * @throws EmailAlreadyExistsException if the email is already in use
     */
    public void verifyCanRegister(Login login, EmailAddress email) {
        if (!userRepository.isLoginUnique(login)) {
            throw new LoginAlreadyExistsException(login);
        }

        if (!userRepository.isEmailUnique(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
