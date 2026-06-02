package by.iivanov.rpm.iam.user.infrastructure.notification;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.TestResources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivationEmailRendererTest {

    private static final String FIXED_LOGIN = "act_fixed_login";
    private static final String FIXED_ACTIVATION_LINK = "http://localhost:5173/activate?token=fixed.activation.token";
    private static final String EXPECTED_SUBJECT = "Activate your RPM account";
    private static final String MALICIOUS_LOGIN = "<script>alert('x')</script>[[${7*7}]]";

    private static final String APPROVED_HTML_FIXTURE = "email/activation-email.approved.html";
    private static final String APPROVED_TEXT_FIXTURE = "email/activation-email.approved.txt";
    private static final String APPROVED_ESCAPED_HTML_FIXTURE = "email/activation-email-escaped-login.approved.html";

    private final ActivationEmailRenderer sut = new ActivationEmailRenderer();

    @Test
    @DisplayName("WHEN rendering with fixed login and link EXPECT subject, HTML, and text match approved fixtures")
    void when_fixedInputs_expect_renderedContentMatchesApprovedFixtures() {
        // GIVEN: deterministic login and activation link
        // WHEN: the activation email is rendered
        ActivationEmailContent content = sut.render(FIXED_LOGIN, FIXED_ACTIVATION_LINK);

        // THEN: subject, HTML body, and text body match the checked-in approved fixtures
        then(content.subject()).isEqualTo(EXPECTED_SUBJECT);
        then(content.htmlBody()).isEqualToNormalizingNewlines(TestResources.readUtf8(APPROVED_HTML_FIXTURE));
        then(content.textBody()).isEqualToNormalizingNewlines(TestResources.readUtf8(APPROVED_TEXT_FIXTURE));
    }

    @Test
    @DisplayName("WHEN login contains HTML/template markup EXPECT it rendered as inert escaped text")
    void when_loginContainsMarkup_expect_escapedInertTextInHtmlBody() {
        // GIVEN: a login carrying an HTML tag payload and a template-expression payload
        // WHEN: the activation email is rendered with that login
        ActivationEmailContent content = sut.render(MALICIOUS_LOGIN, FIXED_ACTIVATION_LINK);

        // THEN: the whole body matches the approved fixture in which the HTML tag payload is
        // entity-escaped (&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;) and the template-expression
        // payload survives verbatim as inert literal text ([[${7*7}]]) — proving neither payload was
        // interpreted. Whole-body equality pins escaping AND non-evaluation in one strict assertion.
        then(content.htmlBody()).isEqualToNormalizingNewlines(TestResources.readUtf8(APPROVED_ESCAPED_HTML_FIXTURE));

        // AND: no raw live <script> tag is present anywhere in the body
        then(content.htmlBody()).doesNotContain("<script>alert('x')</script>");

        // AND: the SpEL payload appears only as inert literal text, never as an evaluated result.
        // We assert the literal fragment is present rather than doesNotContain("49"): this renderer
        // has no expression engine, so "49" could never appear regardless — that check would pass on
        // broken output and prove nothing. Asserting the verbatim, unevaluated fragment is the real proof.
        then(content.htmlBody()).contains("[[${7*7}]]");
    }
}
