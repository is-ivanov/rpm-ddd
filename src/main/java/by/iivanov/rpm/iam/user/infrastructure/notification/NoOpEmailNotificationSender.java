package by.iivanov.rpm.iam.user.infrastructure.notification;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;

/**
 * Temporary no-op implementation of EmailNotificationSender.
 * Should be replaced with a real email service.
 */
@Primary
@InfrastructureComponent
class NoOpEmailNotificationSender implements EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailNotificationSender.class);

    @Override
    public void sendTemporaryPassword(String toEmail, String login, String temporaryPassword) {
        log.warn(
                "TEMPORARY PASSWORD for {} (login: {}): {} — replace with real email service",
                toEmail,
                login,
                temporaryPassword);
    }
}
