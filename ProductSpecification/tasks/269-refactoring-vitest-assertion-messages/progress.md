# Task 269: Vitest assertion messages over comments -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Codify the convention in .claude docs
- [ ] update green-agent "RED-Comment Reframing" (prefer message arg for per-assertion rationale) + vue-ts/tdd.md (message-arg convention; note the playwright/tdd.md precedent)
- [ ] record the FE preference (memory, mirroring the backend AssertJ as() note)

### Step 2: Pilot — migrate the Scn 3.4 multi-column block
- [ ] refactor (per-assertion rationale -> expect(actual, message); keep block/dataset comments)
- [ ] run vitest users-grid.logic.test.ts (green)

### Step 3: Sweep the remaining FE Vitest logic + API-client tests
- [ ] refactor (apply the hybrid rule across FE Vitest tests)
- [ ] run npm run lint + full FE test suite (green)
