package by.iivanov.rpm.testing;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Restores the {@code iam_user} table to its seeded baseline before each full-context integration
 * test. Resolves the auto-configured {@link JdbcClient} from the Spring {@link ExtensionContext} and
 * runs the delete through it, keeping the DB-cleanup responsibility out of the shared context base.
 */
public class IamUserBaselineCleanupExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        // Full-context tests share one Postgres container and commit their registrations, so users
        // created by other tests accumulate in iam_user. The admin user grid lists ALL users, so a
        // read-all assertion is only deterministic against the seeded baseline. Seed rows use the
        // fixed 019b76da… id prefix (see db/data/user.csv); the synthetic system user is 0000…. Drop
        // everything else before each test to restore that baseline.
        JdbcClient jdbcClient = SpringExtension.getApplicationContext(context).getBean(JdbcClient.class);
        jdbcClient.sql("""
            DELETE FROM iam_user
            WHERE id::text NOT LIKE '019b76da%'
              AND id <> '00000000-0000-0000-0000-000000000000'
            """).update();
    }
}
