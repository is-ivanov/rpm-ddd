package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.application.RegisterUserCommand;
import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.PersonName;
import by.iivanov.rpm.shared.infrastructure.validation.RequiredString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record RegisterUserRequest(
        @RequiredString String firstName,
        @Nullable @Size(max = 255) String middleName,
        @RequiredString String lastName,
        @NotBlank @Size(max = Login.MAX_LENGTH) String login,

        @NotBlank @Email @Size(max = EmailAddress.MAX_LENGTH) String email) {

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(
                new PersonName(firstName, middleName, lastName), new Login(login), new EmailAddress(email));
    }
}
