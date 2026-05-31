package by.iivanov.rpm.testing;

/**
 * Base for full-context integration tests that exercise activation-email delivery. Adds only the
 * shared Mailpit SMTP server via the {@link MailTest} tag.
 *
 * <p>The shared {@link org.springframework.mail.javamail.JavaMailSender} spy used to emulate SMTP
 * failures is declared on {@link AbstractApplicationIntegrationTest} through {@link SharedSpies}, not
 * here — the {@code mail} tag does not affect the Spring context cache key, so mail and non-mail
 * integration tests stay on a single cached context.
 */
@MailTest
public abstract class AbstractMailIntegrationTest extends AbstractApplicationIntegrationTest {}
