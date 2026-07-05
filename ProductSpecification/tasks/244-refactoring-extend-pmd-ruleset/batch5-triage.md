# Batch 5 — Triage (65 violations, 26 rules)

Working document for the Batch 5 burn-down. We go through it **in parts**. Each rule carries a
recommendation + evidence; the **Decision** column is filled in as the user decides, then applied.

Baseline at start of Batch 5: ceiling **65** (after GuardLogStatement disabled in the Batch 4 hotfix).
Snapshot taken from `./mvnw pmd:check -B` → `target/pmd.xml`.

Decision legend: `DISABLE` · `CONFIGURE` · `FIX` (in code) · `KEEP` (leave flagged, defer) · `—` (undecided).

Planned execution: **5a** = ruleset-only changes (disable + configure), **5b** = real code fixes.

---

## 🔴 Proposed DISABLE — framework/persistence conflict or non-adopted convention

| Rule | N | Evidence | Rec | Decision |
|------|---|----------|-----|----------|
| `AvoidUncheckedExceptionsInSignatures` | 4 | User keeps the rule ON (agrees with it). All 4 sites are `@Override` (framework-imposed signatures we don't own). | CONFIGURE — suppress `@Override` | ✅ **DECIDED** — `violationSuppressXPath="./ancestor::MethodDeclaration[ModifierList/Annotation[@SimpleName='Override']]"`; verified 4→0, rule stays active for non-override methods. Apply in 5a. |
| `ImplicitFunctionalInterface` | 3 | Domain ports + a **Spring Data repository** (`SpringDataUserSummaryRepository`). `@FunctionalInterface` is semantically wrong and freezes them at one method. | DISABLE | ✅ **DECIDED** — excluded (bestpractices). Applied in 5a·1. |
| `ClassWithOnlyPrivateConstructorsShouldBeFinal` | 3 | `User` is a JPA/Hibernate aggregate (`@Table`, `AbstractAggregateRoot`, jMolecules, `@Version`) → cannot be `final` (proxying/weaving). Rule fights persistence. Also 2 test classes. | DISABLE | — |
| `AbstractClassWithoutAbstractMethod` + `AbstractClassWithoutAnyMethod` | 2+1 | Abstract base **test** classes (`AbstractApplicationIntegrationTest`, `AbstractApi`, `AbstractMailIntegrationTest`) are abstract to share setup / prevent instantiation. Idiomatic. | DISABLE | — |
| `UseProperClassLoader` | 2 | Rule's premise is J2EE app servers; N/A to Spring Boot fat-jar (`getClass().getClassLoader()` for bundled resources is correct). Sites: `ActivationEmailRenderer:41`, `TestResources:26`. | DISABLE | — |
| `AvoidSynchronizedAtMethodLevel` | 2 | Method-level sync on test-infra singletons (`GreenMailServer:45`, `PostgresContainersLifecycleManager:130`) is a legitimate choice; rule is stylistic. | DISABLE | — |
| `DataClass` | 1 | `UserSummaryView` is a read projection; being a data class is by design. | DISABLE | — |
| `ImmutableField` | 1 | `User.login` is a JPA entity field (active-persistence); entity fields not forced `final`. | DISABLE | — |
| `LoosePackageCoupling` | 1 | Needs a hand-maintained package list we don't keep; boundaries enforced by Modulith + ArchUnit. Also the cross-platform-flaky 0/1 rule. | DISABLE | — |

## 🟡 Proposed CONFIGURE — narrow to fit the project

| Rule | N | Proposal | Rec | Decision |
|------|---|----------|-----|----------|
| `FieldNamingConventions` | 9 | Allow lowercase `log` (SLF4J/Lombok idiom) and ArchUnit `@ArchTest` fields (camelCase). NOT covered by Checkstyle (no `ConstantName`). Allow-list via regex + `ignoredAnnotations`/XPath. Sites: `log` in 5 files; ArchUnit fields `dddRules`/`onion`/`classesShouldBeNullSafe`/`modules` in `ArchitectureTest`. | CONFIGURE | — |
| `TooManyMethods` | 4 | Raise threshold (default 10): test DSL (`AuthApi`, `UserStatements`, `AssertionResponse`) and projection `UserSummaryView` legitimately have many methods; the 200-line file limit already guards bloat. | CONFIGURE | — |
| `AvoidDuplicateLiterals` | 4 | All test-data literals (`"Ivanovich"`, `"Ivanov"`, `"Ivan"` in `PersonNameTest`; `"email"` in `RegisterUserRequestTest`). Raise threshold or exclude tests. | CONFIGURE | — |
| `AvoidLiteralsInIfCondition` | 3 | Test infra comparing to 0/1; add `1` to `ignoreMagicNumbers` (0 already ignored) — or disable. Sites: `ControllerDependencyAutoMockRegistrar:79`, `AssertionResponse:123`, `ConstraintViolationExceptionAssert:85`. | CONFIGURE | — |
| `UncommentedEmptyConstructor` | 1 | `UserSummaryView:74` empty ctor required by framework; `ignoreAnnotations` or disable (conflicts with the no-comments rule vs "Document empty constructor"). | CONFIGURE | — |

## 🟢 Proposed FIX in code — genuine small findings, in scope

| Rule | N | Fix | Rec | Decision |
|------|---|-----|-----|----------|
| `LiteralsFirstInComparisons` | 3 | Swap operands (`"x".equals(y)`) — NPE safety. Sites: `DbContainerTestExecutionListener:48`, `GreenMailServerTestExecutionListener:43`, `PostgresContainersLifecycleManager:80`. | FIX | — |
| `PreserveStackTrace` + `AvoidThrowingRawExceptionTypes` | 1+2 | `DbContainerTestExecutionListener:72/88` — throw a specific type carrying the cause `e`. | FIX | — |
| `UnnecessaryAnnotationValueElement` | 1 | `SpaForwardingController:20` — `@GetMapping(value="…")` → `@GetMapping("…")`. | FIX | — |
| `SimplifyBooleanReturns` | 1 | `GreenMailServerTestExecutionListener:40` — collapse `if/return` into one `return`. | FIX | — |

## ⚪ Needs user decision — judgment calls

| Rule | N | Options | Rec | Decision |
|------|---|---------|-----|----------|
| `MissingSerialVersionUID` | 7 | All on domain exceptions. **DISABLE**: app never Java-serializes exceptions (errors go out as RFC 9457 Problem Detail JSON), so the field is meaningless. Alt: FIX (7 one-line `serialVersionUID`). Sites: `EmailAlreadyExistsException`, `InvalidPasswordException`, `LoginAlreadyExistsException`, `TooManyLoginAttemptsException`, `UserAuthenticationException`, `UserNotFoundException`, `DomainValidationException`. | DISABLE (lean) | — |
| `SystemPrintln` | 5 | All in `TestContextValidator:14-22` (context-startup diagnostics). FIX → move to a logger, or suppress if intentional (runs before logging is configured). | — | — |
| `FieldDeclarationsShouldBeAtStartOfClass` | 2 | Trivial move fields up — or DISABLE (not adopted). Sites: `ArchitectureTest:58`, `PostgresContainersLifecycleManager:100`. | — | — |
| `SingularField` | 1 | `AuthenticationServiceTest:36` `passwordEncoder` field → local variable (FIX), or ignore. | — | — |
| `UseUnderscoresInNumericLiterals` | 1 | `GreenMailServer:28` port `33025` → `33_025` (FIX), or DISABLE (pointless for ports). | — | — |
| `UseVarargs` | 1 | `AssertionResponse:122` array param → varargs (FIX), or ignore. | — | — |

---

## Progress log (fill as we go)

- **5a·1** (ceiling 65 → 58): `AvoidUncheckedExceptionsInSignatures` configured (suppress `@Override`, 4→0) +
  `ImplicitFunctionalInterface` disabled (3→0). pmd:check green.
