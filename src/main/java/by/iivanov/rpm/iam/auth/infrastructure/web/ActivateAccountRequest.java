package by.iivanov.rpm.iam.auth.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record ActivateAccountRequest(
        @NotBlank String token, @NotBlank String password) {}
