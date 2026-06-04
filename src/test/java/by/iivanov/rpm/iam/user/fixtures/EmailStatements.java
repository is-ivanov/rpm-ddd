package by.iivanov.rpm.iam.user.fixtures;

import static by.iivanov.rpm.testing.assertj.DeliveredEmailAssert.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.infrastructure.web.RegisterUserRequest;
import by.iivanov.rpm.shared.infrastructure.events.ResubmitIncompletePublicationsJob;
import java.util.UUID;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.stereotype.Component;

/**
 * Statements for asserting activation-email delivery in acceptance tests.
 *
 * <p>Orchestrates setup (inbox reset) and delivery assertions via {@link GreenMailTestClient}; it owns
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
     * to (and including) {@code token=}. The {@code rpm.frontend-base-url} configured in the {@code test}
     * profile resolves to this value; the link must target the frontend activation page — never the
     * backend {@code /api/auth/activate} JSON endpoint.
     */
    private static final String EXPECTED_ACTIVATION_LINK_PREFIX = "http://localhost:5173/activate?token=";

    private final GreenMailTestClient greenMailTestClient;
    private final ResubmitIncompletePublicationsJob resubmitJob;
    private final EventPublicationRegistry eventPublicationRegistry;

    public EmailStatements(
            GreenMailTestClient greenMailTestClient,
            ResubmitIncompletePublicationsJob resubmitJob,
            EventPublicationRegistry eventPublicationRegistry) {
        this.greenMailTestClient = greenMailTestClient;
        this.resubmitJob = resubmitJob;
        this.eventPublicationRegistry = eventPublicationRegistry;
    }

    /** Clears all previously captured messages so the scenario starts from an empty inbox. */
    public void givenEmptyInbox() {
        greenMailTestClient.clearInbox();
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
        DeliveredEmail email = greenMailTestClient.awaitMessageDeliveredTo(recipientEmail);

        assertThat(email)
                .hasFromName(EXPECTED_FROM_NAME)
                .hasFromAddress(EXPECTED_FROM_ADDRESS)
                .hasOnlyRecipient(recipientEmail)
                .hasSubject(EXPECTED_SUBJECT)
                .hasHtmlBodyContaining(EXPECTED_ACTIVATION_LINK_PREFIX);
    }

    /** Runs the resubmit scheduler once by invoking its scheduled job method directly. */
    public void whenResubmitSchedulerRuns() {
        resubmitJob.resubmit();
    }

    /**
     * Asserts that exactly one activation email is delivered to the given recipient — confirming the
     * resubmit scheduler did not deliver a duplicate for an already-completed publication.
     *
     * @param recipientEmail the recipient address that must hold exactly one activation message
     */
    public void assertExactlyOneActivationEmailDeliveredTo(String recipientEmail) {
        greenMailTestClient.awaitMessageDeliveredTo(recipientEmail);
        then(greenMailTestClient.countMessagesDeliveredTo(recipientEmail))
                .as("Activation emails delivered to %s", recipientEmail)
                .isEqualTo(1L);
    }

    /** Asserts the event publication registry holds no incomplete publication for any listener. */
    public void assertNoIncompletePublications() {
        then(eventPublicationRegistry.findIncompletePublications())
                .as("Incomplete event publications in the registry")
                .isEmpty();
    }
}
