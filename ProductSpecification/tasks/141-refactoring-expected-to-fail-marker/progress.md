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
- [~] refactor (red-phase-formats.md annotation syntax + examples; usecase/acceptance/db/rest test-class.md + acceptance/implementation.md)

### Step 4: Agents + universal references
- [ ] refactor (red-agent.md add-marker-after-prediction wording; green-agent.md remove-marker-only wording; universal "test disable marker" references where changed semantics matter)

### Step 5: Final verification
- [ ] refactor (./mvnw verify -B green with junit-pioneer on classpath)
