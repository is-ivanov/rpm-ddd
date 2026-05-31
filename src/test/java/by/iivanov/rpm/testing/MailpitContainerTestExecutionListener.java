package by.iivanov.rpm.testing;

import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TestExecutionListener} that prepares a Mailpit SMTP server for tests tagged {@code mail}.
 *
 * <p>Mirrors {@link DbContainerTestExecutionListener}: it probes the shared Mailpit started by
 * {@code Infra-Tests-Up} (via {@code docker/infra-tests.yml}) at {@code localhost}, reuses it when
 * reachable, and only cold-starts a reusable Testcontainer when the shared instance is unavailable.
 *
 * <p>It exports the SMTP coordinates as {@code spring.mail.*} system properties and the Mailpit REST
 * API base URL as {@code mailpit.api.url} so the test client can poll delivered messages. The library's
 * {@code @ServiceConnection} is intentionally not used so the shared-instance reuse logic stays here.
 */
public class MailpitContainerTestExecutionListener implements TestExecutionListener {

    /** System property exposing the Mailpit REST API base URL to test fixtures. */
    public static final String MAILPIT_API_URL_PROPERTY = "mailpit.api.url";

    private static final Logger log = LoggerFactory.getLogger(MailpitContainerTestExecutionListener.class);

    // 127.0.0.1, not "localhost": on Windows "localhost" resolves to ::1 first, and the IPv6 path to
    // the Docker-mapped Mailpit ports stalls (~21s) before falling back to IPv4. Pinning IPv4 avoids it
    // for both the SMTP send and the REST API polling. The literal loopback IP is intentional here.
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String SHARED_HOST = "127.0.0.1";

    private static final int SHARED_SMTP_PORT = 54025;
    private static final int SHARED_HTTP_PORT = 54825;
    private static final String SHARED_API_URL = "http://" + SHARED_HOST + ":" + SHARED_HTTP_PORT;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        long count = testPlan.countTestIdentifiers(this::hasMailTag);
        log.info("[testing-support] mail testPlanExecutionStarted; matched tests={}.", count);
        if (count > 0) {
            resolveAndPrepareMailpit();
        }
    }

    private boolean hasMailTag(TestIdentifier testIdentifier) {
        if (!testIdentifier.isTest()) {
            return false;
        }
        return testIdentifier.getTags().stream().anyMatch(tag -> tag.getName().equals(Constants.MAIL_TEST_TAG));
    }

    // The Testcontainer is a singleton kept alive for reuse across the whole test run (mirrors
    // the DB container lifecycle); it must NOT be closed here, so try-with-resources is wrong.
    @SuppressWarnings("resource")
    private void resolveAndPrepareMailpit() {
        if (isSharedMailpitAlive()) {
            log.info("[testing-support] Shared Mailpit found at {}. Reusing it.", SHARED_API_URL);
            setSystemMailProperties(SHARED_HOST, SHARED_SMTP_PORT, SHARED_API_URL);
        } else {
            log.warn("[testing-support] Shared Mailpit not reachable at {}. Starting Testcontainer...", SHARED_API_URL);
            MailpitContainer container = MailpitContainersLifecycleManager.init();
            setSystemMailProperties(container.getSmtpHost(), container.getSmtpPort(), container.getHttpUrl());
        }
    }

    private boolean isSharedMailpitAlive() {
        try (HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SHARED_API_URL + "/api/v1/messages"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.info("[testing-support] Shared Mailpit probe failed: {}", e.getMessage());
            return false;
        }
    }

    private void setSystemMailProperties(String host, int smtpPort, String apiUrl) {
        System.setProperty("spring.mail.host", host);
        System.setProperty("spring.mail.port", String.valueOf(smtpPort));
        System.setProperty(MAILPIT_API_URL_PROPERTY, apiUrl);
        log.info("[testing-support] Mail properties set. SMTP {}:{} API {}", host, smtpPort, apiUrl);
    }
}
