# Plan: Add Allure Report (Allure 3) for Visual Test Viewing

## Context

The project has no visual test reporting today — only raw Surefire output plus JaCoCo coverage.
The developer wants an **interactive visual view of test runs**, and specifically wants to **see
parallel test execution** (the backend suite runs `junit.jupiter.execution.parallel.enabled =
true` in concurrent mode). Allure's **Timeline** tab renders each parallel worker as a row with
colored per-test rectangles — directly satisfying that requirement.

**Why Allure 3 (not Allure 2):** The chosen project stack (`ProductSpecification/technology.md`)
is a Java/Spring backend **plus** a planned Vue 3 + TypeScript + **Vitest** frontend and
**Playwright** E2E suite (frontend scenarios in `stories/01-user-login/progress.md` are all
pending). That means a Node.js toolchain is already mandated, and the project will soon have
**three test ecosystems**. All three have Allure adapters (`allure-junit5`, `allure-vitest`,
`allure-playwright`) that emit the **same `allure-results` JSON format**. Allure 3's Node-based
renderer aggregates multiple result sources into **one unified report** (single Timeline across
backend + frontend + E2E) and produces a **single self-contained HTML file** ideal for CI
artifacts. Allure 2's `allure-maven` plugin is JVM-only and could not pull in the Node-side
results, forcing a later migration. Choosing Allure 3 now avoids a backend-only island.

**Key architectural point:** the *test instrumentation* dependency is unchanged regardless of
renderer — `allure-junit5` (the `io.qameta.allure` Java adapter) runs in the JVM during
`./mvnw test` and just writes `allure-results/*.json`. **Node is needed only to *render* the
report**, not to run backend tests.

Scope decisions (confirmed with the developer):
- Tool: **Allure 3**, rendered via the Node CLI (`npx allure`).
- Backend platform: JUnit (JUnit Platform 6 under Spring Boot 4).
- Viewing: **local** (`npx allure serve`) **and** publish a single-file report artifact in CI.
- Detail level: **minimal core reporting — no AspectJ weaver**. Timeline / parallel visualization
  needs no `@Step`/`@Attachment`, so we avoid a Java agent on the new Java 25 runtime. AspectJ can
  be added later if step-level detail is wanted.

## Current State (verified)

- Single-module Maven project (`pom.xml` at root only), Java 25, Spring Boot 4.0.6.
- JUnit Platform 6 via the Boot parent (`junit-jupiter-api` + `junit-platform-launcher`,
  `pom.xml:240-248`).
- Surefire (`pom.xml:451-466`): `<argLine>@{argLine} -Duser.country=US -Duser.language=en</argLine>`
  (`@{argLine}` = JaCoCo's late-bound agent) with parallel concurrent execution enabled.
- Custom `DbContainerTestExecutionListener` registered via
  `src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener`.
  Allure's adapter ships its **own** services entry in its jar, so both register together —
  **no edit to that file needed**.
- No application `package.json` exists yet (only `.opencode/node_modules` tooling).
- CI: `.github/workflows/build.yml` runs `mvn verify -B` on JDK 25, uploads JaCoCo to Codecov.

## Changes

### 1. `pom.xml` — backend result production
- In `<properties>` (`pom.xml:19-47`): add `<allure.version>` (latest stable Allure 2.x adapter
  line — the Java adapter version; verify on Maven Central, e.g. 2.29.x).
- Add a `<dependencyManagement>` block (none today) importing the Allure BOM:
  ```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-bom</artifactId>
        <version>${allure.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  ```
- Add the test dependency (near the JUnit block):
  ```xml
  <dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-junit5</artifactId>
    <scope>test</scope>
  </dependency>
  ```
  It integrates via the JUnit Platform launcher `TestExecutionListener` SPI, auto-capturing
  host/thread per test (feeds the Timeline). **No `<argLine>` change**, **no `allure-maven`
  plugin** (rendering is done by the Node CLI).

### 2. `src/test/resources/allure.properties` (new file)
Pin the JVM results output under `target/`:
```properties
allure.results.directory=target/allure-results
```

### 3. Root `package.json` (new file) — report renderer tooling
A small repo-root `package.json` pinning the Allure 3 CLI as a devDependency (reproducible,
better than unpinned `npx`). This is repo-wide tooling and is separate from the future
`frontend/package.json`.
```json
{
  "name": "rpm-ddd-tooling",
  "private": true,
  "devDependencies": { "allure": "^3" },
  "scripts": {
    "report:serve": "allure serve target/allure-results",
    "report": "allure generate target/allure-results --output target/allure-report --single-file --clean"
  }
}
```
(Confirm the exact npm package name/version for the Allure 3 CLI — `allure` / `@allurereport/cli`
— and the precise `generate`/`serve` flags against the pinned version during implementation.)

### 4. `.gitignore`
Add `node_modules/` (and confirm `target/` is already ignored — it is). No need to commit
`allure-results` or generated reports.

### 5. `.github/workflows/build.yml` — publish single-file report artifact
After the `mvn verify` step, add (all with `if: always()` so reports appear even on test failure):
- `actions/setup-node@v4` (Node LTS).
- `npm ci` (installs the pinned Allure 3 CLI).
- `npm run report` → generates `target/allure-report` as a single self-contained HTML file.
- `actions/upload-artifact@v7` uploading `target/allure-report` as `allure-report`
  (short retention).

Report generation stays out of the Maven lifecycle, so `mvn verify` and CI test time are
unaffected; the Node steps only render the already-produced results. GitHub Pages publishing with
trend history is a deliberate optional follow-up, not in this scope.

## Files to modify / create
- `pom.xml` — `<allure.version>` property, new `<dependencyManagement>`, `allure-junit5` test dep.
- `src/test/resources/allure.properties` — **new**.
- `package.json` (repo root) — **new** (Allure 3 CLI + npm scripts).
- `.gitignore` — add `node_modules/`.
- `.github/workflows/build.yml` — Node setup + generate + upload-artifact steps.

## Forward-looking (out of scope, noted for later)
When the frontend lands, add `allure-vitest` (Vitest) and `allure-playwright` (Playwright),
configured to write into their own results dirs (e.g. `frontend/allure-results`). Then point
`allure generate` at **multiple** results directories to produce one unified report spanning
backend + frontend + E2E. No backend rework needed — the format is already shared.

## JUnit Platform 6 compatibility note
Allure's adapter integrates via the stable `org.junit.platform.launcher.TestExecutionListener`
SPI, whose package/contract is unchanged in JUnit Platform 6. Use the latest Allure adapter
release to maximize compatibility. Because JUnit 6 is new, treat result-file generation as the
gating verification step below — if `target/allure-results` stays empty after a test run, bump to
the newest Allure adapter version before troubleshooting further.

## Verification (end-to-end)
1. `./mvnw test` — confirm `target/allure-results/` is populated with `*-result.json` files
   (one per test). This is the critical JUnit-6 compatibility gate.
2. `npm install` then `npm run report:serve` (or `npx allure serve target/allure-results`) —
   opens the interactive report. Confirm:
   - **Timeline** tab shows multiple worker rows running concurrently (validates the
     parallel-runs requirement).
   - Tree / Suites show pass/fail with `@DisplayName` text and parameterized cases.
3. `npm run report` — confirm a single-file `target/allure-report` HTML opens directly in a
   browser (no server needed).
4. Open a PR — confirm the CI `allure-report` artifact is produced and opens to the same report.
