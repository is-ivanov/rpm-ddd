package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import com.github.f4b6a3.uuid.util.UuidUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRegistrationIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;

    UserRegistrationIntegrationTest(AuthSessionFactory authSessionFactory, UserApi userApi) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
    }

    @Test
    @DisplayName("WHEN admin registers a new user EXPECT 201 Created")
    void when_adminRegistersNewUser_expect_created() {
        // GIVEN: admin is logged in AND new user data is provided
        var admin = authSessionFactory.loginAsAdmin();
        var uniqueSuffix = UUID.randomUUID().toString();
        var request = new RegisterUserRequest(
                "Ivan",
                "Ivanovich",
                "Ivanov",
                "ivanov_ivan_" + uniqueSuffix,
                "ivanov_ivan_" + uniqueSuffix + "@example.com");

        // WHEN: admin registers a new user
        var response = userApi.registerUser(request, admin);

        // THEN: user is registered AND response contains location header
        response.assertCreated()
                .assertLocationIdMatches("/api/admin/users/", UUID::fromString, UuidUtil::isRandomBased);
    }
}
