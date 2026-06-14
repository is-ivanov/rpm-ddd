package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordPolicyTest {

    @SuppressWarnings("deprecation")
    private final PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();

    private final PasswordPolicy sut = new PasswordPolicy(encoder);

    @Nested
    @DisplayName("hashPlain() - valid passwords")
    class ValidPasswordTest {

        @Test
        @DisplayName("WHEN valid password EXPECT hashed password matches original")
        void when_validPassword_expect_hashMatchesOriginal() {
            // GIVEN:
            String validPassword = "Str0ng!Pass#9";
            // WHEN:
            var result = sut.hashPlain(validPassword);
            // THEN:
            then(encoder.matches(validPassword, result.hash())).isTrue();
        }
    }

    @Nested
    @DisplayName("hashPlain() - storage format (security)")
    class StorageFormatTest {

        private static final String VALID_PLAINTEXT = "Passw0rd!Secur3";

        @SuppressWarnings("deprecation")
        private final PasswordPolicy productionSut =
                new PasswordPolicy(PasswordEncoderFactories.createDelegatingPasswordEncoder());

        @Test
        @DisplayName("WHEN valid password hashed EXPECT stored hash hides plaintext and uses BCrypt")
        void when_validPassword_expect_storedHashHidesPlaintextAndIsBcrypt() {
            // GIVEN:
            String plain = VALID_PLAINTEXT;

            // WHEN:
            String hash = productionSut.hashPlain(plain).hash();

            // THEN:
            // The production encoder is a DelegatingPasswordEncoder, which prepends the
            // algorithm id, so the stored hash is "{bcrypt}$2a$..." rather than bare "$2a$".
            then(hash).doesNotContain(plain);
            then(hash).startsWith("{bcrypt}$2a$");
        }

        @Test
        @DisplayName("WHEN same password hashed twice EXPECT different salted hashes")
        void when_samePasswordHashedTwice_expect_differentSaltedHashes() {
            // GIVEN:
            String plain = VALID_PLAINTEXT;

            // WHEN:
            String firstHash = productionSut.hashPlain(plain).hash();
            String secondHash = productionSut.hashPlain(plain).hash();

            // THEN:
            // BCrypt salts each hash, so the same plaintext must never produce an
            // identical stored hash. A regression to an unsalted digest would fail here.
            then(secondHash).isNotEqualTo(firstHash);
        }
    }

    @Nested
    @DisplayName("hashPlain() - invalid passwords")
    class InvalidPasswordTest {

        @ParameterizedTest
        @ValueSource(strings = {"short", "No1!", "aaaaaaaaaaa"})
        @DisplayName("WHEN password too short EXPECT InvalidPasswordException")
        void when_tooShort_expect_exception(String password) {
            // GIVEN:
            // WHEN:
            Throwable thrown = catchThrowable(() -> sut.hashPlain(password));
            // THEN:
            then(thrown)
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("Password must be 12 or more characters in length.");
        }

        @ParameterizedTest(name = "[{index}] password = {0}")
        @CsvSource({
            "lowercase1!, Password must contain 1 or more uppercase characters.",
            "UPPERCASE1!, Password must contain 1 or more lowercase characters.",
            "NoDigitsHere!, Password must contain 1 or more digit characters.",
            "NoSpecialChar1, Password must contain 1 or more special characters.",
            "'Pass word1!', Password contains a whitespace character."
        })
        @DisplayName("WHEN invalid password EXPECT InvalidPasswordException with specific violation")
        void when_invalidPassword_expect_exceptionWithViolation(String password, String expectedViolation) {
            // GIVEN:
            // WHEN:
            Throwable thrown = catchThrowable(() -> sut.hashPlain(password));
            // THEN:
            then(thrown).isInstanceOf(InvalidPasswordException.class).hasMessageContaining(expectedViolation);
        }
    }
}
