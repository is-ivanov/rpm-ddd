package by.iivanov.rpm.testing.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Marks a test API class for discovery by {@code @ComponentScan} filter in {@link by.iivanov.rpm.testing.WebTest}.
 *
 * <p>Place on concrete API classes (subclasses of {@link AbstractApi}) that encapsulate
 * HTTP calls to a specific controller.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface WebApi {}
