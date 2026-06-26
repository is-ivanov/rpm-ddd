package by.iivanov.rpm.iam.user.infrastructure.web;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * One row of the admin user grid: the user's identity, status, audit timestamps and resolved actor names.
 *
 * <p>{@code createdBy}/{@code updatedBy} carry resolved person-name parts — never raw UUIDs.
 */
public record UserRowResponse(
        String userId,
        String firstName,
        @Nullable String middleName,
        String lastName,
        String login,
        String email,
        String status,
        Instant createdAt,
        ActorNameResponse createdBy,
        Instant updatedAt,
        ActorNameResponse updatedBy) {}
