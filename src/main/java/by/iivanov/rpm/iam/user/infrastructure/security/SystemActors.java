package by.iivanov.rpm.iam.user.infrastructure.security;

import by.iivanov.rpm.iam.user.domain.UserId;
import java.util.UUID;

public final class SystemActors {

    public static final UserId SYSTEM_USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    private SystemActors() {}
}
