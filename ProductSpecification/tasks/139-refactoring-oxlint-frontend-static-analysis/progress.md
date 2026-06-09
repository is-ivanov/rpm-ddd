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
- [~] refactor (add `eslint-plugin-oxlint` dev dep; append its flat config to eslint.config.js to disable ESLint rules oxlint already covers; verify `npm run lint` still clean, no double-reporting)

### Step 4: Wire oxlint into CI
- [ ] refactor (.github/workflows/code-quality.yml frontend-lint job runs oxlint so violations fail the build)

### Step 5: Document the workflow
- [ ] refactor (frontend README / AGENTS notes + .claude/tech/vue-ts/infrastructure.md "Static Analysis" so the pre-commit gate runs oxlint)

### Step 6: Final verification
- [ ] refactor (npm run lint:oxlint + npm run lint both clean; CI green)
