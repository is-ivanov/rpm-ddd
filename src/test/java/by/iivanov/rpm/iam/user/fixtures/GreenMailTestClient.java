package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.testing.GreenMailServer;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.awaitility.Awaitility;
import org.springframework.stereotype.Component;

/**
 * Application client for the acceptance suite that reads delivered mail from the in-JVM
 * {@link GreenMailServer}.
 *
 * <p>Owns message polling, recipient filtering, MIME parsing (decoded {@code text/html} extraction) and
 * inbox reset. Statements orchestrate setup/assertions through this client and the {@link DeliveredEmail}
 * snapshot it returns — they never touch the JavaMail API directly.
 */
@Component
public class GreenMailTestClient {

    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private final GreenMail greenMail = GreenMailServer.instance();

    /** Removes all captured messages so assertions stay deterministic across the shared in-JVM server. */
    public void clearInbox() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
        } catch (FolderException e) {
            throw new IllegalStateException("Failed to clear the GreenMail inbox", e);
        }
    }

    /**
     * Polls GreenMail until a message addressed to the given recipient is delivered, then returns its
     * parsed snapshot.
     *
     * @param recipientEmail the expected recipient email address
     * @return the delivered email snapshot
     */
    public DeliveredEmail awaitMessageDeliveredTo(String recipientEmail) {
        Awaitility.await()
                .atMost(AWAIT_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> countMessagesDeliveredTo(recipientEmail) > 0);
        return snapshotOf(firstMessageTo(recipientEmail));
    }

    /**
     * Counts how many delivered messages are addressed to the given recipient.
     *
     * @param recipientEmail the recipient email address to count messages for
     * @return the number of delivered messages addressed to the recipient
     */
    public long countMessagesDeliveredTo(String recipientEmail) {
        return receivedMessages()
                .filter(message -> isAddressedTo(message, recipientEmail))
                .count();
    }

    private Stream<MimeMessage> receivedMessages() {
        return Arrays.stream(greenMail.getReceivedMessages());
    }

    private MimeMessage firstMessageTo(String recipientEmail) {
        return receivedMessages()
                .filter(message -> isAddressedTo(message, recipientEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No message delivered to " + recipientEmail));
    }

    private boolean isAddressedTo(MimeMessage message, String recipientEmail) {
        return recipientsOf(message).contains(recipientEmail);
    }

    private DeliveredEmail snapshotOf(MimeMessage message) {
        try {
            InternetAddress from = (InternetAddress) message.getFrom()[0];
            String fromName = from.getPersonal() == null ? "" : from.getPersonal();
            return new DeliveredEmail(
                    fromName, from.getAddress(), recipientsOf(message), message.getSubject(), htmlBodyOf(message));
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to read the delivered message", e);
        }
    }

    private List<String> recipientsOf(MimeMessage message) {
        try {
            Address[] recipients = message.getAllRecipients();
            return recipients == null
                    ? List.of()
                    : Arrays.stream(recipients)
                            .filter(InternetAddress.class::isInstance)
                            .map(address -> ((InternetAddress) address).getAddress())
                            .toList();
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to read message recipients", e);
        }
    }

    private static String htmlBodyOf(MimeMessage message) {
        try {
            return htmlParts(message).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No text/html part in message"));
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Failed to read the message HTML body", e);
        }
    }

    private static List<String> htmlParts(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/html")) {
            return List.of((String) part.getContent());
        }
        if (part.getContent() instanceof Multipart multipart) {
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < multipart.getCount(); i++) {
                parts.addAll(htmlParts(multipart.getBodyPart(i)));
            }
            return parts;
        }
        return List.of();
    }
}
