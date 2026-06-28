# Task 244: Расширить набор правил PMD (quickstart как база)

Type: refactoring
Issue: #244

## Problem

Набор правил PMD проекта (`code-quality-config/pmd/ruleset.xml`) ссылается на
`rulesets/java/maven-pmd-plugin-default.xml` — минимальный legacy-набор, который
maven-pmd-plugin включает «из коробки» (пустые catch/if/while/try-блоки,
неиспользуемые импорты/переменные и т.п.). Это не рекомендованный и не сколько-нибудь
полный набор правил — фактическое покрытие PMD близко к нулю. PMD 7 раскладывает все
правила по 8 категориям и не имеет единого «recommended»-набора; ближайший официальный
стартовый набор — `rulesets/java/quickstart.xml`.

## Solution

Перейти на `rulesets/java/quickstart.xml` как базу (курируемый срез по всем 8 категориям),
затем курировать исключения:

- прогнать `./mvnw pmd:check -B`, протриажить нарушения;
- точечно исключить/настроить правила, дублирующие уже подключённые инструменты —
  Spotless (Palantir формат), Checkstyle, SpotBugs (+ find-sec-bugs), — чтобы не плодить
  конфликтующий шум;
- сохранить существующий override `TooManyStaticImports` = 7 (тестовые DSL опираются на
  статические импорты);
- реальные нарушения либо починить в рамках задачи, либо вынести в follow-up задачи/issue
  (не складывать в один мегакоммит).

## Key Files

- `code-quality-config/pmd/ruleset.xml`
- `pom.xml` (maven-pmd-plugin, секция `code-quality-config/pmd/ruleset.xml`)
