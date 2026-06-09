# Task 142: RED-phase marker → it.fails() (frontend) -- Progress

Type: refactoring
Issue: #142

## Spec
- [x] spec

## Fix

### Step 1: Verify mechanism (throwaway demo)
- [x] refactor (throwaway `it.fails` demo verified on resolved **vitest 4.1.8**: failing assertion → "1 expected fail", build green (exit 0); passing assertion → "Error: Expect test to fail", build FAILS (exit 1) forcing marker removal at GREEN. Demo removed — never committed. NOTE: `it.fails` has no `withExceptions` analog — an unrelated failure still counts as "expected fail", so the RED reason must be pinned via a specific assertion inside the test.)

### Step 2: Conventions table + tdd binding
- [x] refactor (technology.md Conventions Frontend: Test skip marker `.skip` → `.fails`; vue-ts/tdd.md "Test Skip Marker" → "RED-Phase Marker (`it.fails`)" section mirroring backend: behavior table + RED/GREEN mechanics + "pin the reason via the assertion" compensating control + limitations. `.skip` retained for genuine non-RED skips.)

### Step 3: RED-phase templates
- [x] refactor (vue-ts/templates/logic-test.md ".skip Convention" → ".fails Convention (RED-phase marker)" with `it.fails` example + pin-the-reason note; universal templates/workflow/red-phase-formats.md "Frontend Skip Convention" → "Frontend RED-Phase Marker" referencing `it.fails`/`test.fails` + runs-every-build semantics + pin-the-reason)

### Step 4: Agents + universal rules
- [x] refactor (green-agent.md: "Frontend Skip Convention" → "Frontend RED-Phase Marker" + two parentheticals now name the `.fails` modifier; red-agent.md reference → frontend RED-phase marker `it.fails`; frontend-rules.md test-skip-marker line now states runs-every-build + GREEN-enforcement + pin-the-reason-via-assertion. Generic "test disable/skip marker" phrasings covering both stacks left intact.)

### Step 5: Final verification
- [x] refactor (npm run test → 17 passed (6 files); npm run lint → exit 0 (eslint + prettier clean); no stray `it.skip`/`test.skip` in frontend code; residual `.skip` doc mentions are intentional contrastive references — `.fails` replaces `.skip` for RED, `.skip` retained for genuine non-RED skips)
