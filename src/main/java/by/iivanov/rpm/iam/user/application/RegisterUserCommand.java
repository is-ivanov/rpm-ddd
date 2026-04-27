package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.PersonName;
import by.iivanov.rpm.shared.domain.errors.Checks;

public record RegisterUserCommand(PersonName userName, Login login, EmailAddress email) {

    public RegisterUserCommand {
        Checks.notNull(userName, "userName must not be null");
        Checks.notNull(login, "login must not be null");
        Checks.notNull(email, "email must not be null");
    }
}
