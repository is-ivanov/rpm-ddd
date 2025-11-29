package by.iivanov.rpm.testing;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.*;

/**
 * Meta-annotation for full application integration tests.
 * Combines {@link SpringBootTest} with {@link DbTest}.
 */
@DbTest
@SpringBootTest
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ApplicationIT {
}
