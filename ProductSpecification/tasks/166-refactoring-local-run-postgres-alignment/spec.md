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

**Decision (Step 1 locked Option 2, then revised at Step 3 to Option 1):** a dedicated
**persistent dev stack**. `docker/infra-local.yml` (renamed from the old `services.yml`,
`postgres.yml` indirection dropped) runs Postgres on host port `54036` with a persistent
named volume and stock tuning, plus Mailpit (1025/8025). The `local` profile points at
`:54036`; the IntelliJ `App-Local` run config starts it automatically via an `Infra-Local-Up`
before-launch task. Option 2 (re-point `local` at the ephemeral, test-tuned fullstack infra)
was implemented then superseded — its tmpfs/test-tuning are wrong for hands-on dev. Full
rationale, the rejected options, and the implementation touch-list are in
`decisions/local-run-postgres-alignment-decision.md`.

## Dependency

#166 → #164: #164's conditional `/run-backend` skip in `green-playwright` and the
`/run-frontend` rewrite defer to the dev-Postgres decision made here for the local
real-stack wording. Full map: `ProductSpecification/audits/dependencies.md`.

## Key Files

- `.claude/skills/run-backend/SKILL.md`
- `.claude/tech/java-spring/infrastructure.md` ("Run (local)" / "Test Database" sections)
- `.claude/tech/java-spring/templates/infrastructure/infrastructure-details.md`
- `docker/infra-local.yml` (renamed from `docker/services.yml`; `docker/postgres.yml` deleted)
- `.run/Infra-Local-Up.run.xml` (new), `.run/App-Local.run.xml` (before-launch task)
- `docker/infra-fullstack-tests.yml`, `frontend/acceptance/tests/fullstack/README.md` (reference)
- `src/main/resources/application-local.yml`

## Full-Stack Journey Verdict

no-impact — pure infrastructure/docs alignment of the local-run path; no rendered
critical-path or backend behavior change.
