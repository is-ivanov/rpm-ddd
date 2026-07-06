# Task 244: Extend the PMD ruleset (all categories, burn down in batches) -- Progress

Type: refactoring

Each batch = one `/continue` work unit: take a slice of the outstanding PMD violations,
disable/configure/fix per the triage policy in `spec.md`, re-run `./mvnw pmd:check -B`, set
`<maxAllowedViolations>` in `pom.xml` to the new actual count, verify green, commit.

## Setup
- [x] spec (rewritten in English; batch-based burn-down approach)
- [x] baseline: ruleset references all 8 categories; `pom.xml` ceiling = 2005 (current build green)

## Burn-down batches

### Batch 1 — `final`-modifier rules (796) — DONE, ceiling 2005 → 1209
Discussed per-rule with the user; both disabled (no stack tool enforces `final`; Palantir
does not add it; convention not adopted):
`MethodArgumentCouldBeFinal` (519, disabled), `LocalVariableCouldBeFinal` (277, disabled).
- [x] refactor (codestyle excludes + ceiling 1209 + pmd:check green)

### Batch 2 — `CommentSize` (379) — DONE, ceiling 1209 → 830
Discussed per-rule; disabled. Line length (352) is owned by Checkstyle `LineLength`=120
(applies to comments) + `.editorconfig`=120 — PMD's 80-char default conflicts with the adopted
120; the 6-line cap (27) conflicts with the project rule to preserve Javadoc/comments.
- [x] refactor (documentation exclude CommentSize + ceiling 830 + pmd:check green)

### Batch 2b — formatting/convention noise — DONE, ceiling 830 → 315
Two sub-batches, both discussed per-rule with the user:

* `UseExplicitTypes` (157, disabled) — project adopted `var` (157 usages / 54 files); no tool
  forbids it. Ceiling 830 → 673.
* Three more codestyle rules (ceiling 673 → 315), all disabled:
  - `LongVariable` (136) — 17-char cap forces abbreviating descriptive names
    (`registrationPolicy`, `emailNotificationSender`); contradicts naming convention; nothing else
    caps length.
  - `CommentDefaultAccessModifier` (136) — wants `/* package */` on package-private members; the
    modifier is self-documenting and boundaries are enforced by Modulith + ArchUnit; forced comments
    clash with the no-comments-unless-asked rule.
  - `MethodNamingConventions` (86, all `should_*`/`when_*` test methods) — duplicates Checkstyle
    `MethodName`, already `@Test`-aware via `SuppressionXpathSingleFilter`.

- [x] refactor (codestyle excludes + ceiling 315 + pmd:check green)

### Batch 3 — structural & naming style noise (5 disabled, 5 to configure) — ceiling 315 → 170
Discussed per-rule with the user. First five disabled:
- `AtLeastOneConstructor` (69, codestyle) — flags stateless/static-method + Spring @Configuration classes.
- `CallSuperInConstructor` (23, codestyle) — Java auto-inserts no-arg super(); all violations in test classes.
- `AvoidFieldNameMatchingMethodName` (20, errorprone) — record component false positives (auto-accessor).
- `LawOfDemeter` (17, design) — 9/17 enum-constant accesses (UserStatus.PENDING); rest fluent/library API.
- `OnlyOneReturn` (16, codestyle) — single-return not adopted; guard clauses idiomatic.
- [x] refactor (codestyle/design/errorprone excludes + ceiling 170 + pmd:check green)

### Batch 3b — remaining 5: explore configuration (33 violations)
`ShortVariable` (13), `LinguisticNaming` (13), `ShortMethodName` (4), `ShortClassName` (1),
`ConfusingTernary` (2). Each needs targeted config/exception instead of blanket disable.

* `ShortVariable` (13 → 0, ceiling 170 → 157) — configured `violationSuppressRegex` to allow-list
  conventional short names `(id|bd)` (name appears in the message; precise vs lowering `minimum`);
  renamed the one genuine 1-char smell `p` → `parser` in StringTrimmerJacksonDeserializer.
- [x] refactor: ShortVariable (violationSuppressRegex allow-list + rename p→parser, ceiling 157)

* `ShortMethodName` (4 → 0, ceiling 157 → 153) — the message carries no name, so used
  `violationSuppressXPath` to allow-list AST method nodes `on`/`me`/`id` (Spring @EventListener,
  /me endpoint, record-style accessor). Precise allow-list beats lowering `minimum`.
- [x] refactor: ShortMethodName (violationSuppressXPath allow-list, ceiling 153)

* `ShortClassName` (1 → 0, ceiling 153 → 152) — the message carries the name, so used
  `violationSuppressRegex` to allow-list the domain aggregate `User` (consistent with ShortVariable;
  regex chosen over XPath because the name is in the message — speed is indistinguishable, precision/
  readability is the criterion). Any other short class name still flags.
- [x] refactor: ShortClassName (violationSuppressRegex allow-list, ceiling 152)

