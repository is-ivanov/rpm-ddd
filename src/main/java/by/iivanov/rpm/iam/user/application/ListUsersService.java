package by.iivanov.rpm.iam.user.application;

import by.iivanov.rpm.iam.user.domain.UserSummary;
import by.iivanov.rpm.iam.user.domain.UserSummaryQuery;
import by.iivanov.rpm.shared.infrastructure.ApplicationService;
import java.util.List;

@ApplicationService
public class ListUsersService {

    private final UserSummaryQuery userSummaryQuery;

    /** Constructor. */
    public ListUsersService(UserSummaryQuery userSummaryQuery) {
        this.userSummaryQuery = userSummaryQuery;
    }

    /**
     * Lists every user for the admin grid (excluding the synthetic system user), ordered for display.
     */
    public List<UserSummary> listUsers() {
        return userSummaryQuery.findAllForGrid();
    }
}
