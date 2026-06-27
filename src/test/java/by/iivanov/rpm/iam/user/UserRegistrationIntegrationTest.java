package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.fixtures.UserGridStatements;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import com.github.f4b6a3.uuid.util.UuidUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRegistrationIntegrationTest extends AbstractMailIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;
    private final UserGridStatements userGridStatements;

    UserRegistrationIntegrationTest(
            AuthSessionFactory authSessionFactory,
            UserApi userApi,
            EmailStatements emailStatements,
            UserGridStatements userGridStatements) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.emailStatements = emailStatements;
        this.userGridStatements = userGridStatements;
    }

    @Test
    @DisplayName("WHEN admin registers a new user with a timezone EXPECT 201, activation email, "
            + "AND the user listed PENDING with createdAt==updatedAt and admin as both actors")
    void when_adminRegistersNewUserWithTimeZone_expect_createdEmailedAndListedPendingInGrid() {
        // GIVEN: admin is logged in, the inbox is empty AND a valid registration request carrying a timezone
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

        // THEN: the new user appears in the admin grid as PENDING, createdAt equals updatedAt, and both
        // audit actors resolve to the creating admin's name
        var createdUserId = userApi.extractCreatedUserId(response);
        userGridStatements.assertUserListedAsPendingCreatedByAdmin(admin, createdUserId, registration.request());
    }
}
