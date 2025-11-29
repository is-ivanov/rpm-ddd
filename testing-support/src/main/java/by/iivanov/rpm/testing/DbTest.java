package by.iivanov.rpm.testing;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation to mark tests that require database container.
 * - Adds JUnit tag {@code db} for the TestExecutionListener plan detection;
 * - Imports {@link TestcontainersConfig} to expose connection details via @ServiceConnection;
 * - Activates {@code test} Spring profile by default.
 */
@Tag("db")
@ActiveProfiles("test")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DbTest {
}
