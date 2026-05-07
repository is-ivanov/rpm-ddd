package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.Password;

public record AuthenticateUserCommand(Login login, Password password) {}
