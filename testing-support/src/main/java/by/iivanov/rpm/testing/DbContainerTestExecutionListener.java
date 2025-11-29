package by.iivanov.rpm.testing;

import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbContainerTestExecutionListener implements TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DbContainerTestExecutionListener.class);
    private static final String PROP_DB_TAG = "db.tests.tag";

    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static final String LOCAL_DB_HOST_URL = "jdbc:postgresql://localhost:54034/";
    private static final String TARGET_DB_NAME = "rpm_ddd";

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        String dbTag = System.getProperty(PROP_DB_TAG, "db");
        long count = testPlan.countTestIdentifiers(ti -> hasDbTag(ti, dbTag));
        log.info("[testing-support] testPlanExecutionStarted; tag='{}', matched tests={}.", dbTag, count);
        if (count > 0) {
            String jdbcUrl = resolveAndPrepareDb();

            // Устанавливаем системные свойства, которые Spring Boot подхватит при старте
            System.setProperty("spring.datasource.url", jdbcUrl);
            System.setProperty("spring.datasource.username", DB_USER);
            System.setProperty("spring.datasource.password", DB_PASSWORD);
            System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

            log.info("[testing-support] DB properties set. URL: {}", jdbcUrl);
        }
    }

    private boolean hasDbTag(TestIdentifier testIdentifier, String tagName) {
        if (!testIdentifier.isTest()) {
            return false;
        }
        for (TestTag tag : testIdentifier.getTags()) {
            if (tag.getName().equals(tagName)) {
                return true;
            }
        }
        return false;
    }

    private String resolveAndPrepareDb() {
        String adminUrl = LOCAL_DB_HOST_URL + "postgres";
        String targetUrl = LOCAL_DB_HOST_URL + TARGET_DB_NAME;

        log.info("Checking for local db server at {}", LOCAL_DB_HOST_URL);

        try (var con = DriverManager.getConnection(adminUrl, DB_USER, DB_PASSWORD)) {
            log.info("Local db server found. Recreating database '{}'...", TARGET_DB_NAME);

            try (var stmt = con.createStatement()) {
                String initSql = Files.readString(
                        ResourceUtils.getFile("classpath:db/rpm-db-init.sql").toPath());
                // Завершаем активные сессии к целевой БД, чтобы можно было сделать DROP
                stmt.execute("""
                        SELECT pg_terminate_backend(pid)
                        FROM pg_stat_activity WHERE datname = '%s'""".formatted(TARGET_DB_NAME));
                stmt.execute(initSql);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            log.info("Database '{}' recreated successfully.", TARGET_DB_NAME);
            return targetUrl;

        } catch (SQLException e) {
            log.warn("Local Db server not found or connection failed ({}). Starting Testcontainer...", e.getMessage());
            return PostgresContainersLifecycleManager.init().getJdbcUrl();
        }
    }
}
