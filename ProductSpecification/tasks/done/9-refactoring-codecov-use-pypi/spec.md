# Task 9: Codecov use_pypi instead of skip_validation

Type: refactoring

## Problem

The "Upload coverage to Codecov" step in `.github/workflows/build.yml` currently sets
`skip_validation: true`. This was a workaround for a flaky GPG step: the Codecov wrapper
fetches Codecov's public key from a keyserver on every run, and an empty/failed fetch
(`gpg: no valid OpenPGP data found`) aborted the upload with
`gpg: Can't check signature: No public key`, failing CI (because `fail_ci_if_error: true`).

`skip_validation: true` fixes the flake but disables the integrity check entirely — the
Codecov CLI binary is then downloaded and executed without verifying it is authentic
(supply-chain risk). We only want to bypass the flaky keyserver path, not give up integrity.

## Solution

Replace `skip_validation: true` with `use_pypi: true`. This installs the Codecov CLI from
PyPI (with pip's package-hash integrity) instead of downloading it from `cli.codecov.io`,
bypassing the keyserver/GPG path that flakes while keeping a supply-chain integrity guarantee.

Keep `fail_ci_if_error: true` so genuine coverage-upload failures still gate CI.

## Key Files

- `.github/workflows/build.yml` — "Upload coverage to Codecov" step (job `build`)
