# Task 224: Serialize DB tests with @ResourceLock instead of SAME_THREAD -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Swap SAME_THREAD for @ResourceLock("DB")
- [ ] refactor (add @ResourceLock to @ApplicationIntegrationTest + @DbTest; drop @Execution(SAME_THREAD))

### Step 2: Confirm no parallel-DB flakiness
- [ ] green-acceptance (full suite green across repeated runs)
