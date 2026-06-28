# Task 166: /run-backend vs dev compose — Postgres host-port / local-run alignment

Type: refactoring
Issue: #166  <- task number IS the issue number; refactoring records it for traceability (tests not tagged)

## Problem

The `/run-backend` skill and the dev Docker compose contradict each other:

- `.claude/skills/run-backend/SKILL.md` says the `local` profile needs dev Postgres at
  `localhost:5432`; start it with `docker compose -f docker/services.yml up -d`, then
  `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`.
- The Postgres service in the dev compose **does not publish port 5432 to the host**
  (intentionally, per earlier decisions). Containers start fine, but the backend's
  HikariCP/Liquibase connection to `localhost:5432` gets `Connection refused`, and
  `spring-boot:run` ends in BUILD FAILURE after ~40s.

Net effect: the documented local-run path is guaranteed dead on a dev machine
(reproduced verbatim in Task 8 / #162). The only working real-stack path today is the
fullstack-E2E recipe (`docker/infra-fullstack-tests.yml` → Postgres on host `:54035`,
backend on the `fullstack` profile + seed script).

## Solution

Decide and implement one coherent local-real-stack story so `/run-backend`, the
java-spring `infrastructure.md` tech binding, and the compose files all agree.

Options to evaluate at the `design` step:

1. Publish a host port for Postgres in the dev compose (e.g. `5432:5432` or a non-default
   port + matching `local` profile config) — restores the documented path; check for
   collisions with parallel sessions / other local Postgres instances.
2. Re-point the `local` profile (or the skill) at the fullstack infra (`:54035`) so one
   shared infra serves both local runs and fullstack E2E. **(user's preliminary lean)**
3. Drop the `local` `spring-boot:run` path from the skill entirely and document the
   fullstack recipe as the single way to run the real stack locally.

The chosen option is locked at the `design` step (Step 1) before implementation.

## Dependency

#166 → #164: #164's conditional `/run-backend` skip in `green-playwright` and the
`/run-frontend` rewrite defer to the dev-Postgres decision made here for the local
real-stack wording. Full map: `ProductSpecification/audits/dependencies.md`.

## Key Files

- `.claude/skills/run-backend/SKILL.md`
- `.claude/tech/java-spring/infrastructure.md` ("Run (local)" / "Test Database" sections)
- `docker/services.yml`, `docker/postgres.yml`
- `docker/infra-fullstack-tests.yml`, `frontend/acceptance/tests/fullstack/README.md` (reference)
- `src/main/resources/application-local.yml`

## Full-Stack Journey Verdict

no-impact — pure infrastructure/docs alignment of the local-run path; no rendered
critical-path or backend behavior change.
