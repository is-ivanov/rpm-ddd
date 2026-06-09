# Task 150: Bring frontend/acceptance under strict TS + inspection config -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Strict TS type-checking for the acceptance tree
Add `frontend/acceptance/**` to a strict tsconfig `include` (or a dedicated
`tsconfig.acceptance.json` via project references) and wire a type-check into the lint/CI gate
(same gate as the frontend lint job). Verify the false-positive `!` warnings disappear (the `!`
become validated-required under strict). Do NOT remove the existing `!` assertions.
- [ ] refactor (tsconfig: acceptance under strict + type-check gate wiring)

### Step 2: Fix type issues surfaced across the acceptance tree
Enabling the check will likely surface latent type issues across the existing acceptance specs /
Statements. Fix them (config/type-level only; no test-behaviour changes, skip markers untouched).
- [ ] refactor (fix surfaced acceptance type issues)

### Step 3: Inspection scope for test fixtures
Mark the acceptance tree as test code for the IDE/SonarLint inspection scope so secret-detection
heuristics (S2068 "hard-coded password") don't fire on intentional test fixtures.
- [ ] refactor (inspection scope for acceptance test fixtures)
