package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.StalePublicationStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractMailIntegrationTest;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InFlightIncompletePublicationIntegrationTest extends AbstractMailIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;
    private final StalePublicationStatements stalePublicationStatements;

    InFlightIncompletePublicationIntegrationTest(
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
    @Issue("148")
    @DisplayName("GIVEN an in-flight activation-email publication younger than the grace period WHEN the resubmit "
            + "scheduler runs THEN the in-flight publication is not resubmitted "
            + "AND no duplicate activation email is delivered for it")
    void when_resubmitSchedulerRuns_expect_inFlightPublicationNotResubmitted_andNoDuplicateEmailDelivered() {
        // GIVEN: an activation-email publication is left incomplete and is still young (within the grace window)
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();
        stalePublicationStatements.givenActivationSendFails();
        userApi.registerUser(registration.request(), admin).assertCreated();
        stalePublicationStatements.givenYoungIncompletePublicationFor(registration.email());

        // WHEN: resubmit scheduler runs
        emailStatements.whenResubmitSchedulerRuns();

        // THEN: no activation email is delivered for it (asserted across a bounded window)
        stalePublicationStatements.assertNoActivationEmailDeliveredTo(registration.email());

        // AND: the in-flight publication is not resubmitted — it stays incomplete in the registry
        stalePublicationStatements.assertActivationPublicationStaysIncompleteFor(registration.email());
    }
}
