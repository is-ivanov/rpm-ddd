package by.iivanov.rpm.iam.user.infrastructure.notification;

/**
 * Port for sending email notifications.
 * Implementations should be provided in the infrastructure layer.
 */
public interface EmailNotificationSender {

    /**
     * Sends a temporary password notification to the user.
     */
    void sendTemporaryPassword(String toEmail, String login, String temporaryPassword);
}
