package by.iivanov.rpm.shared.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;

/**
 * Application-wide scheduling configuration. Will own {@code @EnableScheduling},
 * {@code @EnableSchedulerLock}, and the {@code LockProvider} bean, gated by
 * {@code rpm.scheduler.enabled}. Empty stub during the RED phase — the wiring is added in GREEN.
 */
@Configuration
public class SchedulingConfiguration {}
