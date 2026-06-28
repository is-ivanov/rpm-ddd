# Task 166: /run-backend vs dev compose — Postgres host-port / local-run alignment -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: decide the approach (options 1-3 in spec) — /architecture discussion with the user
- [x] design  → Option 2 (local → :54035 fullstack infra; delete services.yml/postgres.yml). ADR: decisions/local-run-postgres-alignment-decision.md

### Step 2: implement the chosen option (compose/profile/skill alignment)
- [~] refactor (infra + skill docs)

### Step 3: verify: documented local-run path works end-to-end (backend healthy)
- [ ] refactor (verification)
