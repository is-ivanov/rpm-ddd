# Task 139: Add oxlint static analysis to the frontend -- Progress

Type: refactoring
Issue: #139

## Spec
- [x] spec

## Fix

### Step 1: Add oxlint dependency + .oxlintrc.json config
- [ ] refactor (add `oxlint` dev dep to frontend/package.json; create frontend/.oxlintrc.json with correctness/suspicious categories; align `ignorePatterns` with eslint.config.js ignores)

### Step 2: Add lint:oxlint script + compose with lint
- [ ] refactor (add `lint:oxlint` npm script; run oxlint first as a fast gate, then ESLint+Prettier; verify `npm run lint:oxlint` clean or document triaged baseline)

### Step 3: Dedupe ESLint rules via eslint-plugin-oxlint
- [ ] refactor (add `eslint-plugin-oxlint` dev dep; append its flat config to eslint.config.js to disable ESLint rules oxlint already covers; verify `npm run lint` still clean, no double-reporting)

### Step 4: Wire oxlint into CI
- [ ] refactor (.github/workflows/code-quality.yml frontend-lint job runs oxlint so violations fail the build)

### Step 5: Document the workflow
- [ ] refactor (frontend README / AGENTS notes + .claude/tech/vue-ts/infrastructure.md "Static Analysis" so the pre-commit gate runs oxlint)

### Step 6: Final verification
- [ ] refactor (npm run lint:oxlint + npm run lint both clean; CI green)
