package by.iivanov.rpm.iam.auth.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String login, @NotBlank String password) {}
