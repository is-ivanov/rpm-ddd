# Task 2: Connect Frontend Linters -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Add ESLint + Prettier tooling
- [x] Add devDependencies (eslint, @typescript-eslint, eslint-plugin-vue, eslint-config-prettier, prettier) to frontend/package.json
- [x] Add `lint`, `lint:fix`, `format` scripts
- [x] Create eslint.config.* (flat config) + prettier config aligned with .editorconfig

### Step 2: Resolve existing violations
- [x] Run `npm run lint`, fix or baseline violations until clean

### Step 3: Wire lint into CI (parallel quality workflow)
- [x] Add `frontend-lint` job (`npm ci` + `npm run lint`) to `.github/workflows/code-quality.yml`, parallel to checkstyle/pmd
- [x] Add `frontend/**` to code-quality.yml push/PR `paths` triggers
- [x] Verify lint runs and fails on violations

Notes:
- Final approach (chosen over wiring into the main build): FE lint gate lives in `.github/workflows/code-quality.yml` as a parallel `frontend-lint` job, NOT on the `build.yml` critical path. This mirrors the backend: Checkstyle and PMD have NO pom executions and run only in `code-quality.yml`; the main build keeps only Spotless (format) + error-prone/NullAway (compile-time). FE analog: `vue-tsc` type-check stays in the build (inside `npm-build`); ESLint + Prettier go to the parallel quality workflow.
- `frontend-lint` job: checkout → setup-node 22.13.0 (npm cache) → `npm ci` → `npm run lint` (eslint + prettier --check). Validated YAML: jobs = checkstyle, pmd, frontend-lint.
- `npm-lint` execution was NOT kept in the pom frontend profile. Verified `./mvnw -Pfrontend generate-resources -B`: only `npm-build` runs, BUILD SUCCESS, no lint on the build path.
- Fails-on-violations: `npm run lint` exits non-zero on any eslint/prettier issue (seen in Step 1, exit 1) → the `frontend-lint` job fails.
- Bumped `node.version` v22.12.0 → v22.13.0 in pom.xml: ESLint 10 requires `^20.19.0 || ^22.13.0 || >=24`; v22.12.0 triggered EBADENGINE warnings during `npm ci`. v22.13.0 resolves them.
