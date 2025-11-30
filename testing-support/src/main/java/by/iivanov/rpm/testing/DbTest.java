package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

/**
 * Meta-annotation to mark tests that require database container.
 * - Adds JUnit tag {@code db} for the TestExecutionListener plan detection;
 * - Activates {@code test} Spring profile by default.
 */
@Tag(Constants.DB_TEST_TAG)
@ActiveProfiles("test")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DbTest {}
