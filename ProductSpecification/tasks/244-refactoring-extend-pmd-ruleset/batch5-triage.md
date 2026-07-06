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
| `ClassWithOnlyPrivateConstructorsShouldBeFinal` | 3 | User keeps rule ON. `User` is a JPA aggregate (`@Table`) → can't be `final`. The 2 test classes (`ViolationAssert`, `EmailAddressGenerator`) are leaves → made `final`. | CONFIGURE (@Table) + FIX | ✅ **DECIDED** — suppress `@Table` via `violationSuppressXPath` + `final` on the 2 test leaves; verified 3→0, `test-compile` green. Applied in 5a·2. |
| `AbstractClassWithoutAbstractMethod` + `AbstractClassWithoutAnyMethod` | 2+1 | User keeps both rules ON (useful). The 3 hits are legit abstract base classes (`AbstractApplicationIntegrationTest`, `AbstractApi` — 4 subclasses + protected ctor + shared helpers, `AbstractMailIntegrationTest`). Rule ignores constructors (verified in PMD source: only `extends`/`implements` exempts), so the doc's protected-ctor note is explanatory, not an exemption. | POINT-WISE SUPPRESS | ✅ **DECIDED** — `@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")` on the first two, `@SuppressWarnings("PMD.AbstractClassWithoutAnyMethod")` on the mail base. Applied in 5a·4. |
| `UseProperClassLoader` | 2 | Rule's premise is J2EE app servers; N/A to Spring Boot fat-jar (`getClass().getClassLoader()` for co-packaged resources is correct; TCCL would be a regression). Sites: `ActivationEmailRenderer:41`, `TestResources:26`. | DISABLE | ✅ **DECIDED** — excluded (errorprone). Applied in 5a·5. |
| `AvoidSynchronizedAtMethodLevel` | 2 | Rule kept ON (valuable for prod / virtual-thread era). The 2 hits are one-shot idempotent test-infra singleton starts (`GreenMailServer.start`, `PostgresContainersLifecycleManager.init`) whose whole body is the critical section — not a hot path, vthreads off, no prod `synchronized` methods. | POINT-WISE SUPPRESS | ✅ **DECIDED** — added `"PMD.AvoidSynchronizedAtMethodLevel"` to the existing `@SuppressWarnings` on both. Applied in 5a·6. |
| `DataClass` | 1 | `UserSummaryView` is a read projection; being a data class is by design. Rule kept ON (catches anemic domain objects — a core DDD concern). | POINT-WISE / SUFFIX SUPPRESS | ✅ **DECIDED** — suppress via `violationSuppressXPath` on the `*View` suffix (project convention: `{Name}View` = read-only projection; recorded in `coding-rules.md` Naming). Same approach as LinguisticNaming/`*Assert`; verified 1→0. Applied in 5a·7. |
| `ImmutableField` | 1 | `User.login` was left non-`final` only for a *future* "login is editable" feature; that is a later concern. Currently assigned once in the ctor, never reassigned — same as the 8 existing `final` fields on `User`. | FIX (`final`) | ✅ **DECIDED** — made `final`; Hibernate still hydrates it (UserRepositorySqlInjectionTest 2✓). Applied in 5a·8. |
| `LoosePackageCoupling` | 0 | **Correction:** never counted — PMD emits a `<configerror>` (not a `<violation>`) because the mandatory `packages` property is unset, and logs `Removed misconfigured rule` on every build. The earlier "flaps 0/1" note was a misattribution (the cross-platform flap was GuardLogStatement/LiteralsFirstInComparisons, already disabled). Configuring it would duplicate Modulith + ArchUnit boundaries. | DISABLE | ✅ **DECIDED** — excluded to silence the per-build misconfiguration warning; count stays 43 (it never counted). Applied in 5a·9. |

## 🟡 Proposed CONFIGURE — narrow to fit the project

