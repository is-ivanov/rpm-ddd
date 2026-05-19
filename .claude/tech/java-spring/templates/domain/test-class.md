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

## Test Class Rules

- Plain JUnit 5 — no Spring context, no base class
- `sut` naming for system under test
- `@Nested` inner classes grouping tests by method or behavior
- `@DisplayName` with `WHEN ... EXPECT ...` pattern
- `// GIVEN:`, `// WHEN:`, `// THEN:` section comments
- No mocks — construct real objects
- Use AssertJ BDD: `then()`, `catchException()`, `catchThrowable()`, `assertThatThrownBy()`

## Value Object Tests

Parameterized tests for validity/invalidity:

```java
class LoginTest {

    @Nested
    @DisplayName("constructor validation")
    class ValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {"", "ab", "a".repeat(129)})
        @DisplayName("WHEN invalid value EXPECT ConstraintViolationException")
        void when_invalidValue_expect_exception(String value) {
            assertThatThrownBy(() -> new Login(value))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }
}
```

Use `@ValueSource` for single-parameter invalid cases.
Use `@CsvSource` for parameter + expected error message pairs.
Use `@MethodSource` for complex test data generation.

For assertions on domain events, use `RpmSoftAssertions` and `AggregateRootAssert`.

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

Test business rules with InMemory fakes:

```java
class UserRegistrationPolicyTest {
    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final UserRegistrationPolicy sut = new UserRegistrationPolicy(userRepository);

    @Test
    void when_loginExists_expect_exception() {
        userRepository.save(existingUser);
        assertThatThrownBy(() -> sut.ensureLoginIsUnique(new Login("existing")))
                .isInstanceOf(LoginAlreadyExistsException.class);
    }
}
```

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
