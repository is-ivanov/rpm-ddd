# Task 140: Add SpotBugs static analysis to the backend -- Progress

Type: refactoring
Issue: #140

## Spec
- [x] spec

## Fix

### Step 1: Add spotbugs-maven-plugin + find-sec-bugs to pom.xml
- [ ] refactor (add version properties; declare `spotbugs-maven-plugin` in `pluginManagement` with `find-sec-bugs` plugin dependency, `effort=Max`, `threshold=Medium`, `excludeFilterFile`; verify Java 25 bytecode compatibility — bump version if needed)

### Step 2: Create the SpotBugs exclude filter file
- [ ] refactor (create `code-quality-config/spotbugs/exclude-filter.xml` — empty/minimal baseline filter, wired via `excludeFilterFile`; mirrors the `code-quality-config/checkstyle/` layout)

### Step 3: Run initial analysis and triage findings
- [ ] refactor (run `./mvnw spotbugs:check`; fix real bugs in `src/main/java/**`; add documented `<Match>` entries to the exclude filter for triaged false positives until the baseline is clean)

### Step 4: Bind spotbugs:check to verify (fail the build)
- [ ] refactor (add `spotbugs-maven-plugin` to `build/plugins` with the `check` goal bound to the `verify` phase so `./mvnw verify -B` fails on violations)

### Step 5: Document the command in AGENTS.md
- [ ] refactor (add a `./mvnw spotbugs:check -B` note to the Build/Test/Development commands section in AGENTS.md)

### Step 6: Final verification
- [ ] refactor (`./mvnw verify -B` runs SpotBugs and passes on the clean triaged baseline; confirm a deliberately-introduced bug pattern fails `spotbugs:check`, then revert it)
