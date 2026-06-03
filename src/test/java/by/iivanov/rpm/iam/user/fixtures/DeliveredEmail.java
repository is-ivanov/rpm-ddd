package by.iivanov.rpm.iam.user.fixtures;

import java.util.List;

/**
 * Immutable snapshot of a delivered email, parsed once from a GreenMail {@code MimeMessage} so Statements
 * assert on plain decoded values without re-touching the JavaMail API or its checked exceptions.
 *
 * @param fromName the personal name of the sender
 * @param fromAddress the sender's email address
 * @param recipients the recipient email addresses
 * @param subject the email subject
 * @param htmlBody the decoded {@code text/html} body
 */
public record DeliveredEmail(
        String fromName, String fromAddress, List<String> recipients, String subject, String htmlBody) {}
