# Task 226 — Step 1 prototype findings: automated UPPER_CASE SQL/JPQL check

## Corpus (every SQL-in-Java / SQL-in-resource site in the repo)

| Site | Form | Keywords case |
|------|------|---------------|
| `UserSummaryView.java:31` `@Subselect("select … from iam_user")` | annotation string (2-line concat) | **lowercase — the one real violation** |
| `EventPublicationCleanupExtension.java:21` `.sql("DELETE FROM …")` | JdbcClient arg | UPPER_CASE OK |
| `IamUserBaselineCleanupExtension.java:23` `.sql("""DELETE FROM …""")` | JdbcClient text block | UPPER_CASE OK |
| `DbContainerTestExecutionListener.java:84` `"""SELECT … FROM … WHERE …"""` | text block | UPPER_CASE OK |
| `FullstackSeedSchemaGuardTest.java:39` `.sql(seedSql)` | SQL read from file (no literal) | n/a |
| Liquibase `<sql>` (`2026.04.10-01-changelog-iam-user.xml:10`) | `CREATE TYPE … AS ENUM …` | UPPER_CASE OK |
| `*.sql` files (`fullstack-seed.sql`, `rpm-db-init.sql`) | raw SQL | (not Java) |

So the SQL-in-Java surface is essentially **one site type** (`@Subselect`/`@Query` annotations + a few `.sql(...)` calls), with **one existing violation**.

## Candidates measured

### A — broad lowercase-keyword regex (any `\b(select|insert|update|delete|from|where|join|set|values|order|group)\b` in Java)
- **92 matches** across `src`, **1 real**. The other 91 are false positives: field/method names (`updatedAt`, `createdBy`, `updatedBy()`), identifiers (`updated_at`, `created_by`), and prose in comments/`@DisplayName` ("selected", "updated", "the user listed").
- **~99 % false-positive rate → REJECT.** Cannot distinguish a SQL keyword from an ordinary English word or a snake_case column in a Java file by text alone.

### B — anchored regex (lowercase SQL verb leading a `@Query`/`@Subselect` value or a `.sql(...)` argument) — **CHOSEN**
- Checkstyle `RegexpMultiline`, two patterns:
  - `@(Query|Subselect)\(\s*"\s*(select|insert|update|delete|with)\b`
  - `\.sql\(\s*"{1,3}\s*[\r\n]*\s*(select|insert|update|delete|with)\b` (`matchAcrossLines=true` for text blocks)
- **Run through the real Checkstyle 13.6.0 engine over `src` (main + test): exactly 1 violation — `UserSummaryView.java:31` — 0 false positives.**
- Reuses the existing `maven-checkstyle-plugin` already bound to `verify`; no new tooling, no new module.
- **Trade-off (documented limitation):** anchors on the *leading* verb, so it would miss a mid-query lowercase clause keyword when the leading verb is already upper-case (e.g. `SELECT … from`). Catching every mid-query keyword by regex re-introduces Candidate-A false positives (a column/alias named `set`, `on`, `as`, `by`). Acceptable: the leading-verb anchor catches the realistic "whole query typed in lower case" mistake, which is the only violation that has ever occurred here.

### C — Error Prone custom `@BugPattern` check (considered, not built)
- Error Prone is already on the compiler's `annotationProcessorPaths` (v2.50.0), so it *could* host a custom check that visits `@Query`/`@Subselect` annotation values and `JdbcClient.sql(...)` arguments, tokenizes the SQL, and flags any lower-case keyword anywhere — strictly more precise and more complete than B (no leading-verb limitation, AST-scoped so no prose/identifier FPs).
- **Cost:** a separate Maven module producing a BugChecker jar, `@AutoService(BugChecker.class)`, wired onto the processor path, plus tests. **Disproportionate** to a one-site corpus. Recommend revisiting only if SQL-in-Java grows materially (multiple repositories with hand-written `@Query`/native SQL).

## Decision

Adopt **Candidate B** (anchored Checkstyle `RegexpMultiline`) in Step 2:
1. Port the two `RegexpMultiline` modules into `code-quality-config/checkstyle/my_checks.xml` (top-level, under `Checker`, scoped to `.java`).
2. Fix the existing violation: upper-case the keywords in `UserSummaryView`'s `@Subselect` (`SELECT … FROM iam_user`) — keyword-case is SQL-semantically neutral.
3. Build green with the check active.

Defer the Error Prone check (Candidate C) to a follow-up gated on SQL-in-Java growth; record review-only remains the fallback for mid-query keywords the anchored rule does not reach.
