package by.iivanov.rpm.testing;

import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.convention.TestBean;

@ApplicationIntegrationTest
@SharedSpies
@Import(SharedTestClockConfiguration.class)
public abstract class AbstractApplicationIntegrationTest {

    @SuppressWarnings("NullAway.Init")
    @TestBean(name = "clock", enforceOverride = true)
    protected Clock clock;

    @SuppressWarnings("NullAway.Init")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static Clock clock() {
        return SharedTestClockConfiguration.CLOCK;
    }

    @BeforeEach
    void resetTestClock() {
        SharedTestClockConfiguration.resetToFixed();
    }

    @BeforeEach
    void deleteUsersCreatedByOtherTests() {
        // Full-context tests share one Postgres container and commit their registrations, so users
        // created by other tests accumulate in iam_user. The admin user grid lists ALL users, so a
        // read-all assertion is only deterministic against the seeded baseline. Seed rows use the
        // fixed 019b76da… id prefix (see db/data/user.csv); the synthetic system user is 0000…. Drop
        // everything else before each test to restore that baseline.
        jdbcTemplate.update("delete from iam_user where id::text not like '019b76da%'"
                + " and id <> '00000000-0000-0000-0000-000000000000'");
    }
}
