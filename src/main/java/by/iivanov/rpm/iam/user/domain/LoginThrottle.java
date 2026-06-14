package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import java.time.Duration;
import java.time.Instant;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/** Transient login throttle state on the User aggregate: consecutive failed attempts and a temporary lockout. */
@ValueObject
public record LoginThrottle(int failedAttempts, @Nullable Instant lockedUntil) {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    /**
     * Constructor.
     *
     * @throws DomainValidationException if failedAttempts is negative
     */
    public LoginThrottle {
        if (failedAttempts < 0) {
            throw new DomainValidationException(
                    "Failed login attempts must not be negative, but was " + failedAttempts);
        }
    }

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
