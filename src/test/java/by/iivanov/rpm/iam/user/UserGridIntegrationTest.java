package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.user.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

class UserGridIntegrationTest extends AbstractApplicationIntegrationTest {

    private static final String EXPECTED_GRID = "__files/iam/user/web/listUsers_out.json";

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;

    UserGridIntegrationTest(AuthSessionFactory authSessionFactory, UserApi userApi) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
    }

    @Test
    @ExpectedToFail(
            value = "GET /api/admin/users not implemented - returns 500 (UnsupportedOperationException) instead of 200",
            withExceptions = AssertionError.class)
    @DisplayName("WHEN admin lists users EXPECT 200 with resolved actor names in createdAt DESC, userId DESC order")
    void when_adminListsUsers_expect_resolvedGridInDeterministicOrder() {
        // GIVEN: an authenticated admin (the seeded users are the fixture data)
        var admin = authSessionFactory.loginAsAdmin();

        // WHEN: the admin requests the user list
        var response = userApi.listUsers(admin);

        // THEN: 200 with the full grid — name parts, status, audit timestamps, resolved actor names, ordered
        response.assertOk().assertBodyMatches(EXPECTED_GRID);
    }
}
