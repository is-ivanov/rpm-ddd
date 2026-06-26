package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.shared.web.AuditResponse;
import by.iivanov.rpm.shared.web.PersonNameResponse;

/**
 * One row of the admin user grid: the user's identity, status, and audit metadata with resolved actor names.
 */
public record UserRowResponse(
        String userId, PersonNameResponse name, String login, String email, String status, AuditResponse audit) {}
