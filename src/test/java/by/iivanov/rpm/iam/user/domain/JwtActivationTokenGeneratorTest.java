package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import io.jsonwebtoken.security.SignatureException;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("parseActivationClaim() - signature verification (security)")
class JwtActivationTokenGeneratorTest {

    private static final String SECRET = "test-activation-secret-key-which-is-long-enough-32+";
    private static final String WRONG_SECRET = "a-totally-different-wrong-secret-key-also-32-bytes!";
    private static final Duration VALID_WINDOW = Duration.ofMinutes(30);

    private final JwtActivationTokenGenerator sut =
            new JwtActivationTokenGenerator(SECRET, VALID_WINDOW, Clock.systemUTC());

    @ParameterizedTest
    @MethodSource("maliciousTokens")
    @DisplayName("WHEN token signed with a non-application secret EXPECT SignatureException")
    void when_tokenSignedWithWrongKey_expect_signatureException(String description, String maliciousToken) {
        // GIVEN: a token that does NOT verify against the application signing key

        // WHEN:
        Throwable thrown = catchThrowable(() -> sut.parseActivationClaim(maliciousToken));

        // THEN:
        then(thrown)
                .as(
                        "%s must be rejected: parseActivationClaim() verifies the HMAC signature "
                                + "against the application secret, so a token signed with any other key "
                                + "fails verification rather than being accepted",
                        description)
                .isInstanceOf(SignatureException.class);
    }

    private static Stream<Arguments> maliciousTokens() {
        var attacker = new JwtActivationTokenGenerator(WRONG_SECRET, VALID_WINDOW, Clock.systemUTC());
        return Stream.of(
                Arguments.argumentSet(
                        "wrong-secret token",
                        "wrong-secret token (signed with WRONG_SECRET, same subject)",
                        attacker.generateToken(new UserId(UUID.randomUUID()), "jti-wrong-secret")),
                Arguments.argumentSet(
                        "tampered-resigned token",
                        "tampered-then-re-signed token (subject changed to a different user, "
                                + "re-signed with WRONG_SECRET)",
                        attacker.generateToken(new UserId(UUID.randomUUID()), "jti-tampered")));
    }
}
