# Task 9: Codecov use_pypi instead of skip_validation -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Swap skip_validation -> use_pypi in the Codecov step
- [x] refactor (replace `skip_validation: true` with `use_pypi: true` in `.github/workflows/build.yml`; keep `fail_ci_if_error: true`; update the explanatory comment)
- [x] verify CI (workflow_dispatch run 27072460344 green; Codecov step success — log shows "Successfully installed codecov-cli-11.2.8" from PyPI + "codecov-cli upload-coverage", no cli.codecov.io download / no GPG keyserver path)
