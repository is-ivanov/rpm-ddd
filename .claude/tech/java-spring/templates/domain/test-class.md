# Domain Test Template -- Java/Spring

> TESTING.md Level 4: Domain unit tests. Fast, no mocks, no Spring context.
> Test: value objects, entities, aggregates, domain services, policies.

## When to Create Domain Tests

Domain tests are OPTIONAL — create them when the domain object has testable logic:

| Object type | Test when |
|-------------|-----------|
| **Value object** | Has validation in constructor (invariants, format checks) |
| **Entity / Aggregate** | Has state transition methods, factory methods with logic, computed fields |
| **Domain service / Policy** | Has business rules, decision logic, orchestration |
| **Enum** | Has behavior methods (`isTerminal()`, `authenticationErrorMessage()`) |

**Skip** when: the domain object is a plain data holder (no validation, no behavior, just getters). Coverage through usecase tests is sufficient.

## Tech-Specific Rules

- Plain JUnit 5 — no Spring context, no base class
- `sut` naming for system under test (services/policies); no `sut` for value objects
- `@Nested` inner classes grouping tests by method or behavior
- `@DisplayName` with `WHEN ... EXPECT ...` pattern
- `// GIVEN:`, `// WHEN:`, `// THEN:` section comments
- No mocks — construct real objects
- Use AssertJ BDD: `then()`, `catchException()` (for exceptions), `catchThrowable()` (for throwables)
- **Never use `assertThatThrownBy()`** — use `catchException()` + `then(caughtException)` pattern instead

## Parameterized Test Variants

| Variant | Use when | Example |
|---------|----------|---------|
| `@ValueSource` | Single invalid parameter, same expected behavior | `@ValueSource(strings = {"short", "No1!"})` |
| `@CsvSource` | Parameter + expected message pairs | `@CsvSource({"lowercase1!, Password must contain..."})` |
| `@MethodSource` + `Arguments.argumentSet()` | Multiple parameter sets with named cases, or when testing both valid AND invalid values | `Arguments.argumentSet("null value", null, "Login must not be blank")` |

## Value Object Tests

Test BOTH valid and invalid values. Use `@MethodSource` with `Arguments.argumentSet()` for named argument sets:

```java
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class LoginTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("invalidValues")
        @DisplayName("WHEN invalid value EXPECT DomainValidationException with message")
        void when_invalidValue_expect_exception(String value, String expectedMessage) {
            var caughtException = catchException(() -> new Login(value));
            then(caughtException)
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidValues() {
            return Stream.of(
                    argumentSet("null value", null, "Login must not be blank"),
                    argumentSet("blank value", "  \t  ", "Login must not be blank"),
                    argumentSet("exceeds max size", "a".repeat(51), "Login must not exceed 50..."));
        }

        @ParameterizedTest
        @MethodSource("validValues")
        @DisplayName("WHEN valid value EXPECT Login created")
        void when_validValue_expect_loginCreated(String value, String expected) {
            var login = new Login(value);
            then(login.login()).isEqualTo(expected);
        }

        static Stream<Arguments> validValues() {
            return Stream.of(
                    argumentSet("simple login", "ivanov", "ivanov"),
                    argumentSet("with spaces trimmed", "  ivanov  ", "ivanov"));
        }
    }
}
```

Key conventions:
- Variable name `caughtException` for the captured exception
- `then(caughtException).isInstanceOf(...).hasMessage(expectedMessage)` — exact message match for value objects
- Catch exceptions via `catchException()` not `catchThrowable()` (it's a narrower type)
- `@MethodSource` method returns `Stream<Arguments>`, each entry via `argumentSet(name, args...)`

## Entity / Aggregate Tests

Test behavior methods:

```java
class UserTest {

    @Nested
    @DisplayName("activate()")
    class ActivateTest {

        @Test
        @DisplayName("WHEN PENDING user activated EXPECT status ACTIVE and event published")
        void when_pendingUserActivated_expect_statusActive() {
            var user = User.create(/* ... */);
            user.activate(password);
            then(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            then(user.domainEvents()).hasSize(1);
        }
    }
}
```

## Domain Service / Policy Tests

Test business rules with InMemory fakes. Use `@ValueSource` for simple invalid inputs, `@CsvSource` for value + expected message pairs:

```java
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

class PasswordPolicyTest {
    private final PasswordPolicy sut = new PasswordPolicy(encoder);

    @Nested
    @DisplayName("hashPlain() - invalid passwords")
    class InvalidPasswordTest {

        @ParameterizedTest
        @ValueSource(strings = {"short", "No1!", "aaaaaaaaaaa"})
        @DisplayName("WHEN password too short EXPECT InvalidPasswordException")
        void when_tooShort_expect_exception(String password) {
            Throwable thrown = catchThrowable(() -> sut.hashPlain(password));
            then(thrown)
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("Password must be 12 or more characters in length.");
        }

        @ParameterizedTest
        @CsvSource({
            "lowercase1!, Password must contain 1 or more uppercase characters.",
            "UPPERCASE1!, Password must contain 1 or more lowercase characters."
        })
        @DisplayName("WHEN invalid password EXPECT InvalidPasswordException with specific violation")
        void when_invalidPassword_expect_exceptionWithViolation(String password, String expectedViolation) {
            Throwable thrown = catchThrowable(() -> sut.hashPlain(password));
            then(thrown).isInstanceOf(InvalidPasswordException.class).hasMessageContaining(expectedViolation);
        }
    }
}
```

Key conventions:
- `sut` for system under test (services/policies only — NOT for value objects)
- `catchThrowable()` for policies/services (may throw any Throwable)
- `then(thrown).hasMessageContaining()` — partial message match (policy messages may have extra context)
- `then(caughtException).hasMessage()` — exact message match for value objects

## Reference (read before generating)

- Example value object test: `src/test/java/by/iivanov/rpm/iam/user/domain/LoginTest.java`
- Example policy test: `src/test/java/by/iivanov/rpm/iam/user/domain/PasswordPolicyTest.java`
- Example entity test: `src/test/java/by/iivanov/rpm/iam/user/domain/UserTest.java`
- RpmSoftAssertions: `src/test/java/by/iivanov/rpm/testing/assertj/RpmSoftAssertions.java`
- AggregateRootAssert: `src/test/java/by/iivanov/rpm/testing/assertj/AggregateRootAssert.java`

## Key Paths

- Domain tests: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/domain/`
- Domain production: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/domain/`
- Value objects: `{subdomain}/domain/{Name}.java`
- Entities: `{subdomain}/domain/{Name}.java`
