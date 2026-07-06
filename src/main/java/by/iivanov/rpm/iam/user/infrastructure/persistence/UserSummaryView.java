package by.iivanov.rpm.iam.user.infrastructure.persistence;

import by.iivanov.rpm.iam.user.domain.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

/**
 * Immutable read-only view over {@code iam_user} for the admin user grid. The {@code created_by} /
 * {@code updated_by} actors are resolved by the ORM through self-referencing {@code @ManyToOne}
 * associations to the same view, so the adapter never hand-writes join SQL.
 *
 * <p>Mapped with {@code @Subselect} rather than {@code @Table("iam_user")}: the write aggregate
 * {@code User} already maps that physical table, and two entities over one table make Hibernate raise
 * {@code DuplicateMappingException}. A subselect is a distinct derived "table", so the read model
 * coexists with the write aggregate and is skipped by schema validation.
 */
@Entity
@Immutable
@Subselect("SELECT id, first_name, middle_name, last_name, login, email, status,"
        + " registered_at, updated_at, created_by, updated_by FROM iam_user")
@Synchronize("iam_user")
class UserSummaryView {

    @Id
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private @Nullable String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "login")
    private String login;

    @Column(name = "email")
    private String email;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @Column(name = "registered_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserSummaryView createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserSummaryView updatedBy;

    // JPA reflection-init exception: Hibernate instantiates this @Subselect entity via the no-arg
    // constructor and populates fields by reflection, so they are non-null after load despite NullAway.
    @SuppressWarnings("NullAway.Init")
    protected UserSummaryView() {
        // no-arg constructor required for Hibernate reflection instantiation of this @Subselect view
    }

    UUID id() {
        return id;
    }

    String firstName() {
        return firstName;
    }

    @Nullable String middleName() {
        return middleName;
    }

    String lastName() {
        return lastName;
    }

    String login() {
        return login;
    }

    String email() {
        return email;
    }

    UserStatus status() {
        return status;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant updatedAt() {
        return updatedAt;
    }

    UserSummaryView createdBy() {
        return createdBy;
    }

    UserSummaryView updatedBy() {
        return updatedBy;
    }
}
