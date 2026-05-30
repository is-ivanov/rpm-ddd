package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Statements for asserting activation-email delivery in acceptance tests.
 *
 * <p>Orchestrates setup (inbox reset) and delivery assertions via {@link MailpitTestClient}; it owns
 * no HTTP calls itself.
 */
@Component
public class EmailStatements {

    private static final String EXPECTED_FROM_NAME = "RPM Platform";
    private static final String EXPECTED_FROM_ADDRESS = "no-reply@rpm-platform.com";
    private static final String EXPECTED_SUBJECT = "Activate your RPM account";

    /**
     * The exact, deterministic prefix of the activation link in the email body. The token suffix is a
     * freshly-signed JWT and is the only non-deterministic part, so the assertion bounds everything up
     * to (and including) {@code token=}. The {@code app.frontend-base-url} configured in the {@code test}
     * profile resolves to this value; the link must target the frontend activation page — never the
     * backend {@code /api/auth/activate} JSON endpoint.
     */
    private static final String EXPECTED_ACTIVATION_LINK_PREFIX = "http://localhost:5173/activate?token=";

    private final MailpitTestClient mailpitTestClient;

    public EmailStatements(MailpitTestClient mailpitTestClient) {
        this.mailpitTestClient = mailpitTestClient;
    }

    /** Clears all previously captured messages so the scenario starts from an empty inbox. */
    public void givenEmptyInbox() {
        mailpitTestClient.clearInbox();
    }

    /**
     * Builds a valid registration request targeting a freshly-unique recipient address, so the
     * activation email assertion can match exactly one message regardless of other test data.
     *
     * @return the prepared request paired with the recipient address it will activate
     */
    public ActivationRegistration givenActivationRegistration() {
        var uniqueSuffix = UUID.randomUUID().toString();
        var email = "activation_" + uniqueSuffix + "@example.com";
        var request = new RegisterUserRequest("Ivan", "Ivanovich", "Ivanov", "act_" + uniqueSuffix, email);
        return new ActivationRegistration(request, email);
    }

    /**
     * A prepared registration request together with the recipient address it activates.
     *
     * @param request the registration payload to submit
     * @param email the recipient address the activation message must reach
     */
    public record ActivationRegistration(RegisterUserRequest request, String email) {}

    /**
     * Asserts that a single activation email is delivered to the given recipient with the configured
     * from-address, subject, and an activation link carrying the token.
     *
     * @param recipientEmail the email address the activation message must be delivered to
     */
    public void assertActivationEmailDeliveredTo(String recipientEmail) {
        Message message = mailpitTestClient.awaitMessageDeliveredTo(recipientEmail);

        then(message.from().name()).as("Activation email from-name").isEqualTo(EXPECTED_FROM_NAME);
        then(message.from().address()).as("Activation email from-address").isEqualTo(EXPECTED_FROM_ADDRESS);
        then(message.recipients())
                .as("Activation email recipient")
                .extracting("address")
                .containsExactly(recipientEmail);
        then(message.subject()).as("Activation email subject").isEqualTo(EXPECTED_SUBJECT);
        then(mailpitTestClient.htmlBodyOf(message))
                .as("Activation email body must contain the frontend activation link prefix with the token")
                .contains(EXPECTED_ACTIVATION_LINK_PREFIX);
    }
}
