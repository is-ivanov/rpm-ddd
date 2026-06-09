# Task 139: Add oxlint static analysis to the frontend -- Progress

Type: refactoring
Issue: #139

## Spec
- [x] spec

## Fix

### Step 1: Add oxlint dependency + .oxlintrc.json config
- [x] refactor (added `oxlint` ^1.69.0 dev dep to frontend/package.json; created frontend/.oxlintrc.json — categories correctness=error/suspicious=warn, ignorePatterns mirror eslint.config.js. `npx oxlint` exit 0 clean on 25 src files; verified it flags a deliberate redeclaration; `npm run lint` still green)

### Step 2: Add lint:oxlint script + compose with lint
- [x] refactor (added `lint:oxlint` = `oxlint --max-warnings=0` so suspicious warnings also fail the gate; `lint` now runs `npm run lint:oxlint && eslint . && prettier --check .` (oxlint as fast first gate); `lint:fix` runs `oxlint --fix` first. Both `npm run lint:oxlint` and composed `npm run lint` exit 0 clean)

### Step 3: Dedupe ESLint rules via eslint-plugin-oxlint
- [x] refactor (added `eslint-plugin-oxlint` ^1.69.0 dev dep; appended `...oxlint.buildFromOxlintConfigFile('.oxlintrc.json')` as the LAST entry in eslint.config.js — derives the dedup from .oxlintrc.json, turning off 112 ESLint rules oxlint covers (e.g. constructor-super, getter-return, no-async-promise-executor). Composed `npm run lint` exit 0, no double-reporting; IDE inspection clean)

### Step 4: Wire oxlint into CI
- [x] refactor (added explicit "Run oxlint (fast gate, fail on violations)" step running `npm run lint:oxlint` before the ESLint+Prettier step in code-quality.yml frontend-lint job; oxlint is also already inside the composed `npm run lint`, but the explicit step fast-fails and gives a clearly-labeled CI check. package.json stays the single source of truth for `lint`)

### Step 5: Document the workflow
- [x] refactor (updated .claude/tech/vue-ts/infrastructure.md "Static Analysis (Pre-Commit)" — `npm run lint` now described as oxlint→ESLint→Prettier with the two-linter split + dedup; added new human-facing frontend/README.md documenting scripts + the oxlint/ESLint/Prettier workflow. Prettier-formatted; `npm run lint` green)

### Step 6: Final verification
- [~] refactor (npm run lint:oxlint + npm run lint both clean; CI green)
