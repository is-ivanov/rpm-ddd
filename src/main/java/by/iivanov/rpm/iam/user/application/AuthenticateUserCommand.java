package by.iivanov.rpm.iam.user.application;

public record AuthenticateUserCommand(String login, String password) {}
