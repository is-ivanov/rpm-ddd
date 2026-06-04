package by.iivanov.rpm.testing;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TestExecutionListener} that starts the in-JVM {@link GreenMailServer} for tests tagged
 * {@code mail} and exports its SMTP coordinates as {@code spring.mail.*} system properties before the
 * Spring context boots.
 *
 * <p>Replaces the Docker-backed {@code MailpitContainerTestExecutionListener}: GreenMail runs in-process
 * over loopback, so there is no Docker Desktop port-proxy and the server-speaks-first {@code 220} greeting
 * is instant. System properties are used (rather than the library {@code @ServiceConnection}) so the
 * coordinates are visible before context creation, mirroring the database listener — and they override the
 * {@code spring.mail.*} defaults in {@code application-test.yml}.
 */
public class GreenMailServerTestExecutionListener implements TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(GreenMailServerTestExecutionListener.class);

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        long count = testPlan.countTestIdentifiers(this::hasMailTag);
        log.info("[testing-support] mail testPlanExecutionStarted; matched tests={}.", count);
        if (count > 0) {
            GreenMailServer.start();
            System.setProperty("spring.mail.host", GreenMailServer.HOST);
            System.setProperty("spring.mail.port", String.valueOf(GreenMailServer.SMTP_PORT));
            log.info(
                    "[testing-support] Mail properties set. SMTP {}:{}",
                    GreenMailServer.HOST,
                    GreenMailServer.SMTP_PORT);
        }
    }

    private boolean hasMailTag(TestIdentifier testIdentifier) {
        if (!testIdentifier.isTest()) {
            return false;
        }
        return testIdentifier.getTags().stream().anyMatch(tag -> tag.getName().equals(Constants.MAIL_TEST_TAG));
    }
}
