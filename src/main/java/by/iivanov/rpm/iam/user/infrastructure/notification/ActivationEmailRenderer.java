package by.iivanov.rpm.iam.user.infrastructure.notification;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Renders the activation email's subject, HTML body, and plain-text body from the recipient's
 * login and the activation link.
 */
@InfrastructureComponent
public class ActivationEmailRenderer {

    private static final String SUBJECT = "Activate your RPM account";
    private static final String HTML_TEMPLATE = "templates/email/activation.html";
    private static final String TEXT_TEMPLATE = "templates/email/activation.txt";

    /**
     * Renders the activation email content for the given login and activation link.
     *
     * @param login the recipient's login, shown in the email greeting
     * @param activationLink the absolute URL to the frontend activation page carrying the token
     * @return the rendered subject, HTML body, and plain-text body
     */
    public ActivationEmailContent render(String login, String activationLink) {
        return new ActivationEmailContent(
                SUBJECT,
                loadAndFill(HTML_TEMPLATE, login, activationLink),
                loadAndFill(TEXT_TEMPLATE, login, activationLink));
    }

    private static String loadAndFill(String classpathResource, String login, String activationLink) {
        return loadTemplate(classpathResource).replace("{login}", login).replace("{activationLink}", activationLink);
    }

    private static String loadTemplate(String classpathResource) {
        try (InputStream stream =
                ActivationEmailRenderer.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (stream == null) {
                throw new IllegalStateException("Email template not found on classpath: " + classpathResource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
