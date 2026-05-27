package by.iivanov.rpm.iam.auth.infrastructure.web;

import by.iivanov.rpm.iam.user.domain.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountRequest(
        @NotBlank String token,

        @NotBlank @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH)
        String password) {}
