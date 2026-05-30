package by.iivanov.rpm.iam.user.infrastructure.notification;

/**
 * Rendered activation-email content: the subject line plus the HTML and plain-text bodies.
 *
 * @param subject the email subject line
 * @param htmlBody the rendered HTML body part
 * @param textBody the rendered plain-text body part
 */
public record ActivationEmailContent(String subject, String htmlBody, String textBody) {}
