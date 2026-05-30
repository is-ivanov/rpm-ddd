# Task 2: Connect Frontend Linters -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Add ESLint + Prettier tooling
- [ ] Add devDependencies (eslint, @typescript-eslint, eslint-plugin-vue, eslint-config-prettier, prettier) to frontend/package.json
- [ ] Add `lint`, `lint:fix`, `format` scripts
- [ ] Create eslint.config.* (flat config) + prettier config aligned with .editorconfig

### Step 2: Resolve existing violations
- [ ] Run `npm run lint`, fix or baseline violations until clean

### Step 3: Wire into Maven `frontend` profile / CI
- [ ] Add `npm-lint` execution (`npm run lint`) to frontend-maven-plugin in pom.xml
- [ ] Verify `mvn verify -B -Pfrontend` runs lint and fails on violations
