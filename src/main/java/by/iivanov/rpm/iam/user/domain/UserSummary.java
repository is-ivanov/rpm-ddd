package by.iivanov.rpm.iam.user.domain;

/**
 * Read-model projection of a user row for the admin user grid: the user identity plus its resolved
 * actor names. Returned by {@link UserSummaryQuery}; never the write aggregate {@code User}.
 */
public record UserSummary(UserId userId, ActorName createdBy, ActorName updatedBy) {}
