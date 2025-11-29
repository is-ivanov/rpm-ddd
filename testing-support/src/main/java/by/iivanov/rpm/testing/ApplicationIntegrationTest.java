package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Meta-annotation for full application integration tests.
 * Combines {@link SpringBootTest} with {@link DbTest}.
 */
@DbTest
@SpringBootTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ApplicationIntegrationTest {
}
