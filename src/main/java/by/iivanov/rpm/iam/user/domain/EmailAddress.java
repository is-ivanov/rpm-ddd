package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.AppConstants;
import by.iivanov.rpm.shared.domain.errors.Checks;
import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

/** Email address VO. */
@ValueObject
public record EmailAddress(String email) {

    public static final int MAX_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Constructor.
     *
     * @throws DomainValidationException if email is blank, invalid format, or exceeds max length
     */
    public EmailAddress(String email) {
        this.email = validate(StringUtils.trimToEmpty(email).toLowerCase(AppConstants.DEFAULT_LOCALE));
    }

    private static String validate(String lowered) {
        Checks.notBlank(lowered, "Email must not be blank");
        Checks.maxLength(lowered, MAX_LENGTH, "Email");
        Matcher matcher = EMAIL_PATTERN.matcher(lowered);
        if (!matcher.matches()) {
            throw new DomainValidationException("Email must be a well-formed email address, but was: " + lowered);
        }
        return lowered;
    }
}
