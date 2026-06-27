package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.PersonName;
import by.iivanov.rpm.shared.domain.errors.Checks;
import java.time.ZoneId;

public record RegisterUserCommand(PersonName userName, Login login, EmailAddress email, ZoneId timeZone) {

    /** Canonical constructor validating that all command fields are present. */
    public RegisterUserCommand {
        Checks.notNull(userName, "userName must not be null");
        Checks.notNull(login, "login must not be null");
        Checks.notNull(email, "email must not be null");
        Checks.notNull(timeZone, "timeZone must not be null");
    }
}
