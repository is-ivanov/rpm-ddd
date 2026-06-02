package by.iivanov.rpm.shared.infrastructure.events;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the incomplete-publication resubmit scheduler, bound from
 * {@code rpm.events.resubmit}. The interval is required — a missing or blank value fails context
 * startup loudly so the scheduler can never silently stop running.
 *
 * @param interval how long to wait between resubmit runs ({@code fixedDelay})
 */
@ConfigurationProperties("rpm.events.resubmit")
@Validated
public record EventResubmitProperties(@NotNull Duration interval) {}
