package by.iivanov.rpm.iam.user.infrastructure.notification;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.TestResources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivationEmailRendererTest {

    private static final String FIXED_LOGIN = "act_fixed_login";
    private static final String FIXED_ACTIVATION_LINK = "http://localhost:5173/activate?token=fixed.activation.token";
    private static final String EXPECTED_SUBJECT = "Activate your RPM account";

    private final ActivationEmailRenderer sut = new ActivationEmailRenderer();

    @Test
    @DisplayName("WHEN rendering with fixed login and link EXPECT subject, HTML, and text match approved fixtures")
    void when_fixedInputs_expect_renderedContentMatchesApprovedFixtures() {
        // GIVEN: deterministic login and activation link
        // WHEN: the activation email is rendered
        ActivationEmailContent content = sut.render(FIXED_LOGIN, FIXED_ACTIVATION_LINK);

        // THEN: subject, HTML body, and text body match the checked-in approved fixtures
        then(content.subject()).isEqualTo(EXPECTED_SUBJECT);
        then(content.htmlBody())
                .isEqualToNormalizingNewlines(TestResources.readUtf8("email/activation-email.approved.html"));
        then(content.textBody())
                .isEqualToNormalizingNewlines(TestResources.readUtf8("email/activation-email.approved.txt"));
    }
}
