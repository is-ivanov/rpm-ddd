package by.iivanov.rpm.iam.user.infrastructure.security;

import by.iivanov.rpm.iam.auth.infrastructure.RpmUserDetails;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@InfrastructureComponent
public class SecurityCurrentActorProvider {

    /**
     * Extracts the current authenticated user's ID from the SecurityContext.
     *
     * @return the UserId of the current user
     * @throws IllegalStateException if no authentication is present or principal is not a RpmUserDetails
     */
    public UserId currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("No authentication in SecurityContext");
        }

        Object principal = Objects.requireNonNull(auth.getPrincipal(), "No principal in authentication");
        if (principal instanceof RpmUserDetails details) {
            return details.userId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal.getClass());
    }
}
