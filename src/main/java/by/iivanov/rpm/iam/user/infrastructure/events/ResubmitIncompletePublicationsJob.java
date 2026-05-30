package by.iivanov.rpm.iam.user.infrastructure.events;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;

/**
 * Periodically resubmits incomplete activation-email event publications so a transient send failure
 * is retried until the listener completes.
 */
@InfrastructureComponent
public class ResubmitIncompletePublicationsJob {

    /** Resubmits incomplete activation-email publications that are still within the resubmit age window. */
    public void resubmit() {
        throw new UnsupportedOperationException();
    }
}
