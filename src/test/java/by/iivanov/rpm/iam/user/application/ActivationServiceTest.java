package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Disabled("TDD Red Phase - ActivationService.validateToken not implemented")
class ActivationServiceTest {

    private static final String TEST_JWT_SECRET = "Y2hhbmdlLXRoaXMtdG8tYS1sb25nLXNlY3JldC1rZXktaW4tcHJvZHVjdGlvbg==";
    private static final String VALID_LOGIN = "testuser";
    private static final String VALID_EMAIL = "testuser@example.com";

    private ActivationService sut;
    private UserStatements userStatements;
    private JwtActivationTokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        userStatements = new UserStatements();
        tokenGenerator =
                new JwtActivationTokenGenerator(TEST_JWT_SECRET, Duration.ofHours(24), Clock.systemDefaultZone());
        sut = new ActivationService(userStatements.userRepository, tokenGenerator);
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTest {

        @Test
        @DisplayName("WHEN valid activation token EXPECT user login and email returned")
        void when_validActivationToken_expect_userLoginAndEmail() {
            var user = userStatements.givenPendingUserWithLoginAndEmail(VALID_LOGIN, VALID_EMAIL);
            var token = tokenGenerator.generateToken(user.getId(), JtiGenerator.generate());

            // WHEN:
            var result = sut.validateToken(token);

            // THEN:
            userStatements.assertValidatedUser(result, user);
        }
    }
}
