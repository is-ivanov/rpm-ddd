package by.iivanov.rpm.iam.user.infrastructure.web;

import org.jspecify.annotations.Nullable;

/**
 * Resolved person name of an actor (creator/updater) referenced by a user-grid row.
 *
 * <p>The seed/{@code SYSTEM} actor renders as {@code firstName: "System"} with an empty {@code lastName}
 * (never a raw UUID).
 */
public record ActorNameResponse(String firstName, @Nullable String middleName, String lastName) {}
