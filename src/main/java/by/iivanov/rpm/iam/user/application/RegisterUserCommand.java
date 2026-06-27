package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.PersonName;
import java.time.ZoneId;

public record RegisterUserCommand(PersonName userName, Login login, EmailAddress email, ZoneId timeZone) {}
