package by.iivanov.rpm.iam.user.infrastructure.persistence;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.ActorName;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.domain.UserSummary;
import by.iivanov.rpm.iam.user.infrastructure.security.SystemActors;
import by.iivanov.rpm.testing.DbTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@DbTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Execution(ExecutionMode.SAME_THREAD)
@Import(JpaUserSummaryQuery.class)
class JpaUserSummaryQueryTest {

    private static final ActorName SYSTEM_ACTOR = new ActorName("System", "", "");
    private static final ActorName ADMIN_ACTOR = new ActorName("System", "System", "System");
    private static final UserId ADMIN_ID = userId("019b76da-a800-7000-a957-f5fb8061a532");
    private static final UserId ANN_LEE_ID = userId("019b76da-a800-7000-a957-f5fb8061a537");
    private static final List<UserId> EXPECTED_ORDER = List.of(
            userId("019b76da-a800-7000-a957-f5fb8061a537"),
            userId("019b76da-a800-7000-a957-f5fb8061a536"),
            userId("019b76da-a800-7000-a957-f5fb8061a535"),
            userId("019b76da-a800-7000-a957-f5fb8061a534"),
            userId("019b76da-a800-7000-a957-f5fb8061a533"),
            userId("019b76da-a800-7000-a957-f5fb8061a532"));

    private final JpaUserSummaryQuery query;

    JpaUserSummaryQueryTest(JpaUserSummaryQuery query) {
        this.query = query;
    }

    private static UserId userId(String value) {
        return new UserId(UUID.fromString(value));
    }

    @Test
    @ExpectedToFail(
            value = "JpaUserSummaryQuery.findAllForGrid() not implemented - throws"
                    + " UnsupportedOperationException; exclusion/ordering/actor-name resolution deferred to GREEN",
            withExceptions = UnsupportedOperationException.class)
    @DisplayName(
            "findAllForGrid excludes the system user, orders createdAt DESC then id DESC, and resolves actor names")
    void should_excludeSystemUser_resolveActorNames_andOrderByCreatedAtThenIdDesc() {
        var summaries = query.findAllForGrid();

        then(summaries).extracting(UserSummary::userId).containsExactlyElementsOf(EXPECTED_ORDER);
        then(summaries).extracting(UserSummary::userId).doesNotContain(SystemActors.SYSTEM_USER_ID);
        then(summaries)
                .filteredOn(summary -> summary.userId().equals(ADMIN_ID))
                .singleElement()
                .isEqualTo(new UserSummary(ADMIN_ID, SYSTEM_ACTOR, SYSTEM_ACTOR));
        then(summaries)
                .filteredOn(summary -> summary.userId().equals(ANN_LEE_ID))
                .singleElement()
                .isEqualTo(new UserSummary(ANN_LEE_ID, ADMIN_ACTOR, ADMIN_ACTOR));
    }
}
