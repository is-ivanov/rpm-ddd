package by.iivanov.rpm.shared.web;

import java.time.Instant;

/**
 * Audit metadata of an auditable read model: when it was created and last updated, and the resolved
 * actor names behind each change.
 */
public record AuditResponse(
        Instant createdAt, PersonNameResponse createdBy, Instant updatedAt, PersonNameResponse updatedBy) {}
