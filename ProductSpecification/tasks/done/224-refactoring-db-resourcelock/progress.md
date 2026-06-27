# Task 224: Serialize DB tests with @ResourceLock instead of SAME_THREAD -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: DB lane -- swap SAME_THREAD for @ResourceLock("DB")
- [x] refactor (add @ResourceLock("DB") to @ApplicationIntegrationTest + @DbTest; drop @Execution(SAME_THREAD); share the lock key)

### Step 2: Web-slice lane -- @ResourceLock("WEB_SLICE_MOCKS")
- [x] refactor (annotate @WebTest with @ResourceLock("WEB_SLICE_MOCKS"); drop @Execution(SAME_THREAD) from AuthResourceTest + UserResourceTest; ensure no two web-slice methods run concurrently across classes -- they share the cached context's auto-mock beans)

### Step 3: Reset web-slice mocks between tests
- [x] refactor (auto-registered controller-dependency mocks are shared singletons in the cached @WebMvcTest context and never reset -- stubbing/invocations leak across tests within and between classes; add a JUnit BeforeEachCallback wired into @WebTest that resets every Mockito mock bean before each test)

### Step 4: Confirm no parallel flakiness (both lanes)
- [x] green-acceptance (full suite green across repeated runs; verify DB and web-slice lanes parallelize via timing, no mock/DB races, no exposed ordering assumptions)
  - 3 consecutive `./mvnw test` runs: 160 tests, 0 failures/errors/skipped each (~54s). 8 concurrent worker threads → lanes parallelize (no SAME_THREAD collapse). Stable across repeats → no mock/DB races, no exposed ordering assumptions.
