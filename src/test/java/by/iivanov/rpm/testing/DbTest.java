package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.test.context.ActiveProfiles;

/**
 * Meta-annotation to mark tests that require database container.
 * - Adds JUnit tag {@code db} for the TestExecutionListener plan detection;
 * - Activates {@code test} Spring profile by default;
 * - Acquires the {@code "DB"} resource lock (READ_WRITE) so all DB-backed tests serialize
 *   against each other — and, since a class-level exclusive lock forces the whole subtree to
 *   one thread, methods within a DB-lane class never run concurrently against the shared
 *   Testcontainers database. Replaces the blanket {@code @Execution(SAME_THREAD)}.
 */
@Tag(Constants.DB_TEST_TAG)
@ResourceLock(Constants.DB_LOCK)
@ActiveProfiles("test")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DbTest {}
