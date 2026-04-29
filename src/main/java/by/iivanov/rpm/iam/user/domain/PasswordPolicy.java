package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.infrastructure.DomainService;
import java.util.Objects;
import org.passay.DefaultPasswordValidator;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.ValidationResult;
import org.passay.data.EnglishCharacterData;
import org.passay.resolver.PropertiesMessageResolver;
import org.passay.rule.CharacterRule;
import org.passay.rule.LengthRule;
import org.passay.rule.WhitespaceRule;
import org.springframework.security.crypto.password.PasswordEncoder;

@DomainService
public class PasswordPolicy {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;

    private static final PasswordValidator VALIDATOR = new DefaultPasswordValidator(
            new PropertiesMessageResolver(),
            new LengthRule(MIN_LENGTH, MAX_LENGTH),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new WhitespaceRule());

    private final PasswordEncoder encoder;

    public PasswordPolicy(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Validates password complexity and returns hashed password.
     *
     * @throws InvalidPasswordException if password does not meet complexity requirements
     */
    public Password hashPlain(String plainPassword) {
        ValidationResult result = VALIDATOR.validate(new PasswordData(plainPassword));
        if (!result.isValid()) {
            throw new InvalidPasswordException(result.getMessages());
        }
        var hash = Objects.requireNonNull(encoder.encode(plainPassword));
        return new Password(hash);
    }
}
