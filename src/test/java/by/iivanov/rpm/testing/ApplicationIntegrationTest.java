package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Meta-annotation for full application integration tests.
 * Combines {@link SpringBootTest} with {@link DbTest}.
 * Inherits the {@code "DB"} {@link ResourceLock} from {@link DbTest} (declared here too for clarity)
 * so all e2e tests serialize against the shared Testcontainers database — without the blanket
 * {@code @Execution(SAME_THREAD)} that also serialized them against the unrelated web-slice lane.
 * Registers {@link IamUserBaselineCleanupExtension} to reset the {@code iam_user} baseline per test.
 */
@DbTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ExtendWith(IamUserBaselineCleanupExtension.class)
@ResourceLock(Constants.DB_LOCK)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ApplicationIntegrationTest {}
