package by.iivanov.rpm.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Meta-annotation for full application integration tests.
 * Combines {@link SpringBootTest} with {@link DbTest}.
 * Inherits the {@code "DB"} {@code @ResourceLock} from {@link DbTest} (discovered transitively
 * through the meta-annotation) so all e2e tests serialize against the shared Testcontainers
 * database — without the blanket {@code @Execution(SAME_THREAD)} that also serialized them
 * against the unrelated web-slice lane.
 * Registers {@link IamUserBaselineCleanupExtension} to reset the {@code iam_user} baseline per test
 * and {@link EventPublicationCleanupExtension} to clear the shared event publication registry, so
 * incomplete publications left by one test do not leak into another (order-independence).
 */
@DbTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ExtendWith({IamUserBaselineCleanupExtension.class, EventPublicationCleanupExtension.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ApplicationIntegrationTest {}
