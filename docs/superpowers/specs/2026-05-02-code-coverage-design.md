# Code Coverage: JaCoCo + Codecov

Add test coverage enforcement and PR diff visualization to the rpm-ddd project.

## Requirements

- Enforce minimum 70% line coverage and 70% branch coverage
- Display coverage annotations in GitHub PR diffs (which lines are covered/uncovered)
- Fail CI build when coverage drops below thresholds
- Exclude standard boilerplate classes from coverage measurement

## Architecture

### Layer 1: JaCoCo Maven Plugin

`jacoco-maven-plugin` in `pom.xml` generates coverage data during tests and enforces thresholds at build time.

Plugin configuration:
- `prepare-agent` goal bound to `initialize` phase — injects JaCoCo Java agent into surefire's `argLine`
- `report` goal bound to `verify` phase — produces XML report at `target/site/jacoco/jacoco.xml`
- `check` goal bound to `verify` phase — fails the build if coverage is below thresholds

Thresholds:
- LINE coverage >= 70%
- BRANCH coverage >= 70%

Excludes (classes not counted toward coverage):
- `by.iivanov.rpm.RpmDddApplication` — Spring Boot entry point
- `by.iivanov.rpm.iam.auth.infrastructure.SecurityConfig` — Spring Security config
- `by.iivanov.rpm.shared.time.infrastructure.ClockConfiguration` — bean config

Surefire integration: JaCoCo uses late-binding `${argLine}` property so existing JVM arguments (`-Duser.country=US -Duser.language=en`) are preserved via late evaluation (`@{argLine}`).

### Layer 2: Codecov in GitHub Actions

A new step added to `.github/workflows/build.yml` after `mvn verify`:
- Uses `codecov/codecov-action@v5`
- Uploads `target/site/jacoco/jacoco.xml`
- Runs with `if: always()` so coverage is uploaded even when tests fail
- Requires `CODECOV_TOKEN` secret in repository settings

Codecov automatically provides:
- Inline annotations in PR diffs (green/red highlighting of covered/uncovered lines)
- Summary comment on each PR with coverage statistics
- Coverage trend tracking over time

### Layer 3: Codecov Configuration

A `codecov.yml` file at the project root configures coverage status checks:
- `project` status: overall project coverage must stay >= 70%
- `patch` status: new code in a PR must be >= 70% covered
- `threshold: 1%`: allows a 1% drop to avoid failures from rounding

## Files to Modify

| File | Change |
|------|--------|
| `pom.xml` | Add `jacoco.version` property, `jacoco-maven-plugin` in `pluginManagement` and `plugins`, update `surefire argLine` for late binding |
| `.github/workflows/build.yml` | Add Codecov upload step after `mvn verify` |
| `codecov.yml` (new) | Coverage status check configuration |

## Out of Scope

- Multi-module JaCoCo aggregation (project is currently a single module)
- Coverage for integration tests separately from unit tests
- Custom Codecov notification rules
