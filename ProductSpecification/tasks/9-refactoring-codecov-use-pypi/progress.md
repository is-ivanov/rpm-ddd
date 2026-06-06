# Task 9: Codecov use_pypi instead of skip_validation -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Swap skip_validation -> use_pypi in the Codecov step
- [ ] refactor (replace `skip_validation: true` with `use_pypi: true` in `.github/workflows/build.yml`; keep `fail_ci_if_error: true`; update the explanatory comment)
- [ ] verify CI (push branch, confirm the "Upload coverage to Codecov" step succeeds on a green run)
