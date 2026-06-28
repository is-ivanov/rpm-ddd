package by.iivanov.rpm.iam.user.infrastructure.persistence;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import by.iivanov.rpm.testing.TestResources;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Per-PR guard against the full-stack journey seed drifting out of sync with the {@code iam_user}
 * schema (#234). The nightly journey applies {@code db/fixtures/fullstack-seed.sql} out-of-band via
 * {@code scripts/seed-fullstack.sh} AFTER the production migrations run; when a story adds a required
 * column to {@code iam_user} without updating that seed, the nightly fails late at the seeding step.
 *
 * <p>This test boots the same production-migrated schema and applies the SAME seed file, so the drift
 * surfaces on the PR instead of silently in the nightly. It shares the cached full application context
 * (no second context fork).
 */
@Issue("234")
class FullstackSeedSchemaGuardTest extends AbstractApplicationIntegrationTest {

    private static final String FULLSTACK_SEED = "db/fixtures/fullstack-seed.sql";

    private final JdbcClient jdbcClient;

    FullstackSeedSchemaGuardTest(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Test
    @DisplayName("WHEN the full-stack journey admin seed runs against the production-migrated schema "
            + "EXPECT it applies cleanly (no NOT NULL/FK violation) and is idempotent")
    void fullstackSeed_appliesAgainstProductionSchema() {
        String seedSql = TestResources.readUtf8(FULLSTACK_SEED);

        int rowsAffected = jdbcClient.sql(seedSql).update();

        then(rowsAffected)
                .as("fullstack-seed.sql must apply against the production-migrated iam_user schema "
                        + "(a missing NOT NULL / FK column would throw here); 0 rows because the journey admin "
                        + "is already seeded by user.csv in the test context, so ON CONFLICT DO NOTHING is a no-op")
                .isZero();
    }
}
