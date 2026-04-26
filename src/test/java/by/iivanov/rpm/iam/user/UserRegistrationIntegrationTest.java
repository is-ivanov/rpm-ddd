package by.iivanov.rpm.iam.user;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.testing.ApplicationIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ApplicationIntegrationTest
class UserRegistrationIntegrationTest {

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

        var location = userApi.registerUser(request, admin).assertCreated().extractLocation();

        then(location).startsWith("/api/admin/users/");
    }
}
