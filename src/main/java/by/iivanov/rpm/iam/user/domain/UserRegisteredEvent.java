package by.iivanov.rpm.iam.user.domain;

import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record UserRegisteredEvent(UserId userId, Login login, EmailAddress email) {}
