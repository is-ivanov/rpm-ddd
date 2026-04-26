package by.iivanov.rpm.iam.user.infrastructure.events;

import by.iivanov.rpm.iam.user.domain.UserRegisteredEvent;
import by.iivanov.rpm.iam.user.infrastructure.notification.EmailNotificationSender;
import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;

/**
 * Listens for UserRegisteredEvent and delegates to EmailNotificationSender.
 */
@InfrastructureComponent
class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final EmailNotificationSender emailNotificationSender;

    UserRegisteredEventListener(EmailNotificationSender emailNotificationSender) {
        this.emailNotificationSender = emailNotificationSender;
    }

    @ApplicationModuleListener
    void on(UserRegisteredEvent event) {
        log.info(
                "User registered: login={}, email={}",
                event.login().login(),
                event.email().email());
        emailNotificationSender.sendTemporaryPassword(
                event.email().email(), event.login().login(), event.temporaryPasswordPlain());
    }
}
