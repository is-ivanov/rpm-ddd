package by.iivanov.rpm.iam.user.domain;

import java.time.Duration;
import java.time.Instant;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/** Transient login throttle state on the User aggregate: consecutive failed attempts and a temporary lockout. */
@ValueObject
public record LoginThrottle(int failedAttempts, @Nullable Instant lockedUntil) {

    static final int MAX_ATTEMPTS = 5;
    static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    public static LoginThrottle empty() {
        return new LoginThrottle(0, null);
    }

    boolean isLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    LoginThrottle recordFailure(Instant now) {
        var attempts = failedAttempts + 1;
        if (attempts >= MAX_ATTEMPTS) {
            return new LoginThrottle(attempts, now.plus(LOCKOUT_WINDOW));
        }
        return new LoginThrottle(attempts, lockedUntil);
    }

    LoginThrottle clear() {
        return empty();
    }
}
