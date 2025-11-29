package by.iivanov.rpm.testing;

import com.github.dockerjava.api.model.HostConfig;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Slf4j
@UtilityClass
public final class PostgresContainersLifecycleManager {

    private static final String POSTGRES_IMAGE = "postgres:18.1";

    private static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName("rpm_ddd")
            .withUsername("postgres")
            .withPassword("postgres")
            .withExposedPorts(5432)
            .withTmpFs(Map.of("/var", "rw"))
            .withReuse(true)
            .withCreateContainerCmdModifier(cmd -> {
                HostConfig hostConfig = cmd.getHostConfig();
                log.info("HostConfig exists {}", hostConfig != null);
                if (hostConfig != null) {
                    hostConfig.withAutoRemove(true);
                }
            });

    /**
     * Initialize and start a container if needed (idempotent).
     */
    public static synchronized PostgreSQLContainer init() {
        if (!POSTGRES_CONTAINER.isRunning()) {
            log.info("[testing-support] Starting reusable Postgres container...");
            POSTGRES_CONTAINER.start();
            log.info(
                    "[testing-support] Postgres container started: {}:{} -> db={} user={}",
                    POSTGRES_CONTAINER.getHost(),
                    POSTGRES_CONTAINER.getMappedPort(5432),
                    POSTGRES_CONTAINER.getDatabaseName(),
                    POSTGRES_CONTAINER.getUsername());
        } else {
            log.info("[testing-support] Postgres container already running (reuse). Skipping start.");
        }
        return POSTGRES_CONTAINER;
    }

}
