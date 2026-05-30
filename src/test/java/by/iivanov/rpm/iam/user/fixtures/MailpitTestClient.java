package by.iivanov.rpm.iam.user.fixtures;

import by.iivanov.rpm.testing.MailpitContainerTestExecutionListener;
import ch.martinelli.oss.testcontainers.mailpit.MailpitClient;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import ch.martinelli.oss.testcontainers.mailpit.assertions.MessageAwaiter;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Application client for the acceptance suite that wraps the Mailpit REST API.
 *
 * <p>Owns the base-URL resolution (from the {@code mailpit.api.url} system property set by the
 * {@link MailpitContainerTestExecutionListener}), message polling, and inbox reset. Statements
 * orchestrate setup/assertions through this client — they never call the Mailpit API directly.
 */
@Component
public class MailpitTestClient {

    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private final MailpitClient client;

    public MailpitTestClient() {
        var baseUrl = System.getProperty(MailpitContainerTestExecutionListener.MAILPIT_API_URL_PROPERTY);
        this.client = new MailpitClient(baseUrl);
    }

    /** Removes all captured messages so assertions stay deterministic across the shared instance. */
    public void clearInbox() {
        client.deleteAllMessages();
    }

    /**
     * Polls Mailpit until a message addressed to the given recipient is delivered, then returns it.
     *
     * @param recipientEmail the expected recipient email address
     * @return the delivered {@link Message}
     */
    public Message awaitMessageDeliveredTo(String recipientEmail) {
        new MessageAwaiter(client, AWAIT_TIMEOUT, POLL_INTERVAL)
                .to(recipientEmail)
                .isPresent();
        return client.getAllMessages().stream()
                .filter(message ->
                        message.recipients().stream().anyMatch(address -> recipientEmail.equals(address.address())))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Returns the rendered HTML body of the given message.
     *
     * @param message the delivered message
     * @return the HTML body
     */
    public String htmlBodyOf(Message message) {
        return client.getMessageHtml(message.id());
    }
}
