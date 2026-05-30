package by.iivanov.rpm.iam.user.infrastructure.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sender identity for outgoing activation emails, bound from {@code app.mail.*}.
 *
 * @param fromName the display name shown as the email sender (e.g. {@code RPM Platform})
 * @param fromAddress the from-address activation emails are sent from (e.g. {@code no-reply@rpm-platform.com})
 */
@ConfigurationProperties("app.mail")
record EmailProperties(String fromName, String fromAddress) {}
