package by.iivanov.rpm.iam.user.domain;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record UserId(UUID id) implements Identifier {}
