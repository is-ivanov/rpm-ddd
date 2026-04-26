package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.domain.errors.Checks;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Password(String hash) {

    public Password(String hash) {
        this.hash = validate(StringUtils.trimToEmpty(hash));
    }

    private static String validate(String value) {
        return Checks.notBlank(value, "Password must not be blank");
    }
}
