package by.iivanov.rpm.iam.user.infrastructure.notification;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Wiring test for the production mail bootstrap. Boots the notification beans against prod-like
 * {@code spring.mail.*} properties so Spring Boot's {@link MailSenderAutoConfiguration} contributes a
 * {@link JavaMailSender}, and asserts the context starts with exactly one {@link JavaMailSender} and
 * exactly one {@link EmailNotificationSender} wired. This is a sliced {@link ApplicationContextRunner}
 * that does not fork the shared full acceptance context.
 */
class ProductionMailBootstrapTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(
                    NotificationBeans.class, SmtpEmailNotificationSender.class, ActivationEmailRenderer.class)
            .withPropertyValues(
                    "spring.mail.host=smtp.example.com",
                    "spring.mail.port=587",
                    "spring.mail.username=mailer",
                    "spring.mail.password=secret",
                    "rpm.frontend-base-url=http://localhost:5173",
                    "rpm.mail.from-name=RPM Platform",
                    "rpm.mail.from-address=no-reply@rpm-platform.com");

    @Test
    @DisplayName("The application context starts with the production mail configuration")
    void productionMailConfig_bootstraps_withSingleSender() {
        contextRunner.run(context -> {
            then(context).hasNotFailed();
            then(context).hasSingleBean(JavaMailSender.class);
            then(context).hasSingleBean(EmailNotificationSender.class);
            then(context.getBean(EmailProperties.class))
                    .isEqualTo(new EmailProperties("RPM Platform", "no-reply@rpm-platform.com"));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(EmailProperties.class)
    static class NotificationBeans {}
}
