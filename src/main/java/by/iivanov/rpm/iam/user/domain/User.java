package by.iivanov.rpm.iam.user.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.ZoneId;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.springframework.data.domain.AbstractAggregateRoot;

@Table(name = "iam_user")
public class User extends AbstractAggregateRoot<User> implements AggregateRoot<User, UserId> {

    private final UserId id;

    private final PersonName personName;
    private final EmailAddress email;
    private final Association<User, UserId> createdBy;
    private final Instant registeredAt;
    private final ZoneId timeZone;
    private final Association<User, UserId> updatedBy;
    private final Instant updatedAt;

    private final Login login;

    @AttributeOverride(name = "hash", column = @Column(name = "password_hash"))
    private Password password;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @AttributeOverride(name = "failedAttempts", column = @Column(name = "failed_login_attempts"))
    private LoginThrottle loginThrottle = LoginThrottle.empty();

    @Version
    private int version;

    private User(
            UserId id,
            PersonName personName,
            EmailAddress email,
            Login login,
            Password password,
            UserId createdBy,
            Instant registeredAt,
            ZoneId timeZone) {
        this.id = id;
        this.personName = personName;
        this.email = email;
        this.login = login;
        this.password = password;
        this.registeredAt = registeredAt;
        this.timeZone = timeZone;
        this.status = UserStatus.PENDING;
        this.createdBy = Association.forId(createdBy);
        // A freshly-registered user has never been updated: updated audit equals created audit.
        this.updatedBy = Association.forId(createdBy);
        this.updatedAt = registeredAt;
    }

    public static User register(
            UserId id,
            PersonName personName,
            EmailAddress email,
            Login login,
            Password password,
            UserId createdBy,
            Instant now,
            ZoneId timeZone) {
        var user = new User(id, personName, email, login, password, createdBy, now, timeZone);
        user.registerEvent(new UserRegisteredEvent(id, login, email));
        return user;
    }

    @Override
    public UserId getId() {
        return id;
    }

    public Login getLogin() {
        return login;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Password getPassword() {
        return password;
    }

    public Association<User, UserId> getCreatedBy() {
        return createdBy;
    }

    public int getVersion() {
        return version;
    }

    public PersonName getPersonName() {
        return personName;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void validateActiveForAuthentication() {
        if (status != UserStatus.ACTIVE) {
            throw new UserAuthenticationException(status.authenticationErrorMessage());
        }
    }

    public void ensureNotThrottled(Instant now) {
        if (loginThrottle.isLocked(now)) {
            throw new TooManyLoginAttemptsException("Too many failed attempts");
        }
    }

    public void recordFailedLogin(Instant now) {
        this.loginThrottle = loginThrottle.recordFailure(now);
    }

    public void clearFailedLogins() {
        this.loginThrottle = loginThrottle.clear();
    }

    public EmailAddress getEmail() {
        return email;
    }

    public void activate(Password hashedPassword) {
        this.password = hashedPassword;
        this.status = UserStatus.ACTIVE;
    }
}
