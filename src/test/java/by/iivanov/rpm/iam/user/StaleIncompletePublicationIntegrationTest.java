package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.StalePublicationStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaleIncompletePublicationIntegrationTest extends AbstractMailIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;
    private final StalePublicationStatements stalePublicationStatements;

    StaleIncompletePublicationIntegrationTest(
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
    @DisplayName("GIVEN an incomplete activation-email publication older than 24 hours WHEN the resubmit "
            + "scheduler runs THEN the stale publication is not resubmitted "
            + "AND no activation email is delivered for it")
    void when_resubmitSchedulerRuns_expect_stalePublicationNotResubmitted_andNoEmailDelivered() {
        // GIVEN: an activation-email publication is left incomplete and aged past the 24h resubmit cutoff
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();
        stalePublicationStatements.givenActivationSendFails();
        userApi.registerUser(registration.request(), admin).assertCreated();
        stalePublicationStatements.givenStaleIncompletePublicationFor(registration.email());

        // WHEN: the resubmit scheduler runs
        emailStatements.whenResubmitSchedulerRuns();

        // THEN: no activation email is delivered for it (asserted across a bounded window)
        stalePublicationStatements.assertNoActivationEmailDeliveredTo(registration.email());

        // AND: the stale publication is not resubmitted — it stays incomplete in the registry
        stalePublicationStatements.assertActivationPublicationStaysIncompleteFor(registration.email());
    }
}