* `ConfusingTernary` (2 → 0, ceiling 152 → 150) — the rule is sound, kept active project-wide;
  the two negation-first sites are the intentional "start-if-not-running" idiom in test infra
  (GreenMailServer.start, PostgresContainersLifecycleManager.init). Suppressed point-wise with
  `@SuppressWarnings("PMD.ConfusingTernary")` + a rationale comment (matches the existing
  `PMD.AvoidUsingHardCodedIP` suppression convention in GreenMailServer) rather than a blanket exclude.
- [x] refactor: ConfusingTernary (point-wise @SuppressWarnings on 2 test-infra methods, ceiling 150)

* `LinguisticNaming` (13 → 0, ceiling 150 → 135) — split into three precise levers instead of a blanket
  checkBooleanMethod/checkGetters disable, keeping the rule active for real prod code:
  1. Renamed the 3 UserStatements exception-catching action methods to the `when*` DSL prefix
     (getCurrentUser→whenGettingCurrentUser, validateToken→whenValidatingToken,
     activate→whenActivatingAccount) — removes the get*→void getter false positive at the source
     (separate refactoring commit 48ecdfa). Aligns with the `when*` convention already in
     LoginThrottleStatements. Incidentally let PMD recognize an assert in one test (UnitTestShouldIncludeAssert
     46→45), hence 135 not 137.
  2. `ignoredAnnotations=Test,ParameterizedTest` — exempts 2 @Test methods whose names start with the
     HTTP verb "get" (getRequest…), not getters.
  3. `violationSuppressXPath` on `*Assert` classes — exempts the 10 custom-AssertJ `has*` methods that
     return the assert type for chaining (idiomatic), keeping checkBooleanMethod active for non-Assert code.
- [x] refactor: LinguisticNaming (rename to when* + ignoredAnnotations + Assert-scoped XPath, ceiling 135)

Batch 3b COMPLETE — all 5 remaining rules configured/renamed, ceiling 170 → 135.

### Batch 4 — test-rule tuning (63 → 0, ceiling 135 → 72) — DONE
Discussed per-rule with the user. Two disabled (incompatible/conflicting), two configured:
- `UnitTestShouldIncludeAssert` (45, disabled) — the 3-tier DSL delegates verification to Statements
  `assert*` and custom AssertJ `*Assert` *instance* methods (+ BDD `then()`/`catchThrowable`); PMD's
  assert detection knows static method names only (`extraAssertMethodNames` is static-only), so it can't
  follow the delegation and flags honest tests. Value already guaranteed by RED discipline.
- `UnitTestContainsTooManyAsserts` (12, disabled) — direct conflict with "one action, assert all
  consequences (Level 1)"; all 12 hits are such L1 acceptance tests.
- `TestClassWithoutTestCases` (4 → 0) — Java rule in **errorprone** (not bestpractices); default pattern
  flags names starting/ending with Test, catching Test-prefixed infra (TestRpmDddApplication,
  TestcontainersConfiguration, TestContextValidator, TestResources). Configured `testClassPattern` to the
  project suffix convention `^(?:.*\.)?[^.]*Tests?$` (real `*Test` still checked).
- `UnitTestShouldUseTestAnnotation` (2 → 0) — default pattern `"Test"` is unanchored, matching any name
  *containing* Test (the `*TestExecutionListener` infra). Same suffix-anchored `testClassPattern`.
- [x] refactor (2 disable + 2 testClassPattern config + ceiling 72 + pmd:check green)

### Batch 4 hotfix — `GuardLogStatement` (disabled, ceiling 72 → 65)
The PR #268 CI (Linux) counted 73 vs local (Windows) 72: `GuardLogStatement` is the one rule that
resolves logger types differently across platforms (CI 10 / local 7), breaking the gate. Discussed with
the user; disabled. The project logs only with SLF4J parameterized placeholders (`log.info("... {}", arg)`)
where the level-guard is redundant — SLF4J defers message assembly past its own level check; the rule
targets the old `log.debug("x" + expensive())` concat pattern the project does not use. Removing it drops
local 72 → 65 and CI 73 → 63; ceiling set to 65 (the higher env, local), ratcheting down.
- [x] refactor (exclude GuardLogStatement + ceiling 65 + pmd:check green)

