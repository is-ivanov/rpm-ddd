package by.iivanov.rpm.shared.infrastructure.events;

import java.time.Duration;

/**
 * Configuration for the incomplete-publication resubmit scheduler. During the RED phase this is a
 * plain record; GREEN binds it via {@code @ConfigurationProperties("rpm.events.resubmit")} and
 * {@code @Validated} with a {@code @NotNull} interval.
 *
 * @param interval how long to wait between resubmit runs ({@code fixedDelay})
 */
public record EventResubmitProperties(Duration interval) {}
