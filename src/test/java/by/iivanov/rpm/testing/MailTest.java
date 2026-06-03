package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;

/**
 * Meta-annotation to mark tests that require an SMTP server for activation-email delivery.
 * - Adds JUnit tag {@code mail} for the {@link GreenMailServerTestExecutionListener} plan detection;
 * - The listener starts the in-JVM {@link GreenMailServer} and exports its loopback SMTP coordinates
 *   as {@code spring.mail.*} system properties before the Spring context boots.
 */
@Tag(Constants.MAIL_TEST_TAG)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MailTest {}
