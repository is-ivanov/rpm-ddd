# Task 14: RED-phase marker → @ExpectedToFail (backend) -- Progress

Type: refactoring
Issue: #141

## Spec
- [x] spec

## Fix

### Step 1: Add junit-pioneer dependency + verify mechanism (throwaway demo)
- [ ] refactor (add org.junit-pioneer:junit-pioneer 2.x test dependency to pom.xml; throwaway demo test proves abort-on-predicted-exception → build green and pass → build fails; remove demo; `./mvnw test` resolves junit-pioneer)

### Step 2: Conventions table + tdd binding
- [ ] refactor (technology.md Conventions: Test disable marker → @ExpectedToFail(withExceptions = ...); java-spring/tdd.md marker description + RED/GREEN mechanics)

### Step 3: RED-phase templates
- [ ] refactor (red-phase-formats.md annotation syntax + examples; usecase/acceptance/db/rest test-class.md + acceptance/implementation.md)

### Step 4: Agents + universal references
- [ ] refactor (red-agent.md add-marker-after-prediction wording; green-agent.md remove-marker-only wording; universal "test disable marker" references where changed semantics matter)

### Step 5: Final verification
- [ ] refactor (./mvnw verify -B green with junit-pioneer on classpath)
