package by.iivanov.rpm.testing;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a single in-JVM GreenMail SMTP server, kept alive for the whole test run.
 *
 * <p>GreenMail runs inside the test JVM and binds the loopback interface directly, so there is no Docker
 * Desktop port-proxy in the SMTP path and the server-speaks-first {@code 220} greeting is delivered
 * instantly. The server is started idempotently and reused across all mail-tagged tests, mirroring the
 * shared-instance lifecycle of the database test infrastructure — only in-process.
 */
public final class GreenMailServer {

    // 127.0.0.1, not "localhost": keep the SMTP path on IPv4 loopback, consistent with the rest of the
    // mail test setup. The literal loopback IP is intentional here.
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public static final String HOST = "127.0.0.1";

    // Fixed loopback SMTP port the in-JVM GreenMail binds. Deliberately in the registered-port range
    // (1024-49151), BELOW the ephemeral range (49152+) that Windows lets Hyper-V/WSL/Docker carve into
    // dynamic reserved ranges. A port in those reserved ranges fails to bind with "Address already in
    // use" even when nothing listens (netsh int ipv4 show excludedportrange), which silently breaks the
    // whole mail suite — exactly the Windows networking trap this in-JVM setup exists to avoid.
    public static final int SMTP_PORT = 33025;

    private static final Logger log = LoggerFactory.getLogger(GreenMailServer.class);

    private static final GreenMail GREEN_MAIL =
            new GreenMail(new ServerSetup(SMTP_PORT, HOST, ServerSetup.PROTOCOL_SMTP));

    private GreenMailServer() {}

    /**
     * Starts the in-JVM GreenMail SMTP server if it is not already running.
     *
     * @return the running GreenMail instance
     */
    // Negation-first is intentional: the "start if not already running" idiom keeps the meaningful
    // action (start) in the main branch; flipping it would bury start() in the else.
    // Method-level synchronized is intentional: a one-shot idempotent singleton start whose whole body is
    // the critical section — not a hot path, no virtual threads; a ReentrantLock would add only boilerplate.
    @SuppressWarnings({"PMD.ConfusingTernary", "PMD.AvoidSynchronizedAtMethodLevel"})
    public static synchronized GreenMail start() {
        if (!GREEN_MAIL.isRunning()) {
            GREEN_MAIL.start();
            log.info("[testing-support] In-JVM GreenMail started: smtp={}:{}", HOST, SMTP_PORT);
        } else {
            log.info("[testing-support] In-JVM GreenMail already running (reuse)");
        }
        return GREEN_MAIL;
    }

    /**
     * Returns the shared in-JVM GreenMail instance for delivery assertions.
     *
     * @return the GreenMail instance
     */
    public static GreenMail instance() {
        return GREEN_MAIL;
    }
}
