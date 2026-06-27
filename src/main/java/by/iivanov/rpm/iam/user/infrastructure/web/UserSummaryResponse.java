package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.domain.ActorName;
import by.iivanov.rpm.iam.user.domain.UserSummary;
import by.iivanov.rpm.shared.infrastructure.web.responses.AuditResponse;
import by.iivanov.rpm.shared.infrastructure.web.responses.PersonNameResponse;

/**
 * A user as shown in the admin user list: a summary projection with identity, status, and audit metadata
 * (resolved actor names). The full single-user view is exposed separately as {@code UserDetailResponse}.
 */
public record UserSummaryResponse(
        String userId, PersonNameResponse name, String login, String email, String status, AuditResponse audit) {

    /** Maps a {@link UserSummary} read-model row to its grid response. */
    public static UserSummaryResponse from(UserSummary summary) {
        return new UserSummaryResponse(
                summary.userId().id().toString(),
                name(summary.name()),
                summary.login(),
                summary.email(),
                summary.status().name(),
                new AuditResponse(
                        summary.createdAt(),
                        name(summary.createdBy()),
                        summary.updatedAt(),
                        name(summary.updatedBy())));
    }

    private static PersonNameResponse name(ActorName actor) {
        return new PersonNameResponse(actor.firstName(), actor.middleName(), actor.lastName());
    }
}
