package by.iivanov.rpm.testing;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Clears the Spring Modulith event publication registry before each full-context test. Incomplete
 * publications deliberately left by one test (e.g. the stale / in-flight resubmit scenarios, which
 * assert a publication stays incomplete) otherwise persist in the shared {@code event_publication}
 * table — which the iam_user baseline cleanup does not touch — and leak into any later test that
 * asserts registry state. Starting every full-context test from an empty registry removes that
 * cross-test ordering coupling.
 */
public class EventPublicationCleanupExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        JdbcClient jdbcClient = SpringExtension.getApplicationContext(context).getBean(JdbcClient.class);
        jdbcClient.sql("DELETE FROM event_publication").update();
    }
}
