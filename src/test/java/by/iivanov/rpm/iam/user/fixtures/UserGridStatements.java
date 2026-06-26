package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.infrastructure.web.ActorNameResponse;
import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.iam.user.infrastructure.web.UserRowResponse;
import by.iivanov.rpm.testing.api.AssertionResponse;
import by.iivanov.rpm.testing.session.SessionContext;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Acceptance Statements for the admin user grid: registers users via the create endpoint and asserts the
 * resolved, ordered rows returned by {@code GET /api/admin/users}. Owns no HTTP calls — it drives the
 * {@link UserApi} client.
 */
@Component
public class UserGridStatements {

    private static final Instant FIXED_REGISTERED_AT = Instant.parse("2026-01-05T10:23:56.632Z");
    private static final String ADMIN_LOGIN = "admin";
    private static final ActorNameResponse ADMIN_ACTOR = new ActorNameResponse("System", "System", "System");
    private static final ActorNameResponse SYSTEM_ACTOR = new ActorNameResponse("System", "", "");
    private static final String NEW_FIRST_NAME = "Ann";
    private static final String NEW_MIDDLE_NAME = "Bee";
    private static final String NEW_LAST_NAME = "Lee";

    private final UserApi userApi;

    public UserGridStatements(UserApi userApi) {
        this.userApi = userApi;
    }

    /**
     * Registers two users as the given admin; the second is created after the first, so its UUIDv7
     * {@code userId} is strictly larger.
     */
    public CreatedUsers givenTwoUsersCreatedBy(SessionContext admin) {
        var first = registerUser(admin);
        var second = registerUser(admin);
        return new CreatedUsers(first, second);
    }

    private CreatedUser registerUser(SessionContext admin) {
        var suffix = UUID.randomUUID().toString().substring(0, 12);
        var request = new RegisterUserRequest(
                NEW_FIRST_NAME, NEW_MIDDLE_NAME, NEW_LAST_NAME, "grid_" + suffix, "grid_" + suffix + "@example.com");
        var response = userApi.registerUser(request, admin);
        return new CreatedUser(userApi.extractCreatedUserId(response), request);
    }

    /**
     * Asserts the list returns 200, the newly-created user's row carries name parts/status/timestamps and
     * admin-resolved actor names, the seed actor renders as "System", and rows are ordered createdAt DESC
     * then userId DESC.
     */
    public void assertResolvedRowsInDeterministicOrder(AssertionResponse response, CreatedUsers created) {
        response.assertOk();
        List<UserRowResponse> rows = Arrays.asList(response.extractBodyAs(UserRowResponse[].class));

        assertCreatedUserRow(rows, created.first());
        assertCreatedUserRow(rows, created.second());
        assertSeedActorRendersAsSystem(rows);
        assertDeterministicOrder(rows, created);
    }

    private void assertCreatedUserRow(List<UserRowResponse> rows, CreatedUser created) {
        then(rowByUserId(rows, created.userId()))
                .as("Created user row carries name parts, status, timestamps and admin-resolved actor names")
                .usingRecursiveComparison()
                .isEqualTo(expectedRowFor(created));
    }

    private UserRowResponse expectedRowFor(CreatedUser created) {
        return new UserRowResponse(
                created.userId(),
                NEW_FIRST_NAME,
                NEW_MIDDLE_NAME,
                NEW_LAST_NAME,
                created.login(),
                created.email(),
                "PENDING",
                FIXED_REGISTERED_AT,
                ADMIN_ACTOR,
                FIXED_REGISTERED_AT,
                ADMIN_ACTOR);
    }

    private void assertSeedActorRendersAsSystem(List<UserRowResponse> rows) {
        UserRowResponse adminRow = adminRow(rows);
        assertResolvesToSystem(
                adminRow.createdBy(),
                "Seed admin's creator (SYSTEM) resolves to the single name \"System\" "
                        + "(firstName \"System\", empty middle and last name) — never a raw UUID");
        assertResolvesToSystem(
                adminRow.updatedBy(), "Seed admin's updater (SYSTEM) resolves to the same \"System\" name");
    }

    private void assertResolvesToSystem(ActorNameResponse actor, String description) {
        then(actor).as(description).usingRecursiveComparison().isEqualTo(SYSTEM_ACTOR);
    }

    private void assertDeterministicOrder(List<UserRowResponse> rows, CreatedUsers created) {
        String adminUserId = adminRow(rows).userId();
        List<String> order = rows.stream()
                .map(UserRowResponse::userId)
                .filter(id ->
                        id.equals(created.secondUserId()) || id.equals(created.firstUserId()) || id.equals(adminUserId))
                .toList();
        then(order)
                .as("createdAt DESC then userId DESC: newest tie first, then earlier, then older seed admin")
                .containsExactly(created.secondUserId(), created.firstUserId(), adminUserId);
    }

    private UserRowResponse rowByUserId(List<UserRowResponse> rows, String userId) {
        return rows.stream()
                .filter(row -> row.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No row for userId " + userId));
    }

    private UserRowResponse adminRow(List<UserRowResponse> rows) {
        return rows.stream()
                .filter(row -> row.login().equals(ADMIN_LOGIN))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No row for login " + ADMIN_LOGIN));
    }

    /**
     * A user created through the admin create endpoint, paired with the request used to create it.
     *
     * @param userId the created user's id, extracted from the Location header
     * @param request the registration payload submitted
     */
    public record CreatedUser(String userId, RegisterUserRequest request) {
        String login() {
            return request.login();
        }

        String email() {
            return request.email();
        }
    }

    /**
     * The two users created during setup, in creation order.
     *
     * @param first the user created first (smaller userId)
     * @param second the user created second (larger userId)
     */
    public record CreatedUsers(CreatedUser first, CreatedUser second) {
        String firstUserId() {
            return first.userId();
        }

        String secondUserId() {
            return second.userId();
        }
    }
}
