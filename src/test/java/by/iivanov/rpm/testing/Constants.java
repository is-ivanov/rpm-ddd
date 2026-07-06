package by.iivanov.rpm.testing;

// Dedicated constants holder: distinct named constants sharing a value (DB_USER/DB_PASSWORD = "postgres")
// is intentional, not a magic-literal smell.
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class Constants {

    static final String DB_TEST_TAG = "db";
    static final String MAIL_TEST_TAG = "mail";
    static final String DB_LOCK = "DB";
    static final String WEB_SLICE_LOCK = "WEB_SLICE_MOCKS";
    static final String TARGET_DB_NAME = "rpm_ddd";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "postgres";

    private Constants() {}
}
