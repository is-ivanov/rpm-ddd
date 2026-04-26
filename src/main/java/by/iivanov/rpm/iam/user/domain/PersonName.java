package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.domain.errors.Checks;
import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Value object representing a person's name.
 */
@ValueObject
public record PersonName(String firstName, @Nullable String middleName, String lastName) {

    private static final int MAX_LENGTH = 255;

    /**
     * Constructor.
     *
     * @throws DomainValidationException if the firstName or the lastName is blank, or any field exceeds max length
     */
    public PersonName(String firstName, @Nullable String middleName, String lastName) {
        this.firstName = validateFirstName(StringUtils.trimToEmpty(firstName));
        this.middleName = validateMiddleName(StringUtils.trimToEmpty(middleName));
        this.lastName = validateLastName(StringUtils.trimToEmpty(lastName));
    }

    private static String validateFirstName(String trimmed) {
        Checks.notBlank(trimmed, "First name must not be blank");
        return Checks.maxLength(trimmed, MAX_LENGTH, "First name");
    }

    private static @Nullable String validateMiddleName(String trimmed) {
        if (trimmed.isBlank()) {
            return null;
        }
        return Checks.maxLength(trimmed, MAX_LENGTH, "Middle name");
    }

    private static String validateLastName(String trimmed) {
        Checks.notBlank(trimmed, "Last name must not be blank");
        return Checks.maxLength(trimmed, MAX_LENGTH, "Last name");
    }
}
