package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.testing.session.SessionContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ActivationTokenFixture {

    private static final String FIRST_NAME = "Test";
    private static final String LAST_NAME = "User";

    private final JwtActivationTokenGenerator tokenGenerator;
    private final UserApi userApi;
    private final AuthSessionFactory authSessionFactory;

    public ActivationTokenFixture(
            JwtActivationTokenGenerator tokenGenerator, UserApi userApi, AuthSessionFactory authSessionFactory) {
        this.tokenGenerator = tokenGenerator;
        this.userApi = userApi;
        this.authSessionFactory = authSessionFactory;
    }

    public String generateValidToken(UserId userId) {
        return tokenGenerator.generateToken(userId, JtiGenerator.generate());
    }

    /** Registers a pending user via API and generates a valid activation token for them. */
    public ActivatedUserRegistration registerPendingUserWithToken() {
        var admin = authSessionFactory.loginAsAdmin();
        var uniqueSuffix = UUID.randomUUID().toString();
        var login = "activate_user_" + uniqueSuffix;
        var email = login + "@example.com";
        var request = new RegisterUserRequest(FIRST_NAME, null, LAST_NAME, login, email);
        var registerResponse = userApi.registerUser(request, admin);
        var userId = userApi.extractCreatedUserId(registerResponse);
        var userIdObj = new UserId(UUID.fromString(userId));
        var token = generateValidToken(userIdObj);
        return new ActivatedUserRegistration(login, email, FIRST_NAME, LAST_NAME, userIdObj, token, admin);
    }

    public record ActivatedUserRegistration(
            String login,
            String email,
            String firstName,
            String lastName,
            UserId userId,
            String token,
            SessionContext adminSession) {}
}
