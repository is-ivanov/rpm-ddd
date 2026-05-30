package by.iivanov.rpm.iam.user.infrastructure.notification;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Sends activation emails over SMTP, rendering the multipart (HTML + plain-text) body via
 * {@link ActivationEmailRenderer} before delivering through the configured {@link JavaMailSender}.
 */
@Primary
@InfrastructureComponent
// JavaMailSender is contributed by Spring Boot's MailSenderAutoConfiguration when spring.mail.host
// is set; IntelliJ cannot see that conditional bean statically (verified resolvable at runtime by
// the full-context integration tests).
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SmtpEmailNotificationSender implements EmailNotificationSender {

    private final JavaMailSender mailSender;
    private final ActivationEmailRenderer renderer;
    private final String fromName;
    private final String fromAddress;
    private final String frontendBaseUrl;

    SmtpEmailNotificationSender(
            JavaMailSender mailSender,
            ActivationEmailRenderer renderer,
            @Value("${app.mail.from-name}") String fromName,
            @Value("${app.mail.from-address}") String fromAddress,
            @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.renderer = renderer;
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void sendActivationToken(String toEmail, String login, String activationToken) {
        ActivationEmailContent content = renderer.render(login, activationLink(activationToken));
        mailSender.send(buildMessage(toEmail, content));
    }

    private String activationLink(String activationToken) {
        return frontendBaseUrl + "/activate?token=" + activationToken;
    }

    private MimeMessage buildMessage(String toEmail, ActivationEmailContent content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject(content.subject());
            helper.setText(content.textBody(), content.htmlBody());
            return message;
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to build activation email for " + toEmail, e);
        }
    }
}
