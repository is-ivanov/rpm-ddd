# Task 166: /run-backend vs dev compose — Postgres host-port / local-run alignment -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: decide the approach (options 1-3 in spec) — /architecture discussion with the user
- [x] design  → Option 2 (local → :54035 fullstack infra; delete services.yml/postgres.yml). ADR: decisions/local-run-postgres-alignment-decision.md

### Step 2: implement the chosen option (compose/profile/skill alignment)
- [x] refactor (infra + skill docs)  → REVISED to Option 1 (persistent dev stack): docker/infra-local.yml (renamed from services.yml; Postgres :54036 + named volume + stock tuning + Mailpit); local→:54036; new .run/Infra-Local-Up + App-Local before-launch task; run-backend skill + infrastructure.md + infrastructure-details.md realigned. ADR header + Revised Decision record the supersession.

### Step 3: verify: documented local-run path works end-to-end (backend healthy)
- [x] refactor (verification)  → `docker compose -f docker/infra-local.yml up -d` (Postgres :54036 healthy, Mailpit 1025/8025) → `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`: Hikari connected to :54036, Liquibase applied migrations, started in 6.2s, GET /actuator/health = 200 {"status":"UP"}. Documented path works end-to-end.
