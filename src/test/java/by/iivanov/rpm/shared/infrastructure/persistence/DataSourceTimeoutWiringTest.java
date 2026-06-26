package by.iivanov.rpm.shared.infrastructure.persistence;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import io.qameta.allure.Issue;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Wiring test for the fail-fast database timeouts (Task #214). Boots the shared full application
 * context and asserts that the configured Hikari pool timeouts and the PostgreSQL JDBC driver
 * connection timeouts (carried as Hikari {@code data-source-properties}) reach the live
 * {@link HikariDataSource} bean, so a stalled DB handshake fails fast instead of hanging startup.
 */
@Issue("214")
class DataSourceTimeoutWiringTest extends AbstractApplicationIntegrationTest {

    private static final long EXPECTED_CONNECTION_TIMEOUT_MS = 10_000L;
    private static final long EXPECTED_VALIDATION_TIMEOUT_MS = 5_000L;
    private static final long EXPECTED_INITIALIZATION_FAIL_TIMEOUT_MS = 10_000L;
    private static final String EXPECTED_SOCKET_TIMEOUT_SECONDS = "30";
    private static final String EXPECTED_CONNECT_TIMEOUT_SECONDS = "10";
    private static final String EXPECTED_LOGIN_TIMEOUT_SECONDS = "10";

    private final HikariDataSource hikariDataSource;

    DataSourceTimeoutWiringTest(DataSource dataSource) {
        this.hikariDataSource = (HikariDataSource) dataSource;
    }

    @Test
    @DisplayName("WHEN the application context is built EXPECT fail-fast DB timeouts wired into the Hikari pool")
    void hikariPool_carriesFailFastTimeouts() {
        // GIVEN
        Properties dataSourceProperties = hikariDataSource.getDataSourceProperties();

        // WHEN
        long connectionTimeout = hikariDataSource.getConnectionTimeout();
        long validationTimeout = hikariDataSource.getValidationTimeout();
        long initializationFailTimeout = hikariDataSource.getInitializationFailTimeout();

        // THEN
        then(connectionTimeout).as("Hikari connection-timeout (ms)").isEqualTo(EXPECTED_CONNECTION_TIMEOUT_MS);
        then(validationTimeout).as("Hikari validation-timeout (ms)").isEqualTo(EXPECTED_VALIDATION_TIMEOUT_MS);
        then(initializationFailTimeout)
                .as("Hikari initialization-fail-timeout (ms)")
                .isEqualTo(EXPECTED_INITIALIZATION_FAIL_TIMEOUT_MS);
        then(dataSourceProperties.getProperty("socketTimeout"))
                .as("JDBC socketTimeout (s)")
                .isEqualTo(EXPECTED_SOCKET_TIMEOUT_SECONDS);
        then(dataSourceProperties.getProperty("connectTimeout"))
                .as("JDBC connectTimeout (s)")
                .isEqualTo(EXPECTED_CONNECT_TIMEOUT_SECONDS);
        then(dataSourceProperties.getProperty("loginTimeout"))
                .as("JDBC loginTimeout (s)")
                .isEqualTo(EXPECTED_LOGIN_TIMEOUT_SECONDS);
    }
}
