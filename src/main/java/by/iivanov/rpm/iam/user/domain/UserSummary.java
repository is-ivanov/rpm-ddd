package by.iivanov.rpm.iam.user.domain;

import java.time.Instant;

/**
 * Read-model projection of a user row for the admin user grid: the user identity, name, login, email,
 * status, audit timestamps, and the resolved {@code createdBy} / {@code updatedBy} actor names.
 * Returned by {@link UserSummaryQuery}; never the write aggregate {@code User}.
 */
public record UserSummary(
        UserId userId,
        ActorName name,
        String login,
        String email,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt,
        ActorName createdBy,
        ActorName updatedBy) {}
