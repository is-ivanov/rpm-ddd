package by.iivanov.rpm.shared.web.errors;

import org.jspecify.annotations.Nullable;

public record FieldErrorDto(
        String objectName, String field, @Nullable String message) {}
