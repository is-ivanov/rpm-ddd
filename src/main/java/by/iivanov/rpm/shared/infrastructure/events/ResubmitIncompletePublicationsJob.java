package by.iivanov.rpm.shared.infrastructure.events;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.springframework.modulith.events.IncompleteEventPublications;

/**
 * Periodically resubmits incomplete event publications so a transient listener failure is retried
 * until the listener completes. Operates across the whole application's event publication registry,
 * not a single module — hence it lives in the shared infrastructure ring.
 */
@InfrastructureComponent
public class ResubmitIncompletePublicationsJob {

    private final IncompleteEventPublications incompletePublications;

    ResubmitIncompletePublicationsJob(IncompleteEventPublications incompletePublications) {
        this.incompletePublications = incompletePublications;
    }

    /** Resubmits incomplete event publications that are still within the resubmit age window. */
    public void resubmit() {
        incompletePublications.resubmitIncompletePublications(publication -> true);
    }
}
