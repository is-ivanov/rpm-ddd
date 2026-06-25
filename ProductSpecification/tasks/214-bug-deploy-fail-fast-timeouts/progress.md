# Task 214: Render deploy fail-fast DB timeouts -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: add fail-fast DB timeouts (JDBC + Hikari)
- [ ] red (wiring test: timeouts reach HikariDataSource bean + JDBC props; tag #214)
- [ ] green (apply Hikari + JDBC timeout config)
- [ ] refactor
- [S] adapters-discovery (config-only, no new ports/adapters)

### Infra: Render health check
- [ ] healthcheck (set Render healthCheckPath=/actuator/health) -- BLOCKED on #215 actuator

## Full-Stack Journey
- [S] fullstack-journey (no-impact: config-only, no rendered critical path change)