### Batch 5 — real fixes + long tail (in parts; full triage + decisions in `batch5-triage.md`)
Discussed per-rule with the user, applied in slices; ceiling ratchets down per slice.
- [~] refactor (curate ruleset + fix code, part by part)
  - [x] 5a·1: AvoidUncheckedExceptionsInSignatures (suppress @Override) + ImplicitFunctionalInterface (disable), ceiling 65→58
  - [x] 5a·2: ClassWithOnlyPrivateConstructorsShouldBeFinal (suppress @Table + final on 2 test leaves), ceiling 58→55
  - [x] 5a·3: LiteralsFirstInComparisons disabled (Yoda notation; NPE owned by NullAway), ceiling 55→52
  - [x] 5a·4: AbstractClassWithoutAbstractMethod/AnyMethod kept ON, 3 abstract bases point-wise suppressed, ceiling 52→49
  - [x] 5a·5: UseProperClassLoader disabled (J2EE premise N/A to fat-jar), ceiling 49→47
  - [x] 5a·6: AvoidSynchronizedAtMethodLevel kept ON, 2 test-infra singleton starts point-wise suppressed, ceiling 47→45
  - [x] 5a·7: DataClass kept ON, suppressed via *View suffix XPath (UserSummaryView); {Name}View convention recorded in coding-rules.md, ceiling 45→44
  - [x] 5a·8: ImmutableField — User.login made final (future editable-login is a later concern; Hibernate hydration verified), ceiling 44→43
  - [x] 5a·9: LoosePackageCoupling excluded (was a <configerror>, never counted; silences per-build misconfig warning), ceiling 43 unchanged
  - [x] 5b·1: FieldNamingConventions kept ON — log via constantPattern alt, ArchitectureTest class-wide @SuppressWarnings (9→0), ceiling 43→34
  - [x] 5b·2: TooManyMethods disabled (duplicated by 200-line file limit; method-rich patterns legit, 4→0), ceiling 34→30
  - [x] 5b·3: AvoidDuplicateLiterals FIX — extracted name/email constants in PersonNameTest + RegisterUserRequestTest (4→0), ceiling 30→26
  - AvoidDuplicateLiterals TIGHTEN (threshold 4→2 + skipAnnotations) — burn down 44 surfaced dups in sub-batches (Variant A: one-time ceiling bump then ratchet down):
    - [x] 5b·4: flip maxDuplicateLiterals=2 + skipAnnotations=true; ceiling 26→70 (one-time tightening bump, documented)
    - [x] 5b·5: PROD (3) — SecurityConfig ACTIVATE_PATH, Email/LoginAlreadyExistsExceptionHandler EMAIL_FIELD/LOGIN_FIELD → constants; ceiling 70→67. Error-code dups (VALIDATION_FAILED/ALREADY_EXISTS) split off to Task #272 (not PMD-flagged, cross-file)
    - [x] 5b·6: AuthResourceTest (9) — token/message/field-name/JSON-body constants (test-local, values unchanged); ceiling 67→58
    - [x] 5b·7: RegisterUserRequestTest (5) — field-name constants (extend EMAIL_FIELD scheme) + MALFORMED_EMAIL value; ceiling 58→53
    - [x] 5b·8: validation-message tests (Email/Login/Password/PersonName/ActivateAccountRequest) — test-local message + value constants (11 dups, pin independent of prod); ceiling 53→42
    - [x] 5b·9: remainder (16 dups) — test-local constants (JSESSIONID, SYSTEM, NOT_EXERCISED_BY_JOB, POSTGRES_PROPERTY_PREFIX, NULLAWAY_HINT, NO_EVENTS_MESSAGE, MESSAGE_FOR_PROPERTY, StringTrimmer bodies/NAME_PARAM, UserResourceTest ALREADY_EXISTS/LOGIN_FIELD/EMAIL_FIELD [ALREADY_EXISTS overlaps #272]); Constants + AuthSessionFactory point-wise @SuppressWarnings (distinct named constants sharing a value); deleted unused TestContextValidator (also cleared 5 SystemPrintln). ceiling 42→21 (5 below target 26 thanks to the deletion)
  - [x] 5c·1: MissingSerialVersionUID (7) — @SuppressWarnings("serial") on all 7 domain exceptions (Spring's own idiom; PMD 7.26 honors the javac "serial" key — verified; rule stays ON to catch genuinely-serializable classes; exceptions serialize to RFC 9457 JSON not Java binary), ceiling 21→14
  - [x] 5c·2: AvoidThrowingRawExceptionTypes (2) + PreserveStackTrace (1) — DbContainerTestExecutionListener: RuntimeException→IllegalStateException (real fix, cause preserved); PreserveStackTrace is a PMD FP on the nested catch (cause `ex` passed) → point-wise @SuppressWarnings("PMD.PreserveStackTrace"), ceiling 14→11
  - [x] 5c·3: simple mechanical group (5) — UnnecessaryAnnotationValueElement (SpaForwardingController drop `value=`), SingularField (AuthenticationServiceTest passwordEncoder→local), SimplifyBooleanReturns (GreenMailServerTestExecutionListener.hasMailTag→single &&), UseUnderscoresInNumericLiterals (GreenMailServer SMTP_PORT 33_025), UncommentedEmptyConstructor (UserSummaryView JPA ctor comment), ceiling 11→6
  - [x] 5c·4: AvoidLiteralsInIfCondition (3) — configure ignoreMagicNumbers=-1,0,1 (rule stays ON; `1` in size/length boundary checks is the same idiom PMD already exempts -1/0 for; all 3 hits are `== 1`/`<= 1`/`> 1` guards), ceiling 6→3

## Final
- [ ] green-acceptance (`./mvnw verify -B` green; ceiling at its final value, ideally 0)