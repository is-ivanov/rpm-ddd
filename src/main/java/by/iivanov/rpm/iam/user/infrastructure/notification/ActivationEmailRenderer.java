package by.iivanov.rpm.iam.user.infrastructure.notification;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;

/**
 * Renders the activation email's subject, HTML body, and plain-text body from the recipient's
 * login and the activation link.
 */
@InfrastructureComponent
public class ActivationEmailRenderer {

    /**
     * Renders the activation email content for the given login and activation link.
     *
     * @param login the recipient's login, shown in the email greeting
     * @param activationLink the absolute URL to the frontend activation page carrying the token
     * @return the rendered subject, HTML body, and plain-text body
     */
    public ActivationEmailContent render(String login, String activationLink) {
        throw new UnsupportedOperationException();
    }
}
