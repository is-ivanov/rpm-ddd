package by.iivanov.rpm.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Reads UTF-8 text resources from the test classpath ({@code src/test/resources}).
 *
 * <p>Centralises the read-classpath-fixture-into-a-String idiom shared by approval tests,
 * response-body fixtures, and database init scripts.
 */
public final class TestResources {

    private TestResources() {}

    /**
     * Reads the given classpath resource as a UTF-8 string.
     *
     * @param classpathResource path relative to the test classpath root (e.g. {@code "email/activation.html"})
     * @return the resource content decoded as UTF-8
     * @throws IllegalStateException if no such resource exists on the classpath
     */
    public static String readUtf8(String classpathResource) {
        try (InputStream stream = TestResources.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (stream == null) {
                throw new IllegalStateException("Classpath resource not found: " + classpathResource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
