package by.iivanov.rpm.iam.user.domain;

import org.jspecify.annotations.Nullable;

/**
 * Read-model name triple for a user row and its resolved actors (createdBy / updatedBy).
 *
 * <p>Lenient by design — unlike the domain {@link PersonName} value object it performs no validation,
 * because it must also carry the synthetic system actor's constant {@code {firstName:"System",
 * middleName:"", lastName:""}} projection, which {@code PersonName} (non-blank last name) rejects.
 */
public record ActorName(String firstName, @Nullable String middleName, String lastName) {}
