package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;

/**
 * Meta-annotation to mark tests that require a Mailpit SMTP server.
 * - Adds JUnit tag {@code mail} for the {@link MailpitContainerTestExecutionListener} plan detection;
 * - The listener probes the shared Mailpit (started by {@code Infra-Tests-Up}) and reuses it,
 *   or starts a reusable Testcontainer when the shared instance is unreachable.
 */
@Tag(Constants.MAIL_TEST_TAG)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MailTest {}
