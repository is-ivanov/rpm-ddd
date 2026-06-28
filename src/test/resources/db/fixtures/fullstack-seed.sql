-- Full-stack E2E journey seed: the pre-seeded ACTIVE admin (the journey's entry actor).
--
-- Applied OUT-OF-BAND by scripts/seed-fullstack.sh against the running
-- Infra-FullStack-Tests Postgres, AFTER the backend has migrated the production
-- schema. Lives in src/test/resources -> NOT packaged in the production jar.
-- Idempotent (ON CONFLICT DO NOTHING) so retries / repeated runs are safe.
-- created_by references the System user (00000000-...-0) inserted by the master changelog.
INSERT INTO iam_user (id, first_name, middle_name, last_name, email, login,
                      password_hash, status, created_by, registered_at, version,
                      updated_at, updated_by, time_zone)
VALUES ('019b76da-a800-7000-a957-f5fb8061a532', 'System', 'System', 'System',
        'admin@localhost.com', 'admin', '{noop}admin', 'ACTIVE'::iam_user_status,
        '00000000-0000-0000-0000-000000000000', '2026-01-01 00:00:00+00', 0,
        '2026-01-01 00:00:00+00', '00000000-0000-0000-0000-000000000000', 'UTC')
ON CONFLICT DO NOTHING;
