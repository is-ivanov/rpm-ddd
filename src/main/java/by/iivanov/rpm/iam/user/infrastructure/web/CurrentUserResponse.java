package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserStatus;
import java.util.List;

public record CurrentUserResponse(
        String userId,
        String login,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        String timeZone,
        List<String> roles) {

    public CurrentUserResponse {
        roles = List.copyOf(roles);
    }

    static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
                user.getId().id().toString(),
                user.getLogin().login(),
                user.getEmail().email(),
                user.getPersonName().firstName(),
                user.getPersonName().lastName(),
                user.getStatus(),
                user.getTimeZone().getId(),
                List.of());
    }
}
