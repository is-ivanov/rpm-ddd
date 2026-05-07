package by.iivanov.rpm.iam.user.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.security.crypto.password.PasswordEncoder;

@Table(name = "iam_user")
public class User extends AbstractAggregateRoot<User> implements AggregateRoot<User, UserId> {

    private final UserId id;

    private final PersonName personName;
    private final EmailAddress email;
    private final Association<User, UserId> createdBy;
    private final Instant registeredAt;

    private Login login;

    @AttributeOverride(name = "hash", column = @Column(name = "password_hash"))
    private Password password;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @Version
    private int version;

    private User(
            UserId id,
            PersonName personName,
            EmailAddress email,
            Login login,
            Password password,
            UserId createdBy,
            Instant registeredAt) {
        this.id = id;
        this.personName = personName;
        this.email = email;
        this.login = login;
        this.password = password;
        this.registeredAt = registeredAt;
        this.status = UserStatus.PENDING;
        this.createdBy = Association.forId(createdBy);
    }

    public static User register(
            UserId id,
            PersonName personName,
            EmailAddress email,
            Login login,
            Password password,
            UserId createdBy,
            Instant now) {
        var user = new User(id, personName, email, login, password, createdBy, now);
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

    public EmailAddress getEmail() {
        return email;
    }

    public void authenticate(Password passwordToValidate, PasswordEncoder encoder) {
        if (status != UserStatus.ACTIVE) {
            throw new UserNotActivatedException("Account not activated");
        }
        if (!encoder.matches(passwordToValidate.hash(), this.password.hash())) {
            throw new UserNotActivatedException("Account not activated");
        }
    }
}