| Rule | N | Proposal | Rec | Decision |
|------|---|----------|-----|----------|
| `FieldNamingConventions` | 9 | Allow lowercase `log` (SLF4J/Lombok idiom) and ArchUnit camelCase fields. NOT covered by Checkstyle (no `ConstantName`) — rule kept ON. Sites: `log` in 5 files (4 test-infra + 1 prod UserRegisteredEventListener); ArchUnit fields `dddRules`/`onion`/`classesShouldBeNullSafe`/`modules` in `ArchitectureTest`. | CONFIGURE | ✅ **DECIDED** — `log` allowed via `constantPattern="[A-Z][A-Z_0-9]*\|log"` (rule's own config, global); `ArchitectureTest` (entirely ArchUnit descriptors) suppressed class-wide with `@SuppressWarnings("PMD.FieldNamingConventions")` + Javadoc rationale. Verified 9→0. Applied in 5b·1. |
| `TooManyMethods` | 4 | Pure method-count heuristic (default 10). The project's hard 200-line file limit already guards bloat on every file; the 4 hits are legit method-rich patterns (projection accessors, 3-tier test DSL, fluent asserts, transport overloads). A god-class can't exist under 200 lines. | DISABLE | ✅ **DECIDED** — excluded (design); the 200-line cap is the adopted bloat guard and this duplicates/conflicts with it. Verified 4→0. Applied in 5b·2. |
| `AvoidDuplicateLiterals` | 4 | Test-data literals (`"Ivan"`/`"Ivanov"`/`"Ivanovich"` name fillers in `PersonNameTest`; `"email"` property-name in `RegisterUserRequestTest`). Extracting to constants reads better and matches the project's own pattern (RegisterUserRequestTest is already constant-driven). | FIX + TIGHTEN | ✅ **DECIDED** — FIX: extracted `FIRST_NAME`/`MIDDLE_NAME`/`LAST_NAME` + `EMAIL_FIELD` constants (4→0). User also wants the rule TIGHTENED: lower `threshold` 4→2 (a literal repeated twice should be a constant) as a follow-up step. Applied FIX in 5b·3; threshold drop in 5b·4. |
| `AvoidLiteralsInIfCondition` | 3 | Test infra comparing to 0/1; add `1` to `ignoreMagicNumbers` (0 already ignored) — or disable. Sites: `ControllerDependencyAutoMockRegistrar:79`, `AssertionResponse:123`, `ConstraintViolationExceptionAssert:85`. | CONFIGURE | — |
| `UncommentedEmptyConstructor` | 1 | `UserSummaryView:74` empty ctor required by framework; `ignoreAnnotations` or disable (conflicts with the no-comments rule vs "Document empty constructor"). | CONFIGURE | — |

## 🟢 Proposed FIX in code — genuine small findings, in scope

| Rule | N | Fix | Rec | Decision |
|------|---|-----|-----|----------|
| `LiteralsFirstInComparisons` | 3 | User: DISABLE — Yoda notation hurts readability; NPE-safety (its only benefit) is owned by explicit null checks + NullAway. Also a cross-platform-variant rule. | DISABLE | ✅ **DECIDED** — excluded (bestpractices). Applied in 5a·3. |
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
- **5a·2** (ceiling 58 → 55): `ClassWithOnlyPrivateConstructorsShouldBeFinal` — suppress `@Table` (JPA `User`)
  + `final` on `ViolationAssert` and `EmailAddressGenerator` (3→0). pmd:check + test-compile green.
- **5a·3** (ceiling 55 → 52): `LiteralsFirstInComparisons` disabled (Yoda notation; NPE owned by NullAway +
  explicit null checks; 3→0). pmd:check green. NOTE: `LoosePackageCoupling` flaps 0/1 locally — pending disable.
- **5a·4** (ceiling 52 → 49): `AbstractClassWithoutAbstractMethod`/`AbstractClassWithoutAnyMethod` — rules kept
  ON, 3 legit abstract bases suppressed point-wise via `@SuppressWarnings` (3→0). pmd:check green.
  (doc follow-up: moved the suppression rationale from line comments to Javadoc.)
- **5a·5** (ceiling 49 → 47): `UseProperClassLoader` disabled (J2EE premise N/A to Spring Boot fat-jar;
  co-packaged resources load correctly via the class's own classloader; 2→0). pmd:check green.
- **5a·6** (ceiling 47 → 45): `AvoidSynchronizedAtMethodLevel` — rule kept ON, the 2 one-shot test-infra
  singleton starts suppressed point-wise (added to their existing `@SuppressWarnings`; 2→0). pmd:check green.
- **5a·7** (ceiling 45 → 44): `DataClass` — rule kept ON, suppressed via `violationSuppressXPath` on the
  `*View` suffix (`UserSummaryView`; 1→0). Recorded the `{Name}View` = read-only projection convention in
  `.claude/rules/coding-rules.md` (Naming). Same suffix-allow-list approach as LinguisticNaming/`*Assert`.
  pmd:check green.
- **5a·8** (ceiling 44 → 43): `ImmutableField` — `User.login` made `final` (1→0). It was non-final only for a
  future "editable login" feature (a later concern); assigned once in the ctor, never reassigned, like the 8
  existing `final` fields. Hibernate hydration verified (UserRepositorySqlInjectionTest 2✓). pmd:check green.
- **5a·9** (ceiling 43, unchanged): `LoosePackageCoupling` excluded. It was never a violation — PMD dropped it
  as misconfigured (`<configerror>`, mandatory `packages` unset) and warned on every build. Cleanup only:
  silences the per-build `Removed misconfigured rule` warning; boundaries stay owned by Modulith + ArchUnit.
  Corrected the earlier "flaps 0/1" misattribution. pmd:check green at 43.
- **5b·1** (ceiling 43 → 34): `FieldNamingConventions` — rule kept ON, two idioms allow-listed (9→0):
  `log` via `constantPattern="[A-Z][A-Z_0-9]*|log"` (SLF4J/Lombok logger, 5 sites); `ArchitectureTest`
  suppressed class-wide via `@SuppressWarnings` + Javadoc (ArchUnit camelCase @ArchTest fields, 4 sites).
  pmd:check green.
- **5b·2** (ceiling 34 → 30): `TooManyMethods` disabled (4→0). Pure method-count heuristic duplicated by the
  project's hard 200-line file limit (the adopted bloat guard, applies to prod too); the 4 hits are legit
  method-rich patterns (projection accessors, 3-tier test DSL, fluent asserts, transport overloads).
  pmd:check green.
- **5b·3** (ceiling 30 → 26): `AvoidDuplicateLiterals` FIX — extracted constants (4→0): `FIRST_NAME`/
  `MIDDLE_NAME`/`LAST_NAME` in `PersonNameTest` (name fillers), `EMAIL_FIELD` in `RegisterUserRequestTest`
  (property name). Both edited tests green (26✓). Trap: an earlier `replace_all "email"→EMAIL_FIELD` also
  rewrote the constant's own initializer into a self-reference (`EMAIL_FIELD = EMAIL_FIELD` → null); same in
  PersonNameTest — fixed both back to the literal before running. pmd:check green.
