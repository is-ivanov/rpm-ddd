package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.shared.infrastructure.web.responses.AuditResponse;
import by.iivanov.rpm.shared.infrastructure.web.responses.PersonNameResponse;

/**
 * A user as shown in the admin user list: a summary projection with identity, status, and audit metadata
 * (resolved actor names). The full single-user view is exposed separately as {@code UserDetailResponse}.
 */
public record UserSummaryResponse(
        String userId, PersonNameResponse name, String login, String email, String status, AuditResponse audit) {}
