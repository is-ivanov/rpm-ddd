package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.fixtures.UserGridStatements;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import com.github.f4b6a3.uuid.util.UuidUtil;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

class UserRegistrationIntegrationTest extends AbstractMailIntegrationTest {

    /**
     * The instant the registration is performed at — advanced past the seeded baseline
     * ({@code 2026-01-05T10:23:56.632Z}) so the new user's audit timestamps prove the create path stamps
     * the live clock rather than coincidentally matching a seed value, while staying within the admin
     * session's validity window.
     */
    private static final Instant REGISTERED_AT = Instant.parse("2026-01-20T12:00:00Z");

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;
    private final UserGridStatements userGridStatements;
    private final MutableClock clock;

    UserRegistrationIntegrationTest(
            AuthSessionFactory authSessionFactory,
            UserApi userApi,
            EmailStatements emailStatements,
            UserGridStatements userGridStatements,
            MutableClock clock) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.emailStatements = emailStatements;
        this.userGridStatements = userGridStatements;
        this.clock = clock;
    }

    @Test
    @DisplayName("WHEN admin registers a new user with a timezone EXPECT 201, activation email, "
            + "AND the user listed PENDING with createdAt==updatedAt and admin as both actors")
    void when_adminRegistersNewUserWithTimeZone_expect_createdEmailedAndListedPendingInGrid() {
        // GIVEN: admin is logged in, the inbox is empty, a valid registration request carrying a timezone
        // is prepared AND the clock is advanced to the moment the user is registered
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();
        clock.setInstant(REGISTERED_AT);

        // WHEN: admin registers a new user
        var response = userApi.registerUser(registration.request(), admin);

        // THEN: user is registered AND response contains location header
        response.assertCreated()
                .assertLocationIdMatches("/api/admin/users/", UUID::fromString, UuidUtil::isTimeOrderedEpoch);

        // THEN: an activation email is delivered to the registered address with the configured
        // from-address, subject, and an activation link carrying the token
        emailStatements.assertActivationEmailDeliveredTo(registration.email());

        // THEN: the new user appears in the admin grid as PENDING, with createdAt and updatedAt both at the
        // registration instant, and both audit actors resolving to the creating admin's name
        var createdUserId = userApi.extractCreatedUserId(response);
        userGridStatements.assertUserListedAsPendingCreatedByAdmin(
                admin, createdUserId, registration.request(), REGISTERED_AT);
    }
}
