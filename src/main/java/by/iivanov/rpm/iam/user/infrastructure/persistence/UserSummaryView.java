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
@Subselect("select id, first_name, middle_name, last_name, login, email, status,"
        + " registered_at, updated_at, created_by, updated_by from iam_user")
@Synchronize("iam_user")
class UserSummaryView {

    @Id
    private final UUID id;

    @Column(name = "first_name")
    private final String firstName;

    @Column(name = "middle_name")
    private final @Nullable String middleName;

    @Column(name = "last_name")
    private final String lastName;

    private final String login;

    private final String email;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private final UserStatus status;

    @Column(name = "registered_at")
    private final Instant createdAt;

    @Column(name = "updated_at")
    private final Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private final UserSummaryView createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private final UserSummaryView updatedBy;

    UserSummaryView(
            UUID id,
            String firstName,
            @Nullable String middleName,
            String lastName,
            String login,
            String email,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            UserSummaryView createdBy,
            UserSummaryView updatedBy) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.login = login;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
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
