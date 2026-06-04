package by.iivanov.rpm.testing;

import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Fast smoke canary for the mail test harness itself — NOT a coverage test for production code.
 *
 * <p>It verifies the one thing every {@code mail}-tagged integration test silently depends on but none
 * asserts in isolation: that the in-JVM {@link GreenMailServer} actually binds its loopback SMTP port,
 * accepts a JavaMail send, and exposes the delivered message through the GreenMail Java API. The full
 * activation-email flow (production {@code SmtpEmailNotificationSender} → this server) is exercised by
 * the integration suite and is a strict superset of what this asserts.
 *
 * <p>Its value is diagnostic isolation and speed: when the harness breaks (e.g. the fixed SMTP port
 * lands in a Windows reserved range and cannot bind), this fails in ~0.4s with a clear {@code BindException},
 * pinpointing "the mail infrastructure is broken" — whereas the full-context mail tests would fail ~15s
 * later with an opaque {@code ConditionTimeout} that looks like an application bug. Standalone (no Spring
 * context, no {@code mail} tag) so it stays fast and independent of the shared mail infrastructure.
 */
class GreenMailServerTest {

    private static final String FROM = "no-reply@rpm-platform.com";
    private static final String RECIPIENT = "user@example.com";
    private static final String SUBJECT = "Activate your RPM account";
    private static final String ACTIVATION_LINK = "http://localhost:5173/activate?token=abc";

    private static final GreenMail GREEN_MAIL = GreenMailServer.start();
    private static final JavaMailSenderImpl MAIL_SENDER = newSender();

    private static JavaMailSenderImpl newSender() {
        var sender = new JavaMailSenderImpl();
        sender.setHost(GreenMailServer.HOST);
        sender.setPort(GreenMailServer.SMTP_PORT);
        return sender;
    }

    @AfterAll
    static void purge() throws FolderException {
        GREEN_MAIL.purgeEmailFromAllMailboxes();
    }

    @Test
    @DisplayName("In-JVM GreenMail receives a JavaMail message over loopback and exposes it via getReceivedMessages")
    void should_receive_message_sent_over_loopback() throws Exception {
        MimeMessage message = MAIL_SENDER.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(FROM);
        helper.setTo(RECIPIENT);
        helper.setSubject(SUBJECT);
        helper.setText("<a href=\"" + ACTIVATION_LINK + "\">Activate</a>", true);

        MAIL_SENDER.send(message);
        boolean delivered = GREEN_MAIL.waitForIncomingEmail(2_000L, 1);

        assertThat(delivered).as("message delivered to in-JVM GreenMail").isTrue();
        MimeMessage[] received = GREEN_MAIL.getReceivedMessages();
        assertThat(received).hasSize(1);
        assertThat(received[0].getSubject()).isEqualTo(SUBJECT);
        assertThat(received[0].getAllRecipients()[0]).hasToString(RECIPIENT);
        assertThat(GreenMailUtil.getBody(received[0])).contains(ACTIVATION_LINK);
    }
}
