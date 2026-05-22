package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

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

    @Nested
    @DisplayName("validateToken() — error cases")
    class ValidateTokenErrorTest {

        @Test
        @DisplayName("WHEN expired activation token EXPECT throws ExpiredJwtException")
        void when_expiredActivationToken_expect_throwsExpiredJwtException() {
            var clock = MutableClock.of(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
            var generator = new JwtActivationTokenGenerator(TEST_JWT_SECRET, Duration.ofHours(24), clock);
            var service = new ActivationService(userStatements.userRepository, generator);
            var user = userStatements.givenPendingUserWithLoginAndEmail(VALID_LOGIN, VALID_EMAIL);
            var token = generator.generateToken(user.getId(), JtiGenerator.generate());
            clock.add(Duration.ofHours(25));

            userStatements.validateToken(service, token);
            userStatements.assertThrownExpiredJwtException();
        }

        @Test
        @DisplayName("WHEN malformed activation token EXPECT throws MalformedJwtException")
        void when_malformedActivationToken_expect_throwsMalformedJwtException() {
            userStatements.validateToken(sut, "not-a-valid-jwt");
            userStatements.assertThrownMalformedJwtException();
        }
    }
}
