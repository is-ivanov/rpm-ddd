# Task 225: Pin production timestamp handling to UTC -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Set hibernate.jdbc.time_zone=UTC
- [x] refactor (add the JPA property; optionally JVM -Duser.timezone=UTC / container TZ=UTC)
- [~] green-acceptance (full suite green; app smoke shows UTC-stable timestamps)
