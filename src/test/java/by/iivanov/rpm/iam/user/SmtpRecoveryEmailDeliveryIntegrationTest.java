package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.StalePublicationStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SmtpRecoveryEmailDeliveryIntegrationTest extends AbstractMailIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;
    private final StalePublicationStatements stalePublicationStatements;

    SmtpRecoveryEmailDeliveryIntegrationTest(
            AuthSessionFactory authSessionFactory,
            UserApi userApi,
            EmailStatements emailStatements,
            StalePublicationStatements stalePublicationStatements) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.emailStatements = emailStatements;
        this.stalePublicationStatements = stalePublicationStatements;
    }

    @Test
    @DisplayName("GIVEN the SMTP server was unavailable when a user registered AND the activation email "
            + "publication remains incomplete WHEN the SMTP server becomes available THEN the activation "
            + "email is delivered to the registered email address without re-registering the user")
    void when_smtpRecovers_expect_youngIncompletePublicationRedelivered() {
        // GIVEN: the SMTP server was unavailable when a user registered, leaving the activation-email
        // publication incomplete and now older than the grace period (but within the 24h resubmit window)
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();
        stalePublicationStatements.givenActivationSendFails();
        userApi.registerUser(registration.request(), admin).assertCreated();
        stalePublicationStatements.givenIncompletePublicationOlderThanGraceFor(registration.email());

        // WHEN: the SMTP server becomes available — the resubmit scheduler reprocesses the incomplete
        // publication and the now-recovered transport delivers the activation email
        emailStatements.whenResubmitSchedulerRuns();

        // THEN: the activation email is delivered to the registered recipient, without re-registering
        emailStatements.assertActivationEmailDeliveredTo(registration.email());
    }
}
