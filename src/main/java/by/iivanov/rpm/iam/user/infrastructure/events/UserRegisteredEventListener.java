package by.iivanov.rpm.iam.user.infrastructure.events;

import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.UserRegisteredEvent;
import by.iivanov.rpm.iam.user.infrastructure.notification.EmailNotificationSender;
import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;

@InfrastructureComponent
class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final JwtActivationTokenGenerator tokenGenerator;
    private final EmailNotificationSender emailNotificationSender;

    UserRegisteredEventListener(
            JwtActivationTokenGenerator tokenGenerator, EmailNotificationSender emailNotificationSender) {
        this.tokenGenerator = tokenGenerator;
        this.emailNotificationSender = emailNotificationSender;
    }

    @ApplicationModuleListener
    void on(UserRegisteredEvent event) {
        log.info(
                "User registered: login={}, email={}",
                event.login().login(),
                event.email().email());

        var jti = JtiGenerator.generate();
        var token = tokenGenerator.generateToken(event.userId(), jti);
        emailNotificationSender.sendActivationToken(
                event.email().email(), event.login().login(), token);
    }
}
