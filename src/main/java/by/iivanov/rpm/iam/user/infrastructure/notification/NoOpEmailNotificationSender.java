package by.iivanov.rpm.iam.user.infrastructure.notification;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporary no-op implementation of EmailNotificationSender.
 * Should be replaced with a real email service.
 */
@InfrastructureComponent
class NoOpEmailNotificationSender implements EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailNotificationSender.class);

    @Override
    public void sendActivationToken(String toEmail, String login, String activationToken) {
        log.warn(
                "ACTIVATION TOKEN for {} (login: {}): {} — replace with real email service",
                toEmail,
                login,
                activationToken);
    }
}
