package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.domain.errors.Checks;
import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Login(String login) {

    public static final int MAX_LENGTH = 50;

    /**
     * Constructor.
     *
     * @throws DomainValidationException if login is blank or exceeds max length
     */
    public Login(String login) {
        this.login = validate(StringUtils.trimToEmpty(login));
    }

    private static String validate(String trimmed) {
        Checks.notBlank(trimmed, "Login must not be blank");
        return Checks.maxLength(trimmed, MAX_LENGTH, "Login");
    }
}
