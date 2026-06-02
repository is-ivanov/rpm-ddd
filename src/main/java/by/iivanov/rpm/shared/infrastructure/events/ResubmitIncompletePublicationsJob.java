package by.iivanov.rpm.shared.infrastructure.events;

import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Periodically resubmits incomplete event publications so a transient listener failure is retried
 * until the listener completes. Operates across the whole application's event publication registry,
 * not a single module — hence it lives in the shared infrastructure ring.
 */
@InfrastructureComponent
public class ResubmitIncompletePublicationsJob {

    private static final Duration RESUBMIT_AGE_CUTOFF = Duration.ofHours(24);

    private final IncompleteEventPublications incompletePublications;
    private final Clock clock;

    ResubmitIncompletePublicationsJob(IncompleteEventPublications incompletePublications, Clock clock) {
        this.incompletePublications = incompletePublications;
        this.clock = clock;
    }

    /** Resubmits incomplete event publications that are still within the resubmit age window. */
    @Scheduled(fixedDelayString = "${rpm.events.resubmit.interval}")
    @SchedulerLock(name = "resubmitIncompletePublications", lockAtMostFor = "PT30S", lockAtLeastFor = "PT0S")
    public void resubmit() {
        Instant cutoff = clock.instant().minus(RESUBMIT_AGE_CUTOFF);
        incompletePublications.resubmitIncompletePublications(
                publication -> publication.getPublicationDate().isAfter(cutoff));
    }
}
