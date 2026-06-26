# Task 214: Render deploy fail-fast DB timeouts -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: add fail-fast DB timeouts (JDBC + Hikari)
- [x] red (wiring test: timeouts reach HikariDataSource bean + JDBC props; tag #214)
- [x] green (apply Hikari + JDBC timeout config)
- [x] refactor (no changes needed — config-only, test already clean)
- [S] adapters-discovery (config-only, no new ports/adapters)

### Infra: Render health check (expose + permit public /actuator/health)
<!-- #215 added actuator but exposed ONLY /info (health closed) and secured /actuator/**
     (authenticated /info, denyAll rest). So the Render probe path needs a security scenario:
     expose health + add a narrow PUBLIC allow-list entry for GET /actuator/health, keeping
     all other /actuator/** closed. User approved full TDD implementation. -->
- [x] red (security: anon GET /actuator/health -> 200 {status:UP}; other /actuator/** stay 401; tag #214)
- [~] green (expose health in application.yml + permit GET /actuator/health in SecurityConfig + healthCheckPath in infra/render.yaml)
- [ ] refactor

## Full-Stack Journey
- [S] fullstack-journey (no-impact: config-only, no rendered critical path change)
