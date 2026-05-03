# Code Coverage (JaCoCo + Codecov) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enforce 70% line and branch coverage with JaCoCo, visualize coverage in GitHub PR diffs via Codecov.

**Architecture:** JaCoCo Maven plugin instruments tests and generates XML coverage report. Codecov GitHub Action uploads the report, providing inline PR annotations. A `codecov.yml` configures status checks.

**Tech Stack:** JaCoCo 0.8.14, Codecov GitHub Action v5, Maven Surefire

---

### Task 1: Add JaCoCo plugin to pom.xml

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add `jacoco.version` property**

In `<properties>`, add after `<spotless-maven-plugin.version>`:

```xml
<jacoco.version>0.8.14</jacoco.version>
```

- [ ] **Step 2: Add `jacoco-maven-plugin` to `pluginManagement`**

In `<pluginManagement><plugins>`, add after the `byte-buddy-maven-plugin` entry:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>${jacoco.version}</version>
  <configuration>
    <excludes>
      <exclude>by/iivanov/rpm/RpmDddApplication*</exclude>
      <exclude>by/iivanov/rpm/iam/auth/infrastructure/SecurityConfig*</exclude>
      <exclude>by/iivanov/rpm/shared/time/infrastructure/ClockConfiguration*</exclude>
    </excludes>
  </configuration>
  <executions>
    <execution>
      <id>prepare-agent</id>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>check</id>
      <phase>verify</phase>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.70</minimum>
              </limit>
              <limit>
                <counter>BRANCH</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.70</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

- [ ] **Step 3: Add plugin reference in `<plugins>` section**

After the `byte-buddy-maven-plugin` reference, add:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
</plugin>
```

- [ ] **Step 4: Update surefire `argLine` to use late binding**

Replace the current `argLine` in `maven-surefire-plugin` configuration:

From:
```xml
<argLine>
  -Duser.country=US -Duser.language=en
</argLine>
```

To:
```xml
<argLine>
  @{argLine} -Duser.country=US -Duser.language=en
</argLine>
```

- [ ] **Step 5: Verify build succeeds**

Run: `./mvnw verify -B`

Expected: BUILD SUCCESS. A `target/site/jacoco/jacoco.xml` file is generated.

- [ ] **Step 6: Commit**

```bash
git add pom.xml
git commit -m "Add JaCoCo code coverage plugin with 70% threshold"
```

---

### Task 2: Add Codecov upload to CI workflow

**Files:**
- Modify: `.github/workflows/build.yml`

- [ ] **Step 1: Add Codecov upload step after `mvn verify`**

After the "Build with Maven" step and before the "Upload JAR artifact" step, add:

```yaml
    - name: Upload coverage to Codecov
      if: always()
      uses: codecov/codecov-action@v5
      with:
        files: target/site/jacoco/jacoco.xml
        fail_ci_if_error: true
      env:
        CODECOV_TOKEN: ${{ secrets.CODECOV_TOKEN }}
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/build.yml
git commit -m "Add Codecov coverage upload to CI workflow"
```

---

### Task 3: Add Codecov configuration

**Files:**
- Create: `codecov.yml`

- [ ] **Step 1: Create `codecov.yml`**

```yaml
coverage:
  status:
    project:
      default:
        target: 70%
        threshold: 1%
    patch:
      default:
        target: 70%
```

- [ ] **Step 2: Commit**

```bash
git add codecov.yml
git commit -m "Add Codecov configuration with 70% coverage target"
```

---

### Task 4: Verify locally

**Files:** None (verification only)

- [ ] **Step 1: Run full build with coverage**

Run: `./mvnw verify -B`

Expected: BUILD SUCCESS. JaCoCo report generated at `target/site/jacoco/index.html`.

- [ ] **Step 2: Check coverage report**

Run: `grep -A2 'Total' target/site/jacoco/index.html | head -5` or open `target/site/jacoco/index.html` in a browser to verify coverage metrics are reported.

- [ ] **Step 3: Verify XML report exists for Codecov**

Run: `test -f target/site/jacoco/jacoco.xml && echo "OK"`

Expected: OK
