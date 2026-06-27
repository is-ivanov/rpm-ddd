# Task 224: Serialize DB tests with @ResourceLock instead of SAME_THREAD -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: DB lane -- swap SAME_THREAD for @ResourceLock("DB")
- [x] refactor (add @ResourceLock("DB") to @ApplicationIntegrationTest + @DbTest; drop @Execution(SAME_THREAD); share the lock key)

### Step 2: Web-slice lane -- @ResourceLock("WEB_SLICE_MOCKS")
- [~] refactor (annotate @WebTest with @ResourceLock("WEB_SLICE_MOCKS"); drop @Execution(SAME_THREAD) from AuthResourceTest + UserResourceTest; ensure no two web-slice methods run concurrently across classes -- they share the cached context's auto-mock beans)

### Step 3: Confirm no parallel flakiness (both lanes)
- [ ] green-acceptance (full suite green across repeated runs; verify DB and web-slice lanes parallelize via timing, no mock/DB races, no exposed ordering assumptions)
