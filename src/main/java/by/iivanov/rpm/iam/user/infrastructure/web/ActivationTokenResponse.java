package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.domain.User;

public record ActivationTokenResponse(String login, String email) {

    static ActivationTokenResponse from(User user) {
        return new ActivationTokenResponse(
                user.getLogin().login(), user.getEmail().email());
    }
}
