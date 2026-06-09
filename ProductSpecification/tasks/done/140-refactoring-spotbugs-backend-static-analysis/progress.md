# Task 140: Add SpotBugs static analysis to the backend -- Progress

Type: refactoring
Issue: #140

## Spec
- [x] spec

## Fix

### Step 1: Add spotbugs-maven-plugin + find-sec-bugs to pom.xml
- [x] refactor (added `spotbugs-maven-plugin.version` 4.9.8.4 + `findsecbugs.version` 1.14.0 properties; declared `spotbugs-maven-plugin` in `pluginManagement` with `find-sec-bugs` as a nested SpotBugs plugin, `effort=Max`, `threshold=Medium`, `excludeFilterFile=code-quality-config/spotbugs/exclude-filter.xml`. Plugin 4.9.8.4 bundles SpotBugs core 4.9.8 which supports JDK 25 bytecode (BCEL 6.11.0, JDK 25 GA build support added in core 4.9.7) — no core override needed. `./mvnw spotbugs:help` resolves + loads cleanly under JDK 25; IDE inspection on pom.xml clean)

### Step 2: Create the SpotBugs exclude filter file
- [x] refactor (created `code-quality-config/spotbugs/exclude-filter.xml` — empty `<FindBugsFilter>` baseline (no `<Match>` = excludes nothing), wired via `excludeFilterFile`; mirrors the `code-quality-config/checkstyle/` layout. Used the classic no-namespace filter form (SpotBugs-native) to avoid IDE remote-XSD-resolution errors; IDE inspection clean)

### Step 3: Run initial analysis and triage findings
- [x] refactor (first run analyzed Java 25 bytecode cleanly — SpotBugs core 4.9.8 confirmed JDK 25 compatible. 13 Medium findings triaged → clean baseline (0). REAL FIXES: `CurrentUserResponse` adds `List.copyOf(roles)` defensive copy (EI_EXPOSE_REP/REP2, aligns with the defensive-copies rule); `JwtActivationTokenGenerator` made `final` (CT_CONSTRUCTOR_THROW — consistent with the implicitly-final validating value-object records). EXCLUDED (documented false positives): RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT on record canonical constructors + `AbstractAggregateRoot` super() across `*.domain.*` (6 findings); NM_CLASS_NAMING_CONVENTION on `$jMolecules$` generated classes (4 findings). Also added a forward-looking documented exclusion of CT_CONSTRUCTOR_THROW for `*.application.*` — the finding is N/A to Spring singleton beans (trusted-config construction, no attacker-controlled subclassing; AOP-proxied app services can't be made `final`). SpotBugs has no annotation matcher, so scoped by the application-service `*Service` naming convention — narrow enough to leave command/DTO records and other application-layer types under the detector. `spotbugs:check` exit 0; checkstyle+pmd exit 0; affected tests 9 passed / 0 failed; IDE inspections clean)

### Step 4: Bind spotbugs:check to verify (fail the build)
- [x] refactor (added a bare `spotbugs-maven-plugin` ref to `build/plugins` with execution `spotbugs-check` binding the `check` goal to the `verify` phase; version+config inherited from `pluginManagement`. Unlike checkstyle/pmd (standalone CI jobs), SpotBugs runs inside the main build per the issue. Verified: `./mvnw -B -DskipTests -Djacoco.skip=true verify` shows `spotbugs:4.9.8.4:check (spotbugs-check)` executing at verify → BUILD SUCCESS on the clean baseline; pom IDE inspection clean)

### Step 5: Document the command in AGENTS.md
- [x] refactor (added a `./mvnw spotbugs:check -B` bullet to the Build/Test/Development commands section — notes find-sec-bugs, effort=Max/threshold=Medium, the exclude-filter location, and the verify binding; updated the `verify -B` bullet to mention SpotBugs)

### Step 6: Take SpotBugs off the CI critical path (mirror Spotless)
- [x] refactor (kept the `verify` binding for local `./mvnw verify`; added `-Dspotbugs.skip=true` to both `build.yml` verify invocations (PR + main) so it's off the critical-path `build` job; added a parallel `spotbugs` job to `code-quality.yml` running `mvn -B compile spotbugs:check` — compiles first because SpotBugs analyzes bytecode, unlike the source-based checkstyle/pmd jobs. Verified: parallel command `mvn -B compile spotbugs:check` → BUILD SUCCESS; critical-path `mvn -Dspotbugs.skip=true verify` → "Spotbugs plugin skipped", no analysis, BUILD SUCCESS. YAML IDE inspections clean (build.yml warnings pre-existing codecov-action params))

### Step 7: Final verification
- [x] refactor (Part A: temporarily removed `final` from `JwtActivationTokenGenerator` → `mvn -B compile spotbugs:check` failed with 1 bug `CT_CONSTRUCTOR_THROW` (BUILD FAILURE) exactly as predicted, then reverted (working tree clean). Part B: full `./mvnw verify -B` → BUILD SUCCESS in 57s with SpotBugs running at the verify phase on the clean baseline; tests 120 passed / 0 failed / 0 skipped)
