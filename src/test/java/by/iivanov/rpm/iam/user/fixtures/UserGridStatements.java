package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.iam.user.infrastructure.web.UserSummaryResponse;
import by.iivanov.rpm.shared.infrastructure.web.responses.AuditResponse;
import by.iivanov.rpm.shared.infrastructure.web.responses.PersonNameResponse;
import by.iivanov.rpm.testing.session.SessionContext;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * Statements for asserting how a freshly-created user appears in the admin user grid. Orchestrates the
 * grid read via {@link UserApi} and pins the row's status and audit metadata; it owns no HTTP calls itself.
 */
@Component
public class UserGridStatements {

    private static final PersonNameResponse SEEDED_ADMIN_ACTOR = new PersonNameResponse("System", "System", "System");

    private final UserApi userApi;

    public UserGridStatements(UserApi userApi) {
        this.userApi = userApi;
    }

    /**
     * Asserts the user with the given id is listed in the admin grid exactly as registered — name, login and
     * email echoing the submitted request, status PENDING, both audit actors resolving to the seeded admin's
     * name, and createdAt and updatedAt both at the registration instant. The full row is pinned by exact
     * equality against the expected row.
     *
     * @param admin the authenticated admin session used to read the grid
     * @param userId the id of the freshly-created user to locate in the grid
     * @param registration the registration request whose fields the listed row must echo
     * @param registeredAt the instant the user was registered at — both audit timestamps must equal it
     */
    public void assertUserListedAsPendingCreatedByAdmin(
            SessionContext admin, String userId, RegisterUserRequest registration, Instant registeredAt) {
        UserSummaryResponse[] users = userApi.listUsers(admin).assertOk().extractBodyAs(UserSummaryResponse[].class);
        UserSummaryResponse row = findById(users, userId);
        then(row)
                .as("Freshly-created user %s listed in the admin grid as PENDING created by the admin", userId)
                .isEqualTo(expectedPendingRow(userId, registration, registeredAt));
    }

    private UserSummaryResponse expectedPendingRow(
            String userId, RegisterUserRequest registration, Instant registeredAt) {
        return new UserSummaryResponse(
                userId,
                new PersonNameResponse(registration.firstName(), registration.middleName(), registration.lastName()),
                registration.login(),
                registration.email(),
                "PENDING",
                new AuditResponse(registeredAt, SEEDED_ADMIN_ACTOR, registeredAt, SEEDED_ADMIN_ACTOR));
    }

    private UserSummaryResponse findById(UserSummaryResponse[] users, String userId) {
        return Arrays.stream(users)
                .filter(user -> user.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("User " + userId + " not found in the admin grid"));
    }
}
