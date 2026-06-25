# Task 214: Render deploy fail-fast DB timeouts

Type: bug
Issue: #214  <- bug tests are tagged with this issue number (per tech binding `tdd.md`)

## Problem

A Render deploy intermittently hangs during application startup and is killed by
Render's port-scan timeout (`update_failed`).

**Not a code bug.** The same Docker image SHA `a3a313d…` failed once and then succeeded
on a manual retry with identical bytes:

- `dep-d8uhv2flk1mc73fsfbo0` — 2026-06-25 12:31 — `update_failed`
- `dep-d8uikkurnols739m6p40` — 2026-06-25 13:17 — `live` (same SHA)

Log evidence (failed attempt): the app boots, connects to Postgres (Supabase pooler over
SSL, `sslmode=require`), runs Liquibase, initializes the JPA `EntityManagerFactory` at
12:33:05 — then ~13 minutes of total log silence. `Tomcat started on port(s) 10000` and
`Started RpmDddApplication` never print, so the HTTP connector never binds and Render's
port scan times out at 12:46:10.

The main startup thread blocked, most likely on a stalled network round-trip to the
Supabase pooler, because there are **no fail-fast timeouts** configured: the JDBC URL has
no `connectTimeout`/`socketTimeout`/`loginTimeout` and Hikari has no
`initialization-fail-timeout`. A stalled socket read hangs the main thread indefinitely
instead of failing fast and letting Render auto-retry.

## Solution

1. Add PostgreSQL JDBC `connectTimeout` / `socketTimeout` / `loginTimeout`.
2. Add Hikari `connection-timeout` / `validation-timeout` / `initialization-fail-timeout`
   so a slow/stalled DB handshake fails fast (Render then auto-retries instead of hanging
   ~13 min).
3. Wire the Render `healthCheckPath` to `/actuator/health`. **Depends on the actuator
   endpoint added in Task #215** — do this step only after #215 lands actuator.

## Key Files

- `src/main/resources/application.yml` — Hikari timeouts (non-secret, testable here)
- `src/main/resources/application-prod.yml` — JDBC URL params / prod overrides
- Render service config — `healthCheckPath` (after #215)

## Reproduction

Intermittent / environmental — does **not** reproduce deterministically. Proof it is
environmental: the same image SHA `a3a313d…` both failed and succeeded.

## Full-Stack Journey Verdict

**no-impact** — config-only change, no rendered critical path is touched.

## Test Approach

Lightweight wiring test only: assert the configured timeout values reach the
`HikariDataSource` bean (e.g. `connectionTimeout`, `validationTimeout`,
`initializationFailTimeout`) and that the JDBC connection properties carry the socket/login
timeouts. **No** heavy Toxiproxy/blackhole socket-stall simulation (agreed out of scope).
