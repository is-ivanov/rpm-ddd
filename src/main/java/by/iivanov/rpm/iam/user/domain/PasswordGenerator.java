package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.infrastructure.DomainService;
import java.security.SecureRandom;

@DomainService
public class PasswordGenerator {

    private static final int PASSWORD_LENGTH = 12;
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%&*?";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private final SecureRandom random;

    public PasswordGenerator() {
        this.random = new SecureRandom();
    }

    /**
     * Generates a random temporary password in plain text.
     * The result should be hashed by PasswordPolicy before persisting
     * and delivered in plain text to the user via email.
     */
    public String generate() {
        var chars = new char[PASSWORD_LENGTH];
        chars[0] = randomChar(UPPERCASE);
        chars[1] = randomChar(LOWERCASE);
        chars[2] = randomChar(DIGITS);
        chars[3] = randomChar(SPECIAL);

        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            chars[i] = randomChar(ALL_CHARS);
        }

        return shuffle(chars);
    }

    private char randomChar(String from) {
        return from.charAt(random.nextInt(from.length()));
    }

    private String shuffle(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
        return new String(array);
    }
}
