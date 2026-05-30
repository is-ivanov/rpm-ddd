package by.iivanov.rpm.testing;

import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import com.github.dockerjava.api.model.HostConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing the lifecycle of a Mailpit container used for testing purposes.
 * The container is initialized and started in a reusable, idempotent way, mirroring
 * {@link PostgresContainersLifecycleManager}.
 *
 * <p>Configuration is loaded from {@code docker/.env} in the project root. The image is read from
 * {@code MAILPIT_TEST_IMAGE} so the Testcontainer and {@code docker/infra-tests.yml} stay in sync.
 *
 * <p>The library {@code @ServiceConnection} support is deliberately skipped — the shared-instance
 * reuse logic lives in {@link MailpitContainerTestExecutionListener}, consistent with the database setup.
 */
public final class MailpitContainersLifecycleManager {

    public static final String MAILPIT_IMAGE;

    private static final String ENV_FILE_PATH = "docker/.env";
    private static final String IMAGE_PROPERTY = "MAILPIT_TEST_IMAGE";
    private static final String DEFAULT_IMAGE = "axllent/mailpit:v1.21";

    private static final Logger log = LoggerFactory.getLogger(MailpitContainersLifecycleManager.class);

    static {
        var env = loadEnvFile();
        MAILPIT_IMAGE = env.getProperty(IMAGE_PROPERTY, DEFAULT_IMAGE);
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

    private static final MailpitContainer MAILPIT_CONTAINER;

    static {
        MAILPIT_CONTAINER = new MailpitContainer(MAILPIT_IMAGE).withReuse(true).withCreateContainerCmdModifier(cmd -> {
            HostConfig hostConfig = cmd.getHostConfig();
            if (hostConfig != null) {
                hostConfig.withAutoRemove(true);
            }
        });
    }

    /**
     * Initializes and starts a Mailpit container for testing purposes.
     * If the container is already running, it will be reused without restarting.
     *
     * @return an instance of {@code MailpitContainer} representing the initialized container.
     */
    public static synchronized MailpitContainer init() {
        if (!MAILPIT_CONTAINER.isRunning()) {
            log.info("[testing-support] Starting Mailpit container: image={}", MAILPIT_IMAGE);
            MAILPIT_CONTAINER.start();
            log.info(
                    "[testing-support] Mailpit started: smtp={}:{} http={}",
                    MAILPIT_CONTAINER.getSmtpHost(),
                    MAILPIT_CONTAINER.getSmtpPort(),
                    MAILPIT_CONTAINER.getHttpUrl());
        } else {
            log.info("[testing-support] Mailpit container already running (reuse)");
        }
        return MAILPIT_CONTAINER;
    }

    private MailpitContainersLifecycleManager() {}
}
