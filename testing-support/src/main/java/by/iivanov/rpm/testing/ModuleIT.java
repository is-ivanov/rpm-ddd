package by.iivanov.rpm.testing;

import org.springframework.modulith.test.ApplicationModuleTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for module-scoped integration tests.
 * Combines {@link ApplicationModuleTest} with {@link DbTest}.
 */
@DbTest
@ApplicationModuleTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ModuleIT {
}
