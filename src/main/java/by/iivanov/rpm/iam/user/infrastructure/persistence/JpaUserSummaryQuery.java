package by.iivanov.rpm.iam.user.infrastructure.persistence;

import by.iivanov.rpm.iam.user.domain.UserSummary;
import by.iivanov.rpm.iam.user.domain.UserSummaryQuery;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class JpaUserSummaryQuery implements UserSummaryQuery {

    private final SpringDataUserSummaryRepository repository;

    JpaUserSummaryQuery(SpringDataUserSummaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserSummary> findAllForGrid() {
        throw new UnsupportedOperationException();
    }
}
