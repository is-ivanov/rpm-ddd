package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import com.github.f4b6a3.uuid.util.UuidUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRegistrationIntegrationTest extends AbstractMailIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;

    UserRegistrationIntegrationTest(
            AuthSessionFactory authSessionFactory, UserApi userApi, EmailStatements emailStatements) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.emailStatements = emailStatements;
    }

    @Test
    @DisplayName("WHEN admin registers a new user EXPECT 201 Created AND an activation email delivered")
    void when_adminRegistersNewUser_expect_createdAndActivationEmailDelivered() {
        // GIVEN: admin is logged in, the inbox is empty AND a valid registration request is prepared
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();

        // WHEN: admin registers a new user
        var response = userApi.registerUser(registration.request(), admin);

        // THEN: user is registered AND response contains location header
        response.assertCreated()
                .assertLocationIdMatches("/api/admin/users/", UUID::fromString, UuidUtil::isTimeOrderedEpoch);

        // THEN: an activation email is delivered to the registered address with the configured
        // from-address, subject, and an activation link carrying the token
        emailStatements.assertActivationEmailDeliveredTo(registration.email());
    }
}
