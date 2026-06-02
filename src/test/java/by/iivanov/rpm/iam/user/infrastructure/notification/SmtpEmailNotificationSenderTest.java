package by.iivanov.rpm.iam.user.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class SmtpEmailNotificationSenderTest {

    private static final String MALFORMED_EMAIL = "not a valid @@ address";
    private static final String LOGIN = "act_login";
    private static final String ACTIVATION_TOKEN = "activation.token";
    private static final String FROM_NAME = "RPM Platform";
    private static final String FROM_ADDRESS = "no-reply@rpm-platform.com";
    private static final String FRONTEND_BASE_URL = "http://localhost:5173";

    private final SmtpEmailNotificationSender sut = new SmtpEmailNotificationSender(
            new JavaMailSenderImpl(),
            new ActivationEmailRenderer(),
            new EmailProperties(FROM_NAME, FROM_ADDRESS),
            FRONTEND_BASE_URL);

    @Test
    @DisplayName("WHEN the recipient address is malformed EXPECT IllegalStateException wrapping the MessagingException")
    void when_recipientAddressMalformed_expect_messagingExceptionWrappedAsIllegalState() {
        // GIVEN: a sender configured with a real JavaMailSender and renderer
        // WHEN: an activation email is built for a malformed recipient address
        // THEN: the MessagingException raised by strict address parsing is rewrapped as
        // IllegalStateException carrying the build-failure message and the original cause
        assertThatThrownBy(() -> sut.sendActivationToken(MALFORMED_EMAIL, LOGIN, ACTIVATION_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to build activation email for " + MALFORMED_EMAIL)
                .hasCauseInstanceOf(MessagingException.class);
    }
}
