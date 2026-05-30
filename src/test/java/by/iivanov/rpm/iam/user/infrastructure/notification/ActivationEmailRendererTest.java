package by.iivanov.rpm.iam.user.infrastructure.notification;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivationEmailRendererTest {

    private static final String FIXED_LOGIN = "act_fixed_login";
    private static final String FIXED_ACTIVATION_LINK = "http://localhost:5173/activate?token=fixed.activation.token";
    private static final String EXPECTED_SUBJECT = "Activate your RPM account";

    private final ActivationEmailRenderer sut = new ActivationEmailRenderer();

    @Test
    @Disabled("TDD Red Phase - Not yet implemented")
    @DisplayName("WHEN rendering with fixed login and link EXPECT subject, HTML, and text match approved fixtures")
    void when_fixedInputs_expect_renderedContentMatchesApprovedFixtures() {
        // GIVEN: deterministic login and activation link
        // WHEN: the activation email is rendered
        ActivationEmailContent content = sut.render(FIXED_LOGIN, FIXED_ACTIVATION_LINK);

        // THEN: subject, HTML body, and text body match the checked-in approved fixtures
        then(content.subject()).isEqualTo(EXPECTED_SUBJECT);
        then(content.htmlBody()).isEqualToNormalizingNewlines(readResource("email/activation-email.approved.html"));
        then(content.textBody()).isEqualToNormalizingNewlines(readResource("email/activation-email.approved.txt"));
    }

    private static String readResource(String classpathResource) {
        try (InputStream stream =
                ActivationEmailRendererTest.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (stream == null) {
                throw new IllegalStateException("Classpath resource not found: " + classpathResource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
