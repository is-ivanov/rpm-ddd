package by.iivanov.rpm.iam.user.domain;

import java.util.List;

/**
 * Query port for the admin user grid read-model: every user row (excluding the synthetic system user)
 * with its resolved actor names, ordered {@code created_at DESC, id DESC}.
 */
public interface UserSummaryQuery {

    /**
     * Returns the admin grid rows, excluding the synthetic system user, ordered {@code created_at DESC, id DESC}.
     */
    List<UserSummary> findAllForGrid();
}
