# Java/Spring TDD Idioms

Tech binding for `tdd-rules.md`. Load alongside the universal rules.

## Test Disable Marker

- JUnit 5: `@Disabled` annotation on test methods/classes
- Referenced as "test disable marker" in universal rules
- In RED: add `@Disabled` after validating prediction
- In GREEN: remove `@Disabled` (the only test modification allowed)
- In commit discipline: RED commits include `@Disabled` tests

## Test Structure Conventions

### @Nested Grouping

- Use `@Nested` inner classes to group tests by method or behavior
- Inner class `@DisplayName` describes the method/behavior under test (e.g., `"constructor"`, `"hashPlain() - invalid passwords"`)
- Test method `@DisplayName` describes the specific case: `WHEN ... EXPECT ...`

### Section Comments

- Use `// GIVEN:`, `// WHEN:`, `// THEN:` section comments in every test method
- Each section separated by blank line from the code

### Naming Conventions

| Element | Pattern | Example |
|---------|--------|---------|
| Test class | `{ClassUnderTest}Test.java` | `LoginTest.java`, `PasswordPolicyTest.java` |
| Integration test class | `{Feature}IntegrationTest.java` | `LoginStatusValidationIntegrationTest.java` |
| Test method | `when_{condition}_expect_{outcome}()` | `when_invalidValue_expect_exception()` |
| Statements class | `{Feature}Statements.java` | `UserStatements.java` |
| System under test | `sut` (services/policies only) | `private final PasswordPolicy sut = ...` |

### SUT Naming

- `sut` — for domain services, policies, application services
- No `sut` — for value objects (test the object itself)

## Stub Pattern

- `throw new UnsupportedOperationException()` for real adapter stubs in RED
- `return null` as an alternative minimal stub
- Fakes are functional (not stubbed) — only real adapters use this pattern

## Parallel Execution

JUnit 5 parallel execution is enabled by default. Project meta-annotations enforce the correct mode:

| Test type | Execution | Mechanism |
|-----------|-----------|-----------|
| Unit tests (domain, usecase) | **Parallel** (default) | Plain JUnit 5 — no `@Execution` override |
| Web slice tests (`@WebTest`) | **Parallel** (default) | `@WebMvcTest` — Spring-managed |
| E2E integration (`@ApplicationIntegrationTest`) | **Sequential** | `@Execution(SAME_THREAD)` in meta-annotation — shared Testcontainers database |
| DB adapter tests (`@DataJpaTest`) | **Sequential** | `@Execution(SAME_THREAD)` in `@RepositoryTest` — shared test database |

Never manually add `@Execution(SAME_THREAD)` to individual tests — use the project's meta-annotations.

## Domain Stub Examples

- If a test asserts `Column.empty("To Do")`, Column needs only a `name` field — not a `List<Task> tasks` field and a separate `Task.java` with 4 fields
- When a domain constructor changes (e.g., adding `TaskStatus` to `Task`), patch callers with enum defaults like `TaskStatus.TODO`

## Build Green in RED — Forbidden Changes

- Never add JPA `@Column` annotations during RED
- Never add Liquibase migrations during RED
- Never add new JPA entity fields during RED
- These are implementation, not plumbing — they belong in GREEN

## GREEN Phase Artifacts

- Production code, SQL migrations (Liquibase), JPA entities, Spring Data repositories

## Coverage Tool

- JaCoCo for Java code coverage
- Reports in `target/site/jacoco/` (XML format)
- Single Maven module: one JaCoCo report (`target/site/jacoco/`) covers the whole module — domain classes exercised by usecase tests already appear, no per-module runs needed
- Test layers map to packages, not modules: domain → `*.domain`, usecase → `*.application`, web slice → `*.infrastructure.web`, acceptance → `*IntegrationTest`
- Scan touched files under `src/main/**/*.java`

## Test Filter Flag

- Maven (single module): `-Dtest='*ClassName*'` to run a single test class
- Example: `./mvnw test -Dtest='*TaskTest*'`
- DB/acceptance group: `./mvnw test -Dgroups=db`
- Acceptance: poll output file for `BUILD SUCCESS|BUILD FAILURE`

## 3-Tier Test Architecture — Java Specifics

### Test Class
- No `assertThat`, no `assertThatThrownBy` — these belong in Statements
- No `for`, `while`, `if` — control flow belongs in Statements
- No private methods, no inner records/classes

### Usecase Test Data

Usecase tests use Instancio for request objects and Statements for setup (not Scope/Builder). See `.claude/tech/java-spring/templates/usecase/test-class.md`.

## Test Data & Isolation — Java/Spring Specifics

- DB adapter tests: `@DataJpaTest` + `@DbTest` + `@Execution(SAME_THREAD)` with PostgreSQL Testcontainers
- REST adapter tests: `@WebTest` + `RestTestClient` via `AbstractApi` (auto-mocks via `ControllerDependencyAutoMockRegistrar`)
- Mockito: reset mocks/fakes before each test (`@BeforeEach` reset or `Mockito.reset()`)

### Test Data Generation — Instancio

Use Instancio for generating test objects with `bean.validation.enabled=true`:

```java
User existingUser = Instancio.of(User.class)
        .set(field(User::getLogin), new Login(login))
        .set(field(User::getStatus), status)
        .create();
```

