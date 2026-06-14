package by.iivanov.rpm.iam.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Transient login throttle state on the User aggregate: consecutive failed attempts and a temporary lockout. */
@Embeddable
public class LoginThrottle {

    static final int MAX_ATTEMPTS = 5;
    static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    @Column(name = "failed_login_attempts")
    private int failedAttempts;

    @Column(name = "locked_until")
    private @Nullable Instant lockedUntil;

    protected LoginThrottle() {}

    private LoginThrottle(int failedAttempts, @Nullable Instant lockedUntil) {
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }

    public static LoginThrottle empty() {
        return new LoginThrottle(0, null);
    }

    boolean isLocked(Instant now) {
        return lockedUntil().map(now::isBefore).orElse(false);
    }

    void recordFailure(Instant now) {
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
            lockedUntil = now.plus(LOCKOUT_WINDOW);
        }
    }

    void clear() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    private Optional<Instant> lockedUntil() {
        return Optional.ofNullable(lockedUntil);
    }
}
