package by.iivanov.rpm.shared.infrastructure.events;

import static org.assertj.core.api.BDDAssertions.then;

import io.qameta.allure.Issue;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

class ResubmitIncompletePublicationsJobTest {

    private static final Instant NOW = Instant.parse("2026-01-05T10:23:56.632Z");

    private static final String IN_FLIGHT = "in-flight-30s";
    private static final String STUCK = "stuck-5min";
    private static final String STALE = "stale-25h";

    private static final Duration YOUNGER_THAN_GRACE = Duration.ofSeconds(30);
    private static final Duration BETWEEN_GRACE_AND_24H = Duration.ofMinutes(5);
    private static final Duration OLDER_THAN_24H = Duration.ofHours(25);

    private final MutableClock clock = MutableClock.of(NOW, ZoneOffset.UTC);
    private final RecordingIncompletePublications incompletePublications = new RecordingIncompletePublications();
    private final ResubmitIncompletePublicationsJob sut =
            new ResubmitIncompletePublicationsJob(incompletePublications, clock);

    @Test
    @Issue("148")
    @DisplayName("WHEN the resubmit job runs EXPECT only publications older than the grace period and younger "
            + "than 24h are selected; an in-flight publication younger than grace and a stale one older than 24h "
            + "are not")
    void when_resubmitRuns_expect_onlyPublicationsBetweenGraceAnd24hSelected() {
        // GIVEN: an in-flight publication younger than grace, a genuinely stuck one between grace and 24h,
        // and a stale one older than 24h
        incompletePublications.register(IN_FLIGHT, NOW.minus(YOUNGER_THAN_GRACE));
        incompletePublications.register(STUCK, NOW.minus(BETWEEN_GRACE_AND_24H));
        incompletePublications.register(STALE, NOW.minus(OLDER_THAN_24H));

        // WHEN: resubmit job runs
        sut.resubmit();

        // THEN: only the genuinely stuck publication (older than grace, younger than 24h) is resubmitted
        then(incompletePublications.resubmittedLabels()).containsExactly(STUCK);
    }
}
