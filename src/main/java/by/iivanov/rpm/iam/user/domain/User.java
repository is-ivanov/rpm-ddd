package by.iivanov.rpm.iam.user.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

    private Login login;

    @AttributeOverride(name = "hash", column = @Column(name = "password_hash"))
    private Password password;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @Version
    private int version;

    private User(
            UserId id, PersonName personName, EmailAddress email, Login login, Password password, UserId createdBy) {
        this.id = id;
        this.personName = personName;
        this.email = email;
        this.login = login;
        this.password = password;
        this.status = UserStatus.PENDING;
        this.createdBy = Association.forId(createdBy);
    }

    /**
     * Factory method for registering a new user.
     *
     * @param temporaryPasswordPlain plain-text temporary password for email delivery
     */
    public static User register(
            UserId id,
            PersonName personName,
            EmailAddress email,
            Login login,
            Password password,
            String temporaryPasswordPlain,
            UserId createdBy) {
        var user = new User(id, personName, email, login, password, createdBy);
        user.registerEvent(new UserRegisteredEvent(id, login, email, temporaryPasswordPlain));
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
}
