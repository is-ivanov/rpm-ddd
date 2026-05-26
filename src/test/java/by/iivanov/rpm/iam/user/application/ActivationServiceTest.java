package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import by.iivanov.rpm.iam.user.fixtures.UserStatements;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.threeten.extra.MutableClock;

class ActivationServiceTest {

    private static final String TEST_JWT_SECRET = "Y2hhbmdlLXRoaXMtdG8tYS1sb25nLXNlY3JldC1rZXktaW4tcHJvZHVjdGlvbg==";
    private static final String VALID_LOGIN = "testuser";
    private static final String VALID_EMAIL = "testuser@example.com";

    @SuppressWarnings("deprecation")
    private final PasswordPolicy passwordPolicy = new PasswordPolicy(NoOpPasswordEncoder.getInstance());

    private MutableClock clock;
    private ActivationService sut;
    private UserStatements userStatements;
    private JwtActivationTokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        clock = MutableClock.of(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        userStatements = new UserStatements();
        tokenGenerator = new JwtActivationTokenGenerator(TEST_JWT_SECRET, Duration.ofHours(24), clock);
        sut = userStatements.createActivationService(tokenGenerator, passwordPolicy);
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTest {

        @Test
        @DisplayName("WHEN valid activation token EXPECT user login and email returned")
        void when_validActivationToken_expect_userLoginAndEmail() {
            var user = userStatements.givenPendingUserWithLoginAndEmail(VALID_LOGIN, VALID_EMAIL);
            var token = userStatements.generateActivationToken(tokenGenerator, user);

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
            // GIVEN:
            var user = userStatements.givenPendingUserWithLoginAndEmail(VALID_LOGIN, VALID_EMAIL);
            var token = userStatements.generateActivationToken(tokenGenerator, user);
            clock.add(Duration.ofHours(25));

            // WHEN:
            userStatements.validateToken(sut, token);

            // THEN:
            userStatements.assertThrownExpiredJwtException();
        }

        @Test
        @DisplayName("WHEN malformed activation token EXPECT throws MalformedJwtException")
        void when_malformedActivationToken_expect_throwsMalformedJwtException() {
            userStatements.validateToken(sut, "not-a-valid-jwt");
            userStatements.assertThrownMalformedJwtException();
        }
    }

    @Nested
    @DisplayName("activate() — password policy errors")
    class ActivatePasswordPolicyTest {

        @Test
        @DisplayName("WHEN password fails complexity rules EXPECT throws InvalidPasswordException")
        void when_passwordFailsComplexity_expect_throwsInvalidPasswordException() {
            var user = userStatements.givenPendingUserWithLoginAndEmail(VALID_LOGIN, VALID_EMAIL);
            var token = userStatements.generateActivationToken(tokenGenerator, user);
            // WHEN:
            userStatements.activate(sut, token, "passwordwithoutuppercase");

            // THEN:
            userStatements.assertThrownInvalidPasswordException();
        }
    }
}
