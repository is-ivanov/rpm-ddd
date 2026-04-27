package by.iivanov.rpm.testing.session;

/**
 * Immutable snapshot of an authenticated test session.
 *
 * <p>Keep this object small. It should only carry the data needed to replay authenticated HTTP
 * requests in e2e tests.
 */
public record SessionContext(String sessionId, String csrfToken) {}
