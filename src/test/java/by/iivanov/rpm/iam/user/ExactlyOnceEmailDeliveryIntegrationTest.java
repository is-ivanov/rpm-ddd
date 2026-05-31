package by.iivanov.rpm.iam.user;

import by.iivanov.rpm.iam.auth.fixtures.AuthSessionFactory;
import by.iivanov.rpm.iam.user.fixtures.EmailStatements;
import by.iivanov.rpm.iam.user.fixtures.UserApi;
import by.iivanov.rpm.testing.AbstractApplicationIntegrationTest;
import by.iivanov.rpm.testing.MailTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MailTest
class ExactlyOnceEmailDeliveryIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final EmailStatements emailStatements;

    ExactlyOnceEmailDeliveryIntegrationTest(
            AuthSessionFactory authSessionFactory, UserApi userApi, EmailStatements emailStatements) {
        this.authSessionFactory = authSessionFactory;
        this.userApi = userApi;
        this.emailStatements = emailStatements;
    }

    @Test
    @DisplayName("GIVEN a registration whose activation email is delivered WHEN the resubmit scheduler runs "
            + "EXPECT no additional email AND the publication is complete in the registry")
    void when_resubmitSchedulerRuns_expect_noDuplicateEmail_andPublicationComplete() {
        // GIVEN: a user is registered and the activation email is delivered successfully
        var admin = authSessionFactory.loginAsAdmin();
        emailStatements.givenEmptyInbox();
        var registration = emailStatements.givenActivationRegistration();
        userApi.registerUser(registration.request(), admin).assertCreated();
        emailStatements.assertExactlyOneActivationEmailDeliveredTo(registration.email());

        // WHEN: the resubmit scheduler runs
        emailStatements.whenResubmitSchedulerRuns();

        // THEN: no additional activation email is delivered for that registration
        emailStatements.assertExactlyOneActivationEmailDeliveredTo(registration.email());

        // AND: the event publication is marked complete in the registry
        emailStatements.assertNoIncompletePublications();
    }
}
