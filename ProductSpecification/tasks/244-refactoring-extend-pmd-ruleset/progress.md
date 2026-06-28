# Task 244: Расширить набор правил PMD (quickstart как база) -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Базу набора переключить на quickstart
- [ ] refactor (config: `ruleset.xml` база `maven-pmd-plugin-default.xml` → `rulesets/java/quickstart.xml`, override `TooManyStaticImports`=7 сохранить)

### Step 2: Триаж нарушений и курирование исключений
- [ ] refactor (run `./mvnw pmd:check -B`, протриажить нарушения)
- [ ] refactor (config: исключить/настроить правила, дублирующие Spotless / Checkstyle / SpotBugs+find-sec-bugs)

### Step 3: Закрыть оставшиеся реальные нарушения
- [ ] refactor (починить реальные нарушения; то, что вне scope — вынести в follow-up issue)
- [ ] refactor (cleanup)

### Step 4: Верификация
- [ ] green-acceptance (`./mvnw verify -B` зелёный: PMD + остальные проверки)
