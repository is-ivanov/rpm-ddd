package by.iivanov.rpm.iam.user.infrastructure.persistence;

import by.iivanov.rpm.iam.user.domain.ActorName;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserSummary;
import by.iivanov.rpm.iam.user.domain.UserSummaryQuery;
import by.iivanov.rpm.iam.user.infrastructure.security.SystemActors;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class JpaUserSummaryQuery implements UserSummaryQuery {

    private static final Sort GRID_ORDER =
            Sort.by(Sort.Direction.DESC, UserSummaryView::createdAt, UserSummaryView::id);

    private final SpringDataUserSummaryRepository repository;

    JpaUserSummaryQuery(SpringDataUserSummaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserSummary> findAllForGrid() {
        return repository.findByIdNot(SystemActors.SYSTEM_USER_ID.id(), GRID_ORDER).stream()
                .map(this::toSummary)
                .toList();
    }

    private UserSummary toSummary(UserSummaryView view) {
        return new UserSummary(
                new UserId(view.id()),
                new ActorName(view.firstName(), view.middleName(), view.lastName()),
                view.login(),
                view.email(),
                view.status(),
                view.createdAt(),
                view.updatedAt(),
                resolveActor(view.createdBy()),
                resolveActor(view.updatedBy()));
    }

    private ActorName resolveActor(UserSummaryView actor) {
        if (actor.id().equals(SystemActors.SYSTEM_USER_ID.id())) {
            return SystemActors.SYSTEM_ACTOR_NAME;
        }
        return new ActorName(actor.firstName(), actor.middleName(), actor.lastName());
    }
}
