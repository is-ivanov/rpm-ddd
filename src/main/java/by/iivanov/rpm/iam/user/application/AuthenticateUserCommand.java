package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.Login;

public record AuthenticateUserCommand(Login login, String password) {}
