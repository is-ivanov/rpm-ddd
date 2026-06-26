package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.fixtures.UserGridStatements;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

class UserGridIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final UserGridStatements userGridStatements;

    UserGridIntegrationTest(
            AuthSessionFactory authSessionFactory, UserApi userApi, UserGridStatements userGridStatements) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.userGridStatements = userGridStatements;
    }

    @Test
    @ExpectedToFail(
            value = "GET /api/admin/users not implemented - returns 500 (UnsupportedOperationException) instead of 200",
            withExceptions = AssertionError.class)
    @DisplayName("WHEN admin lists users EXPECT 200 with resolved actor names in createdAt DESC, userId DESC order")
    void when_adminListsUsers_expect_rowsWithResolvedActorNamesInDeterministicOrder() {
        // GIVEN: admin is logged in AND two users are created by the admin
        var admin = authSessionFactory.loginAsAdmin();
        var created = userGridStatements.givenTwoUsersCreatedBy(admin);

        // WHEN: admin requests the user list
        var response = userApi.listUsers(admin);

        // THEN: 200, rows carry resolved actor names, seed actor renders as "System", deterministic order
        userGridStatements.assertResolvedRowsInDeterministicOrder(response, created);
    }
}
