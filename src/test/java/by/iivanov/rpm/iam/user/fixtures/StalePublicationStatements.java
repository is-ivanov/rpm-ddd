package by.iivanov.rpm.iam.user.fixtures;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import by.iivanov.rpm.iam.user.domain.UserRegisteredEvent;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.modulith.events.core.TargetEventPublication;
import org.springframework.stereotype.Component;
import org.threeten.extra.MutableClock;

/**
 * Statements for the resubmit age-cutoff scenario: arming a deterministic first-send failure so an
 * activation-email publication stays incomplete, and asserting the stale publication is neither
 * resubmitted (stays incomplete) nor redelivered (no email reaches GreenMail).
 *
 * <p>Reads the Spring Modulith event publication registry and the GreenMail inbox; it owns no HTTP
 * calls. The SMTP failure is injected by stubbing the shared {@link JavaMailSender} spy (declared on
 * the mail integration base) — no production wiring is replaced. The publication's age is controlled
 * exclusively through the test {@code clock} the registry reads — registry rows are never mutated to
 * fake a timestamp.
 */
@Component
public class StalePublicationStatements {

    private static final Duration INCOMPLETE_WAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration INCOMPLETE_POLL_INTERVAL = Duration.ofMillis(250);
    private static final Duration NO_DELIVERY_WINDOW = Duration.ofSeconds(15);
    private static final Duration NO_DELIVERY_POLL_INTERVAL = Duration.ofMillis(250);

    private final JavaMailSender mailSender;
    private final EventPublicationRegistry eventPublicationRegistry;
    private final GreenMailTestClient greenMailTestClient;
    private final MutableClock clock;

    /** Constructor. */
    public StalePublicationStatements(
            JavaMailSender mailSender,
            EventPublicationRegistry eventPublicationRegistry,
            GreenMailTestClient greenMailTestClient,
            MutableClock clock) {
        this.mailSender = mailSender;
        this.eventPublicationRegistry = eventPublicationRegistry;
        this.greenMailTestClient = greenMailTestClient;
        this.clock = clock;
    }

    /**
     * Stubs the SMTP transport so the first send (the original activation delivery) fails — leaving the
     * publication incomplete — while every later send succeeds. This makes the failure deterministic
     * regardless of when the async listener actually attempts the send: a redelivery (resubmit) would
     * therefore reach the real transport and deliver, so the no-delivery assertion keeps its teeth.
     */
    public void givenActivationSendFails() {
        doThrow(new MailSendException("Simulated SMTP failure for activation email"))
                .doCallRealMethod()
                .when(mailSender)
                .send(any(MimeMessage.class));
    }

    /**
     * Waits until the activation-email publication for the given recipient is incomplete, then advances
     * the test clock past the 24h resubmit cutoff so the publication is stale.
     *
     * @param recipientEmail the recipient whose activation publication must become incomplete
     */
    public void givenStaleIncompletePublicationFor(String recipientEmail) {
        awaitIncompletePublicationFor(recipientEmail);
        clock.add(Duration.ofHours(25));
    }

    /**
     * Waits until the activation-email publication for the given recipient is incomplete, without
     * advancing the clock — so the publication stays young (within the 24h resubmit window). This
     * confirms the original send failed before the test resubmits, race-free.
     *
     * @param recipientEmail the recipient whose activation publication must become incomplete
     */
    public void givenYoungIncompletePublicationFor(String recipientEmail) {
        awaitIncompletePublicationFor(recipientEmail);
    }

    /**
     * Asserts exactly one incomplete activation-email publication for the recipient remains in the
     * registry — i.e. the stale publication was not resubmitted to completion.
     *
     * @param recipientEmail the recipient whose stale publication must stay incomplete
     */
    public void assertActivationPublicationStaysIncompleteFor(String recipientEmail) {
        then(incompletePublicationsFor(recipientEmail))
                .as("Incomplete activation-email publications for %s", recipientEmail)
                .hasSize(1);
    }

    /**
     * Asserts no activation email is delivered to the recipient throughout a bounded window. Resubmit
     * reprocessing is asynchronous, so the inbox is polled for the whole window and the assertion fails
     * the instant a message appears — proving the stale publication was never redelivered.
     *
     * @param recipientEmail the recipient that must receive no activation email
     */
    public void assertNoActivationEmailDeliveredTo(String recipientEmail) {
        Awaitility.await()
                .during(NO_DELIVERY_WINDOW)
                .atMost(NO_DELIVERY_WINDOW.plusSeconds(1))
                .pollInterval(NO_DELIVERY_POLL_INTERVAL)
                .untilAsserted(() -> then(greenMailTestClient.countMessagesDeliveredTo(recipientEmail))
                        .as("Activation emails delivered to %s during the no-delivery window", recipientEmail)
                        .isZero());
    }

    private void awaitIncompletePublicationFor(String recipientEmail) {
        Awaitility.await()
                .atMost(INCOMPLETE_WAIT_TIMEOUT)
                .pollInterval(INCOMPLETE_POLL_INTERVAL)
                .until(() -> !incompletePublicationsFor(recipientEmail).isEmpty());
    }

    private List<TargetEventPublication> incompletePublicationsFor(String recipientEmail) {
        return eventPublicationRegistry.findIncompletePublications().stream()
                .filter(publication -> isActivationFor(publication, recipientEmail))
                .toList();
    }

    private static boolean isActivationFor(TargetEventPublication publication, String recipientEmail) {
        return publication.getEvent() instanceof UserRegisteredEvent event
                && recipientEmail.equals(event.email().email());
    }
}
