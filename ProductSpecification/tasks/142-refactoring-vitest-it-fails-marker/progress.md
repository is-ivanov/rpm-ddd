# Task 142: RED-phase marker → it.fails() (frontend) -- Progress

Type: refactoring
Issue: #142

## Spec
- [x] spec

## Fix

### Step 1: Verify mechanism (throwaway demo)
- [ ] refactor (add a throwaway `it.fails` demo test; prove runs-and-fails → green and runs-and-passes → BUILD FAILURE forcing marker removal; demo removed; record vitest version actually resolved)

### Step 2: Conventions table + tdd binding
- [ ] refactor (technology.md Conventions Frontend: Test skip marker `.skip` → `.fails` (RED-state); vue-ts/tdd.md "Test Skip Marker" section: behavior table + RED/GREEN mechanics + "pin the reason via the assertion" compensating control)

### Step 3: RED-phase templates
- [ ] refactor (vue-ts/templates/logic-test.md ".skip Convention" → `.fails` syntax + example; universal templates/workflow/red-phase-formats.md frontend marker reference)

### Step 4: Agents + universal rules
- [ ] refactor (red-agent.md + green-agent.md frontend marker wording; .claude/rules/frontend-rules.md test-skip-marker semantics — test now runs every build)

### Step 5: Final verification
- [ ] refactor (npm run test green; npm run lint green; confirm no stray `.skip` RED markers and docs consistently reference `.fails`)
