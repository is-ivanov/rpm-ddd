# Task 141: RED-phase marker → @ExpectedToFail (backend) -- Progress

Type: refactoring
Issue: #141

## Spec
- [x] spec

## Fix

### Step 1: Add junit-pioneer dependency + verify mechanism (throwaway demo)
- [x] refactor (add org.junit-pioneer:junit-pioneer 2.3.0 test dependency to pom.xml; throwaway demo proved abort-on-AssertionError → build green and pass → BUILD FAILURE "remove @ExpectedToFail"; demo removed; junit-pioneer 2.3.0 on test classpath. NOTE: project resolves JUnit Jupiter **6.0.3** — junit-pioneer 2.3.0 verified compatible with JUnit 6.)

### Step 2: Conventions table + tdd binding
- [x] refactor (technology.md Conventions: Test disable marker → @ExpectedToFail(withExceptions = ...); java-spring/tdd.md "RED-Phase Marker" section: behavior table + RED/GREEN mechanics + limitations + JUnit 6.0.3 compatibility note)

### Step 3: RED-phase templates
- [x] refactor (red-phase-formats.md: @ExpectedToFail syntax + import + withExceptions table + method-only note; usecase/acceptance/db/rest test-class.md + acceptance/implementation.md updated. FINDING: @ExpectedToFail is @Target({METHOD, ANNOTATION_TYPE}) — method-only, NO class-level; adapter templates now mark the single RED @Test method, not the class.)

### Step 4: Agents + universal references
- [x] refactor (red-agent.md ×2 + green-agent.md ×1 + universal templates/workflow/red-phase-formats.md ×1: replaced the now-wrong "class-level disable marker / one marker disables all methods" guidance with per-method placement, since @ExpectedToFail is method-only. Removed the literal @Disabled leak in red-agent.md. Abstract "test disable marker" term kept everywhere else — it resolves to the tech binding's Conventions value.)

### Step 5: Final verification
- [x] refactor (./mvnw verify -B → BUILD SUCCESS in 43s: 120 tests run, 0 failures, 0 errors, 0 skipped; JaCoCo coverage checks met; junit-pioneer 2.3.0 on the test classpath)
