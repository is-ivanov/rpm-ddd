package by.iivanov.rpm.testing.assertj;

import by.iivanov.rpm.iam.user.fixtures.DeliveredEmail;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/// Custom AssertJ assertions for [DeliveredEmail], so Statements assert on a delivered message's
/// decoded fields fluently without re-deriving each comparison by hand.
/// ```
/// assertThat(email)
///     .hasFromName("RPM Platform")
///     .hasFromAddress("no-reply@rpm-platform.com")
///     .hasOnlyRecipient("user@example.com")
///     .hasSubject("Activate your RPM account")
///     .hasHtmlBodyContaining("/activate?token=");
/// ```
public class DeliveredEmailAssert extends AbstractAssert<DeliveredEmailAssert, DeliveredEmail> {

    protected DeliveredEmailAssert(DeliveredEmail actual) {
        super(actual, DeliveredEmailAssert.class);
    }

    /**
     * Entry point: creates assertions for the given delivered email.
     *
     * @param email the delivered email snapshot to assert on
     */
    public static DeliveredEmailAssert assertThat(DeliveredEmail email) {
        return new DeliveredEmailAssert(email);
    }

    /**
     * Verifies the sender's personal name equals the expected value.
     *
     * @param expected the expected from-name
     */
    public DeliveredEmailAssert hasFromName(String expected) {
        isNotNull();
        Assertions.assertThat(actual.fromName()).as("from-name").isEqualTo(expected);
        return this;
    }

    /**
     * Verifies the sender's email address equals the expected value.
     *
     * @param expected the expected from-address
     */
    public DeliveredEmailAssert hasFromAddress(String expected) {
        isNotNull();
        Assertions.assertThat(actual.fromAddress()).as("from-address").isEqualTo(expected);
        return this;
    }

    /**
     * Verifies the message is addressed to exactly the given recipient and no other.
     *
     * @param expected the single expected recipient address
     */
    public DeliveredEmailAssert hasOnlyRecipient(String expected) {
        isNotNull();
        Assertions.assertThat(actual.recipients()).as("recipients").containsExactly(expected);
        return this;
    }

    /**
     * Verifies the subject equals the expected value.
     *
     * @param expected the expected subject
     */
    public DeliveredEmailAssert hasSubject(String expected) {
        isNotNull();
        Assertions.assertThat(actual.subject()).as("subject").isEqualTo(expected);
        return this;
    }

    /**
     * Verifies the decoded HTML body contains the given substring.
     *
     * @param expected the substring the HTML body must contain
     */
    public DeliveredEmailAssert hasHtmlBodyContaining(String expected) {
        isNotNull();
        Assertions.assertThat(actual.htmlBody()).as("html-body").contains(expected);
        return this;
    }
}
