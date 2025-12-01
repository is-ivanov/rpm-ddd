package by.iivanov.rpm.shared;

import com.google.common.base.Preconditions;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Value object representing a person's name.
 */
@Builder
@ValueObject
public record PersonName(String firstName, @Nullable String middleName, String lastName) {

    /**
     * Constructor.
     */
    public PersonName(String firstName, @Nullable String middleName, String lastName) {
        this.firstName = Preconditions.checkNotNull(StringUtils.trimToNull(firstName), "firstName must not be null");
        this.middleName = StringUtils.trimToNull(middleName);
        this.lastName = Preconditions.checkNotNull(StringUtils.trimToNull(lastName), "lastName must not be null");
    }
}
