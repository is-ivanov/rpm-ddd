package by.iivanov.rpm.testing;

import com.github.dockerjava.api.model.HostConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataSize;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Utility class for managing the lifecycle of a PostgreSQL container used for testing purposes.
 * The container is initialized and started in a reusable, idempotent way, ensuring consistent
 * setup for integration tests.
 *
 * <p>Configuration is loaded from {@code docker/.env} file in project root.
 * All {@code POSTGRES_*} variables (except {@code POSTGRES_TEST_IMAGE}) are converted
 * to PostgreSQL command-line flags. For example:
 * <ul>
 *   <li>{@code POSTGRES_FSYNC=off} becomes {@code -c fsync=off}</li>
 *   <li>{@code POSTGRES_SHARED_BUFFERS=128MB} becomes {@code -c shared_buffers=128MB}</li>
 * </ul>
 *
 * <p>Key Features:
 * <ul>
 *   <li>Single instance of PostgreSQL container with idempotent initialization</li>
 *   <li>Configuration from {@code docker/.env} file - sync between testcontainers and docker-compose</li>
 *   <li>Tuned for test performance (fsync=off, autovacuum=off, etc.)</li>
 *   <li>tmpfs for in-memory data, shared memory 256MB</li>
 *   <li>Reusable across test runs via testcontainers reuse feature</li>
 * </ul>
 */
public final class PostgresContainersLifecycleManager {

    public static final String POSTGRES_IMAGE;

    private static final String ENV_FILE_PATH = "docker/.env";
    private static final String IMAGE_PROPERTY = "POSTGRES_TEST_IMAGE";
    private static final String SHARED_MEMORY_PROPERTY = "POSTGRES_SHARED_MEMORY";
    private static final DataSize DEFAULT_SHARED_MEMORY = DataSize.ofMegabytes(256);
    private static final String[] POSTGRES_COMMAND;
    private static final DataSize SHARED_MEMORY_SIZE;

    private static final Logger log = LoggerFactory.getLogger(PostgresContainersLifecycleManager.class);

    static {
        var env = loadEnvFile();
        POSTGRES_IMAGE = env.getProperty(IMAGE_PROPERTY, "postgres:18.3-alpine");
        POSTGRES_COMMAND = buildPostgresCommand(env);
        SHARED_MEMORY_SIZE = parseMemory(env.getProperty(SHARED_MEMORY_PROPERTY, "256MB"));
    }

    private static Properties loadEnvFile() {
        var env = new Properties();
        var envPath = Path.of(ENV_FILE_PATH);

        try {
            if (Files.exists(envPath)) {
                env.load(Files.newInputStream(envPath));
                log.info("[testing-support] Loaded {} entries from {}", env.size(), ENV_FILE_PATH);
            } else {
                log.warn("[testing-support] {} not found, using defaults", ENV_FILE_PATH);
            }
        } catch (IOException e) {
            log.warn("[testing-support] Failed to load {}: {}", ENV_FILE_PATH, e.getMessage());
        }
        return env;
    }

    private static String[] buildPostgresCommand(Properties env) {
        var args = new ArrayList<String>();
        args.add("postgres");

        for (var key : env.stringPropertyNames()) {
            if (key.startsWith("POSTGRES_") && !key.equals(IMAGE_PROPERTY)) {
                // POSTGRES_FSYNC=off -> -c fsync=off
                var pgParam = key.substring("POSTGRES_".length()).toLowerCase();
                args.add("-c");
                args.add(pgParam + "=" + env.getProperty(key));
            }
        }

        return args.toArray(new String[0]);
    }

    private static DataSize parseMemory(String value) {
        try {
            return DataSize.parse(value);
        } catch (IllegalArgumentException _) {
            log.warn("[testing-support] Failed to parse shared memory value: {}. Using default value.", value);
            return DEFAULT_SHARED_MEMORY;
        }
    }

    private static final PostgreSQLContainer POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName(Constants.TARGET_DB_NAME)
                .withUsername(Constants.DB_USER)
                .withPassword(Constants.DB_PASSWORD)
                .withCommand(POSTGRES_COMMAND)
                .withTmpFs(Map.of("/var/lib/postgresql", "rw"))
                .withSharedMemorySize(SHARED_MEMORY_SIZE.toBytes())
                .withReuse(true)
                .withCreateContainerCmdModifier(cmd -> {
                    HostConfig hostConfig = cmd.getHostConfig();
                    log.debug("HostConfig: {}", hostConfig != null);
                    if (hostConfig != null) {
                        hostConfig.withAutoRemove(true);
                    }
                });
    }

    /**
     * Initializes and starts a PostgreSQL container for testing purposes.
     * If the container is already running, it will be reused without restarting.
     * Logs the container's status, including host, port, database name, and user credentials.
     *
     * @return an instance of {@code PostgreSQLContainer} representing the initialized container.
     */
    public static synchronized PostgreSQLContainer init() {
        if (!POSTGRES_CONTAINER.isRunning()) {
            log.info("[testing-support] Starting Postgres container: image={}", POSTGRES_IMAGE);
            log.info("[testing-support] Command: {}", String.join(" ", POSTGRES_COMMAND));
            POSTGRES_CONTAINER.start();
            log.info(
                    "[testing-support] Postgres started: {}:{} -> db={} user={}",
                    POSTGRES_CONTAINER.getHost(),
                    POSTGRES_CONTAINER.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                    POSTGRES_CONTAINER.getDatabaseName(),
                    POSTGRES_CONTAINER.getUsername());
        } else {
            log.info("[testing-support] Postgres container already running (reuse)");
        }
        return POSTGRES_CONTAINER;
    }

    private PostgresContainersLifecycleManager() {}
}
