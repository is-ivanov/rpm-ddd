# Task 225: Pin production timestamp handling to UTC

Type: refactoring
Issue: #225

## Problem

Story 4 surfaced that TZ-naive `timestamptz` values are interpreted in the JVM/session time zone.
The fix was applied for tests (`SET TIME ZONE 'UTC'` in `db.changelog-test.xml`), but production has
no timezone pin (`hibernate.jdbc.time_zone` is unset), so production correctness relies on the host
running UTC.

## Solution

Set `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` so Hibernate normalizes all timestamp
binding/reading to UTC regardless of JVM/DB zone. Optionally also pin `-Duser.timezone=UTC` on the
app JVM and `TZ=UTC` on the container for defense in depth. A `SET TIME ZONE` in a migration does NOT
fix runtime (each app connection resets its session zone), so this must be a datasource/Hibernate
setting. Note: the Hibernate setting does not cover Liquibase's own connection, so the test seed-load
pin likely stays.

Covers Story 4 review Q8. Backlog ref: Story 4 improvements.md I4.

## Key Files

- `src/main/resources/application.yml` (JPA/datasource config)
