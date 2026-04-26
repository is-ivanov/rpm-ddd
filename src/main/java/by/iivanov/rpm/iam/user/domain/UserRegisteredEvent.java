package by.iivanov.rpm.iam.user.domain;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Domain event published when a new user is registered.
 * Carries the temporary password in plain text for email delivery.
 */
@DomainEvent
public record UserRegisteredEvent(UserId userId, Login login, EmailAddress email, String temporaryPasswordPlain) {}