Configuration in `src/test/resources/instancio.properties`:
```properties
bean.validation.enabled=true
```

Avoid hard-coded magic values. Use Instancio `create()` for default-filled objects, overriding only relevant fields via `.set()`.

## Assertion Library (AssertJ BDD)

Project uses AssertJ BDD style exclusively. **Never use `assertThat()` or `assertThatThrownBy()`** — use BDD equivalents:

- Strict equality: `then(actual).isEqualTo(expected)`
- Non-null (last resort): `then(actual).isNotNull()`
- Exception capture: `var caughtException = catchException(() -> ...)` then `then(caughtException).isInstanceOf(...).hasMessage(expected)`
- Throwable capture: `Throwable thrown = catchThrowable(() -> ...)` then `then(thrown).isInstanceOf(...)`
- Exact message: `.hasMessage(expectedMessage)` — for value objects
- Partial message: `.hasMessageContaining(expectedSubstring)` — for domain services/policies
- Recursive comparison: `then(actual).usingRecursiveComparison().isEqualTo(expected)`
- List recursive comparison: `then(list).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrderElementsOf(expected)`
- Reserve per-field assertions only when custom comparators or field exclusions are needed

Import convention:
```java
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
```

## Async Wait Pattern (Awaitility)

- **Negative assertion**: `Awaitility.await().during(Duration).atMost(Duration).untilAsserted(...)`
- **Waiting for side-effect**: `Awaitility.await().atMost(...).untilAsserted(...)`
- Never use `Thread.sleep` — always use Awaitility polling

## Test Review Grep Patterns

Grep patterns for the test-review-agent checklist. Each entry maps to a checklist item number.

| # | Check | Grep pattern |
|---|-------|-------------|
| 2 | Loose string assertions | `contains(\|isNotNull\|isNotEmpty\|isNotBlank` |
| 3 | Range/direction checks | `isGreaterThan\|isLessThan\|isBetween\|isAfter\|isBefore` |
| 4 | Loose mock matchers | `any(` |
| 6 | Partial collection coverage | `get(0)` |
| 12 | Assertions in test class | `assertThat\|assertThatThrownBy` |
| 21 | Calculated expected values | `Math\.\|ceil\|floor\|% \|/ \(double\)\|totalElements.*pageSize` |
| 23 | Private methods or inner types in test class | `private .* \|private record\|private class\|private static` |
| 26 | HTTP client code in acceptance Statements | `RestAssured\|given()\|baseUri\|\.get(\|\.post(\|\.put(\|\.delete(\|HttpClient\|fetch(` |

## Test Clock

- `MutableClock.advance(Duration)` to simulate time passing
- Example: `clock.advance(Duration.ofDays(31))` to expire a 30-day session

### Usecase Test Setup with MutableClock

- **Always configure `sut` in `@BeforeEach`.** Never create a separate service instance inside a test method — the `sut` field is shared across all tests in the class.
- Use `MutableClock` as the clock for `sut` in `@BeforeEach` when the test class contains time-dependent tests (token expiration, session timeout). Tests that need a "current" time work fine with a fixed instant — `MutableClock` doesn't affect them.
- To test expiration: generate data → advance clock past expiry → call `sut` method → assert exception.
- Never create a second generator/service/clock inside a test method to test expiration. Advance the shared `clock` instead.

```java
private MutableClock clock;
private ActivationService sut;

@BeforeEach
void setUp() {
    clock = MutableClock.of(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    tokenGenerator = new JwtActivationTokenGenerator(SECRET, Duration.ofHours(24), clock);
    sut = new ActivationService(userRepository, tokenGenerator, passwordPolicy);
}

@Test
void when_expiredToken_expect_throwsExpiredJwtException() {
    var token = tokenGenerator.generateToken(userId, jti);
    clock.add(Duration.ofHours(25));
    // uses the same sut, same clock — now past expiry
    Throwable thrown = catchThrowable(() -> sut.validateToken(token));
    then(thrown).isInstanceOf(ExpiredJwtException.class);
}
```

## Test Data Builder Pattern

When the same domain entity is constructed via raw Instancio calls in **3+ test files**, extract a Test Data Builder into the `fixtures` package.

**Structure:**

```java
public class UserBuilder {

    private final InstancioApi<User> builder = Instancio.of(User.class);

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public UserBuilder withEmail(String email) {
        builder.set(field(User::getEmail), new EmailAddress(email));
        return this;
    }

    public User build() {
        return builder.create();
    }
}
```

**Usage in Statements:**

```java
import static by.iivanov.rpm.iam.user.fixtures.UserBuilder.aUser;

User user = aUser().withLogin("admin").withStatus(UserStatus.ACTIVE).build();
```

**Naming:**
- Class: `{Entity}Builder`
- Factory method: `a{Entity}()` / `an{Entity}()` — correct English article by phonetics
- With-methods: `with{Field}(String)` for VO fields (builder handles VO construction), `with{Field}(Enum)` for enum fields
- Terminal: `build()`

**Threshold:** 3+ raw `Instancio.of(Entity.class).set(...)...create()` call sites for the same entity type across test files. Do NOT extract for DTOs or value objects — use Instancio directly. Refactoring template: `.claude/templates/refactoring/test-data-builder.md`.
