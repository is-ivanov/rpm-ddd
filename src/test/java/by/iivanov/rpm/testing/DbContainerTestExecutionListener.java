package by.iivanov.rpm.testing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * An implementation of the {@link TestExecutionListener} interface
 * designed to manage database setup and configuration during the execution of test plans.
 *
 * <p>This includes initializing local or containerized PostgreSQL databases,
 * setting up system properties for test use, and handling database recreation as needed.
 * This listener checks for test cases tagged with a specific database-related tag (e.g., "db") and
 * ensures the necessary database environment is prepared.
 * It interacts with PostgreSQL installations
 * on the localhost or starts a test container if a local database server is not available.
 *
 * <p>Key functionality includes:
 * <ul>
 *   <li>Detecting and counting tests with the configured database tag</li>
 *   <li>Preparing the database by recreating the schema or initializing a test container</li>
 *   <li>Setting relevant Spring Boot properties for datasource configurations</li>
 * </ul>
 */
public class DbContainerTestExecutionListener implements TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DbContainerTestExecutionListener.class);

    private static final String LOCAL_DB_HOST_URL = "jdbc:postgresql://localhost:54034/";

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        long count = testPlan.countTestIdentifiers(this::hasDbTag);
        log.info("[testing-support] testPlanExecutionStarted; matched tests={}.", count);
        if (count > 0) {
            String jdbcUrl = resolveAndPrepareDb();
            setSystemSpringBootDbProperties(jdbcUrl);
            log.info("[testing-support] DB properties set. URL: {}", jdbcUrl);
        }
    }

    private boolean hasDbTag(TestIdentifier testIdentifier) {
        if (!testIdentifier.isTest()) {
            return false;
        }
        return testIdentifier.getTags().stream().anyMatch(tag -> tag.getName().equals(Constants.DB_TEST_TAG));
    }

    private String resolveAndPrepareDb() {
        String adminUrl = LOCAL_DB_HOST_URL + "postgres";
        String targetUrl = LOCAL_DB_HOST_URL + Constants.TARGET_DB_NAME;

        log.info("Checking for local db server at {}", LOCAL_DB_HOST_URL);

        try (var con = DriverManager.getConnection(adminUrl, Constants.DB_USER, Constants.DB_PASSWORD)) {
            log.info("Local db server found. Recreating database '{}'...", Constants.TARGET_DB_NAME);
            recreateDatabase(con);
            log.info("Database '{}' recreated successfully.", Constants.TARGET_DB_NAME);
            return targetUrl;

        } catch (SQLException e) {
            log.warn("Local Db server not found or connection failed ({}). Starting Testcontainer...", e.getMessage());
            var container = PostgresContainersLifecycleManager.init();
            String containerAdminUrl =
                    "jdbc:postgresql://%s:%d/postgres".formatted(container.getHost(), container.getMappedPort(5432));
            try (var connection =
                    DriverManager.getConnection(containerAdminUrl, container.getUsername(), container.getPassword())) {
                recreateDatabase(connection);
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to recreate database in testcontainer", ex);
            }
            return container.getJdbcUrl();
        }
    }

    private void recreateDatabase(Connection connection) {
        try (var stmt = connection.createStatement()) {
            String initSql = Files.readString(
                    ResourceUtils.getFile("classpath:db/rpm-db-init.sql").toPath());
            // Terminate active sessions to the target database so that we can make a DROP
            // language=PostgreSQL
            stmt.execute("""
                    SELECT PG_TERMINATE_BACKEND(pid)
                    FROM pg_stat_activity WHERE datname = '%s'""".formatted(Constants.TARGET_DB_NAME));
            stmt.execute(initSql);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to recreate database", e);
        }
    }

    private void setSystemSpringBootDbProperties(String jdbcUrl) {
        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", Constants.DB_USER);
        System.setProperty("spring.datasource.password", Constants.DB_PASSWORD);
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
    }
}
