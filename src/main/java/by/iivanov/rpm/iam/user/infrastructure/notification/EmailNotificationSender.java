package by.iivanov.rpm.iam.user.infrastructure.notification;

/**
 * Port for sending email notifications.
 * Implementations should be provided in the infrastructure layer.
 */
public interface EmailNotificationSender {

    void sendActivationToken(String toEmail, String login, String activationToken);
}
