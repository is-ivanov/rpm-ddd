# Test Coverage Commands — Java/Spring (JaCoCo)

Universal workflow, module mapping, report format, and remediation: see `.claude/templates/testing/coverage-commands.md`.

## Run tests with coverage

The JaCoCo `report` goal is bound to the `verify` phase (see `jacoco-maven-plugin` in the root `pom.xml`), so coverage runs as part of `verify` — `./mvnw test` alone does NOT produce a report.

```bash
./mvnw verify -B
```

To scope to a single test class while still generating the report:
```bash
./mvnw verify -B -Dtest={TestClass}
```

The `prepare-agent` goal instruments the bytecode and the `report` goal writes `target/site/jacoco/` on every `verify`.

## Cross-module coverage

This project is a single Maven module (sources under `src/main/`, classes under `target/classes/`). The JaCoCo `report` goal covers the whole module in one report, so domain classes exercised by usecase tests already appear — no extra configuration is needed. Classes excluded from coverage are listed under the `jacoco-maven-plugin` `<excludes>` in the root `pom.xml`.

## Report locations

- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`
- HTML: `target/site/jacoco/index.html`

## Module summary

```bash
 awk -F',' 'NR>1 { lm+=$9; lc+=$10; bm+=$7; bc+=$8 } END { printf "Lines: %d/%d (%.1f%%)\nBranches: %d/%d (%.1f%%)\n", lc, lm+lc, (lm+lc>0)?lc*100/(lm+lc):100, bc, bm+bc, (bm+bc>0)?bc*100/(bm+bc):100 }' target/site/jacoco/jacoco.csv
```

## List classes with gaps

```bash
awk -F',' 'NR>1 && ($7>0 || $9>0) { printf "%s — lines: %d/%d (%.0f%%), branches: %d/%d (%.0f%%)\n", $3, $10, $9+$10, $10*100/($9+$10), $8, $7+$8, ($7+$8>0)?$8*100/($7+$8):100 }' target/site/jacoco/jacoco.csv | sort -t'(' -k3 -n
```

## Focus filter — touched files

```bash
git diff HEAD --name-only -- 'src/main/**/*.java' | sed 's/.*\///' | sed 's/\.java//'
```

## Extract uncovered lines from XML

```bash
CLASS_FILTER="{class_filter}"
awk -v filters="$CLASS_FILTER" '
BEGIN { RS="<"; FS="[\" ]"; n=split(filters,filt,",") }
/^sourcefile / { src=""; for(i=1;i<=NF;i++) if($i=="name=") { src=$(i+1); sub(/\.java$/,"",src) } }
/^line / && src {
    nr=0; mi=0; mb=0
    for(i=1;i<=NF;i++) {
        if($i=="nr=") nr=$(i+1)+0
        if($i=="mi=") mi=$(i+1)+0
        if($i=="mb=") mb=$(i+1)+0
    }
    if((mi>0 || mb>0)) {
        for(f=1;f<=n;f++) if(src==filt[f]) print "  L"nr": missed_instr="mi" missed_branch="mb
    }
}
' target/site/jacoco/jacoco.xml
```
