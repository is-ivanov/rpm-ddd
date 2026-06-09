# Task 140: Add SpotBugs static analysis to the backend

Type: refactoring
Issue: #140  <- enhancement issue (not a bug); tracks this backend-tooling change

## Problem

The Maven backend (Spring Boot 4, Java 25) gates today on three source-level
analyzers — Checkstyle (`code-quality-config/checkstyle/my_checks.xml`), PMD
(`pmd:check`), and Spotless (Palantir Java Format) — all run via `./mvnw verify -B`.
None of them inspect compiled bytecode, so real bug patterns that only surface
post-compile (null-pointer dereferences, resource leaks, bad `equals`/`hashCode`,
concurrency hazards, infinite loops) slip through. The project's security-scenario
focus also wants OWASP-relevant detectors that source linters don't provide.

## Solution

Add [SpotBugs](https://spotbugs.github.io/) (with the
[find-sec-bugs](https://find-sec-bugs.github.io/) plugin) as a fourth static-analysis
gate, **complementing** the existing source-level analyzers. Division of labor:

- **SpotBugs** owns bytecode-level bug patterns (NPE, resource leaks, bad
  contracts, concurrency) — it runs after `compile`, unlike the source-based
  analyzers.
- **find-sec-bugs** adds OWASP-relevant security detectors (injection, weak crypto,
  etc.), fitting the project's security focus.
- **Checkstyle / PMD / Spotless** keep their source-level concerns unchanged.

Configuration: `spotbugs-maven-plugin` in `pluginManagement` (pinned version) +
`build/plugins` with `spotbugs:check` bound to the `verify` phase, `effort=Max`,
`threshold=Medium` (tuned after the first run). An exclude filter file under
`code-quality-config/spotbugs/` holds triaged false positives, consistent with the
existing `code-quality-config/` layout. The build fails on any remaining violation
once the baseline is clean.

## Key Files

- `pom.xml` — add `spotbugs.version` / `find-sec-bugs.version` / plugin-version
  properties; add `spotbugs-maven-plugin` to `pluginManagement` and `build/plugins`
  with `find-sec-bugs` as a plugin dependency; bind `check` to `verify`; set
  `effort=Max`, `threshold=Medium`, `excludeFilterFile`
- `code-quality-config/spotbugs/exclude-filter.xml` — new triaged false-positive
  filter (mirrors the `code-quality-config/checkstyle/` layout)
- Real-bug fixes in `src/main/java/**` surfaced by the initial triage (scope known
  only after the first run)
- `AGENTS.md` — add a `./mvnw spotbugs:check` note to the build-commands section
- `.github/workflows/build.yml` — skip SpotBugs on the critical-path `build` job
  (`-Dspotbugs.skip=true`), mirroring how Spotless is skipped there
- `.github/workflows/code-quality.yml` — new parallel `spotbugs` job
  (`mvn -B compile spotbugs:check`) enforcing it off the hot path

## Acceptance

- `./mvnw verify -B` runs SpotBugs and passes on a clean (triaged) baseline.
- A newly introduced bug pattern fails the build (`spotbugs:check`).
- SpotBugs / find-sec-bugs versions are compatible with Java 25 bytecode.
- The exclude filter documents each triaged false positive.
- `./mvnw spotbugs:check` is documented in AGENTS.md.

## Related

- Frontend static-analysis adoption in Task 139 (#139, oxlint) — this task is the
  backend analogue, extending the existing Checkstyle/PMD/Spotless gate.
