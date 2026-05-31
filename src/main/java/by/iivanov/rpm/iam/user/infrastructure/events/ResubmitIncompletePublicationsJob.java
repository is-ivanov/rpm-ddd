package by.iivanov.rpm.iam.user.infrastructure.events;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.springframework.modulith.events.IncompleteEventPublications;

/**
 * Periodically resubmits incomplete activation-email event publications so a transient send failure
 * is retried until the listener completes.
 */
@InfrastructureComponent
public class ResubmitIncompletePublicationsJob {

    private final IncompleteEventPublications incompletePublications;

    ResubmitIncompletePublicationsJob(IncompleteEventPublications incompletePublications) {
        this.incompletePublications = incompletePublications;
    }

    /** Resubmits incomplete activation-email publications that are still within the resubmit age window. */
    public void resubmit() {
        incompletePublications.resubmitIncompletePublications(publication -> true);
    }
}
