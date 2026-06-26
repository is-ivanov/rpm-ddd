package by.iivanov.rpm.shared.web;

import org.jspecify.annotations.Nullable;

/**
 * A person's name, broken into its parts, for any read model that exposes one (users, patients, audit actors).
 */
public record PersonNameResponse(String firstName, @Nullable String middleName, String lastName) {}
