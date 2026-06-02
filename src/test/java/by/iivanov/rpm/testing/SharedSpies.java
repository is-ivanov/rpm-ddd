package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Bundles the Mockito spy beans shared by every full-context integration test. Declared on
 * {@link AbstractApplicationIntegrationTest} so the whole integration suite — mail and non-mail
 * alike — reuses a single cached Spring context instead of forking one per spy configuration.
 *
 * <p>Each spy delegates to the real bean by default, so behaviour is unchanged until a test stubs it.
 * Declaring the spies through this annotation (rather than as fields on the base class) keeps the base
 * uncluttered and gives one place to grow the shared-spy set.
 *
 * <p>{@link JavaMailSender} is always present because {@code spring.mail.host} is set in
 * {@code application-test.yml}; {@code MailpitContainerTestExecutionListener} only retargets its host
 * and port at runtime for {@link MailTest}-tagged tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MockitoSpyBean(types = {JavaMailSender.class})
public @interface SharedSpies {}
