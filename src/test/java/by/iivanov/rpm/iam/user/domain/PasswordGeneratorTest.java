package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

class PasswordGeneratorTest {

    private final PasswordGenerator sut = new PasswordGenerator();

    private static final Pattern UPPERCASE = Pattern.compile("[ABCDEFGHJKLMNPQRSTUVWXYZ]");
    private static final Pattern LOWERCASE = Pattern.compile("[abcdefghjkmnpqrstuvwxyz]");
    private static final Pattern DIGIT = Pattern.compile("[23456789]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%&*?]");

    @RepeatedTest(10)
    @DisplayName("WHEN generate EXPECT password length is 12")
    void when_generate_expect_lengthIs12() {
        // WHEN:
        var password = sut.generate();
        // THEN:
        then(password).hasSize(12);
    }

    @RepeatedTest(50)
    @DisplayName("WHEN generate EXPECT password contains at least one char from each required group")
    void when_generate_expect_containsRequiredChars() {
        // WHEN:
        var password = sut.generate();
        // THEN:
        then(UPPERCASE.matcher(password).find())
                .as("password should contain uppercase")
                .isTrue();
        then(LOWERCASE.matcher(password).find())
                .as("password should contain lowercase")
                .isTrue();
        then(DIGIT.matcher(password).find()).as("password should contain digit").isTrue();
        then(SPECIAL.matcher(password).find())
                .as("password should contain special char")
                .isTrue();
    }

    @RepeatedTest(10)
    @DisplayName("WHEN generate twice EXPECT different passwords")
    void when_generateTwice_expect_differentPasswords() {
        // WHEN:
        var first = sut.generate();
        var second = sut.generate();
        // THEN:
        then(first).isNotEqualTo(second);
    }
}
