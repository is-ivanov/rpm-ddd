# Testing Strategy

## Test Pyramid

Each level tests only what is NOT covered by the level above it. Happy path is verified at Level 1, so Levels 2-4 focus on corner cases.

```
        ┌──────────────┐
        │  Level 1     │  @SpringBootTest(RANDOM_PORT) — happy path e2e
        │  e2e / Full  │  ~1-2 tests per use case
        ├──────────────┤
        │  Level 2     │  @WebMvcTest — validation + error responses
        │  Web Slices  │  1-2 tests per endpoint
        ├──────────────┤
        │  Level 3     │  Unit tests — corner cases of business logic
        │  App Services│  Test Fixture + InMemory implementations
        ├──────────────┤
        │  Level 4     │  Unit tests — fast, no mocks
        │  Domain      │  Value objects, entities, aggregates, policies
        └──────────────┘
        ┌──────────────┐
        │  Infra       │  @DataJpaTest — complex repository queries
        │  (ad-hoc)    │  Only for @Query, native SQL, Specifications
        └──────────────┘
```

## Level 1 — E2E (Full Application Integration)

- **Annotation:** `@ApplicationIntegrationTest` (existing meta-annotation combining `@SpringBootTest` + `@DbTest`).
- **Web environment:** `RANDOM_PORT`.
- **What to test:** happy path of every use case via HTTP API.
- **What NOT to test:** error cases, validation, corner cases — those belong to lower levels.
- **Execution:** sequential (enforced by `@Execution(SAME_THREAD)` built into `@ApplicationIntegrationTest`). All e2e tests share one Testcontainers database.
- **Example:** call `POST /api/users` with valid data → assert 200 + correct response body.

```java
@ApplicationIntegrationTest
class UserRegistrationIntegrationTest {

    @Test
    void registerUser() {
        // GIVEN: valid registration request
        // WHEN: POST /api/users
        // THEN: 201 with user ID in response
    }
}
```

## Level 2 — Web Slices (Controllers)

### Meta-annotation

Use `@WebTest` — a meta-annotation that loads all controllers with auto-mocked dependencies.

```java
@WebTest
class UserResourceTest {
    @Autowired RestTestClient restClient;
}
```

### How auto-mocking works

`@WebTest` imports `ControllerDependencyAutoMockRegistrar` — a `BeanDefinitionRegistryPostProcessor` that:
1. Finds all controller bean definitions in the registry.
2. Inspects each controller's constructor parameters via reflection.
3. For each parameter type that has no bean definition registered, creates a `Mockito.mock()` bean.

No manual mock declarations needed. When a new controller with new dependencies is added, mocks are created automatically.

### Exception: controllers with custom security

For controllers that require specific security configuration or custom filters, use a per-controller test:

```java
@WebMvcTest(SpecialController.class)
@Import(SecurityTestConfig.class)
class SpecialControllerTest { ... }
```

### What to test per controller

**Flat DTO** (all fields can be made invalid in one request):
- Test all validation rules in a single web test method.

**Nested/Complex DTO**:
- In the web test: verify exactly ONE invalid variant — confirming `@Valid` annotation is present and returns an error response.
- In a separate unit test alongside the DTO: test all validation rules using `Validator validator = Validation.buildDefaultValidatorFactory().getValidator();`.

### DTO validation unit tests

Place validation unit tests next to the DTO in the `infrastructure/web` package:

```
src/test/java/.../infrastructure/web/
├── UserResourceTest.java          ← @WebTest slice
├── RegisterUserCommandTest.java   ← unit test with Validator (for complex DTOs)
```

## Test API Classes

One class per controller, encapsulating HTTP calls via `RestTestClient`. Usable in both e2e and web slice tests. Discovered via `@WebApi` annotation filter — place in the `fixtures` package of each subdomain.

### Placement

API classes go into the same `fixtures` package as service fixtures:

```
src/test/java/by/iivanov/rpm/iam/user/fixtures/
├── UserApi.java                              ← API class for UserResource
├── UserRegistrationServiceFixture.java       ← fixture for application service tests
└── ...
```

### Convention

- Extend `AbstractApi`, inject `RestTestClient` via constructor.
- Annotate with `@WebApi` (meta-annotation containing `@Component`).
- Methods return `AssertionResponse` — fluent assertions and body extraction.
- One method per controller endpoint.

```java
// by.iivanov.rpm.iam.user.fixtures.UserApi
@WebApi
public class UserApi extends AbstractApi {

    private static final String BASE_URI = "/api/admin/users";

    public UserApi(RestTestClient restClient) {
        super(restClient);
    }

    public AssertionResponse registerUser(Object request) {
        return post(BASE_URI, request);
    }
}
```

### Usage in e2e test

```java
@ApplicationIntegrationTest
class UserRegistrationIntegrationTest {

    private final UserApi userApi;
    private final AuthSessionFactory authSessionFactory;

    @Test
    void registerUser() {
        var admin = authSessionFactory.loginAsAdmin();
        userApi.registerUser(validRequest, admin).assertCreated();
    }
}
```

### Usage in web test

```java
@WebTest
class UserResourceTest {

    @Autowired UserApi userApi;

    @Test
    void validationError() {
        userApi.registerUserRaw("{}").unwrap()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
```

### Session-first e2e fixtures

When an e2e test needs authentication, keep the login flow separate from controller APIs:

- `AuthApi` stays transport-only.
- `AuthSessionFactory` performs the login flow and returns `SessionContext`.
- Pass `SessionContext` explicitly to API methods that require authentication.

```java
var admin = authSessionFactory.loginAsAdmin();
userApi.registerUser(request, admin).assertCreated();
```

This keeps the session object small and lets controller APIs stay reusable across sessioned and non-sessioned tests.

## Level 3 — Application Services (Use Cases)

- **No Spring context.** Pure unit tests.
- **No Mockito mocks** for repository/port interfaces. Use **InMemory test implementations** instead.
- For external integrations (email, message broker): test implementations of port interfaces.
- For database: `InMemoryXxxRepository` implementations.
- **What to test:** corner cases of business logic — duplicate login, duplicate email, invalid state transitions, etc.

### Test Fixture pattern

Each application service gets a Test Fixture class that:
1. Instantiates the service with InMemory dependencies.
2. Exposes the service under test.
3. Exposes InMemory dependencies for assertions and setup.
4. Provides helper methods for common setup scenarios.

```java
public class UserRegistrationServiceFixture {

    final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    final InMemoryUserUniquenessChecker uniquenessChecker = new InMemoryUserUniquenessChecker();
    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    final UserRegistrationService service;

    public UserRegistrationServiceFixture() {
        var passwordPolicy = new PasswordPolicy(passwordEncoder);
        var passwordGenerator = new PasswordGenerator();
        var registrationPolicy = new UserRegistrationPolicy(uniquenessChecker);
        this.service = new UserRegistrationService(
                userRepository, registrationPolicy, passwordPolicy, passwordGenerator);
    }

    public UserRegistrationServiceFixture withExistingUser(User user) {
        userRepository.save(user);
        return this;
    }
}
```

```java
class UserRegistrationServiceTest {

    @Test
    void throwsWhenLoginAlreadyExists() {
        var fixture = new UserRegistrationServiceFixture()
                .withExistingUser(existingUser);

        assertThatThrownBy(() -> fixture.service.registerUser(command, createdBy))
                .isInstanceOf(LoginAlreadyExistsException.class);
    }
}
```

### Fixture placement

Fixtures and InMemory implementations are placed **per subdomain**, not in a shared `testing` package.

- **Fixtures:** in `fixtures` subpackage of each subdomain.
- **InMemory implementations:** in the same `infrastructure` package as the production repository interface — keeps them in sync during refactoring and gives access to package-private members.

```
src/test/java/by/iivanov/rpm/iam/user/
├── fixtures/
│   └── UserRegistrationServiceFixture.java
├── infrastructure/
│   └── InMemoryUserRepository.java        ← same package as production UserRepository interface
└── ...

src/test/java/by/iivanov/rpm/testing/
├── api/
│   ├── AbstractApi.java                  ← shared base class
│   ├── WebApi.java                       ← @Component meta-annotation
├── assertj/
│   └── ...
└── ...
```

## Level 4 — Domain Objects

- **Fast unit tests, no mocks.**
- **Value objects:** parameterized tests for validity/invalidity (`@ParameterizedTest` + `@MethodSource`).
- **Entities / Aggregates:** test behavior — invariants, state transitions, domain events.
- **Domain services / policies:** test business rules.
- Use `RpmSoftAssertions` for asserting domain events on aggregates.

```java
@ParameterizedTest
@MethodSource("invalidLogins")
void rejectsInvalidLogin(String value) {
    assertThatThrownBy(() -> new Login(value))
            .isInstanceOf(ConstraintViolationException.class);
}
```

## Infrastructure Tests — Complex Repository Queries

Not a pyramid level, but an ad-hoc category for testing infrastructure adapters.

- **Annotation:** `@RepositoryTest` — meta-annotation combining `@DataJpaTest` + `@DbTest` + `@Execution(SAME_THREAD)`.
- **When to use:**
  - Custom `@Query` with JPQL — easy to get syntax wrong.
  - Native SQL queries — no type safety.
  - JPA Specifications — complex composition logic.
  - Queries with JOIN FETCH — risk of N+1 or incorrect joins.
- **When NOT to use:** simple Spring Data derived queries (`findByXxx`) — these are framework boilerplate.

```java
@RepositoryTest
class CustomUserRepositoryTest {

    @Autowired
    CustomUserRepository repository;

    @Test
    void findByLoginWithRoles() {
        // test custom @Query that fetches user with eagerly loaded roles
    }
}
```

## Conventions

### Naming
- `*Test.java` for all tests.
- `*IntegrationTest.java` for Level 1 e2e tests only.

### Test structure
- Use `@Nested` inner classes to group tests by method or behavior.
- Use `// GIVEN:`, `// WHEN:`, `// THEN:` section comments.
- Use `@DisplayName` with `WHEN ... EXPECT ...` pattern where `@Nested` name is not self-documenting.

### Assertions
- AssertJ BDD style: `then()`, `catchThrowable()`, `catchIllegalArgumentException()`.
- Custom: `RpmSoftAssertions` for aggregate domain events and `ConstraintViolationException`.

### Parallel execution
- Enabled by default for unit and web tests.
- **Disabled for e2e tests** (`@Execution(SAME_THREAD)`) — shared Testcontainers database.
- **Disabled for `@DataJpaTest`** — shared test database.

### Formatting
- All test code is formatted by Spotless (Palantir Java Format). Run `./mvnw spotless:apply`.
- Checkstyle and PMD apply to test sources (`includeTests=true`).

### Test data
- Use Instancio for generating test objects. Configured with `bean.validation.enabled=true` in `instancio.properties`.
- Avoid hard-coded magic values. Use descriptive factory methods or Instancio create.

### Profile
- Tests use `@ActiveProfiles("test")` via `@DbTest` meta-annotation.
- `application-test.yml` enables lazy bean initialization and configures Liquibase.

## Existing Test Infrastructure

The project provides shared test utilities in `by.iivanov.rpm.testing`:

| Component | Description |
|-----------|-------------|
| `@ApplicationIntegrationTest` | Meta-annotation: `@SpringBootTest` + `@DbTest` + `@Execution(SAME_THREAD)` |
| `@WebTest` | Meta-annotation: `@WebMvcTest` + auto-mock registrar + `@AutoConfigureRestTestClient` + `@WebApi` filter scan |
| `@RepositoryTest` | Meta-annotation: `@DataJpaTest` + `@DbTest` + `@Execution(SAME_THREAD)` |
| `ControllerDependencyAutoMockRegistrar` | Auto-mocks all missing controller constructor dependencies |
| `@WebApi` | Meta-annotation (`@Component`) for test API classes, discovered by `@WebTest` filter |
| `AbstractApi` | Base class for test API helpers — `post()`, `get()`, `postRaw()` via `RestTestClient`. Returns `AssertionResponse` |
| `AssertionResponse` | Fluent assertion/extraction wrapper over `RestTestClient.ResponseSpec`. Assert status, compare JSON bodies (classpath resource or inline), extract DTOs, headers, Location IDs |
| `@ModuleIntegrationTest` | Meta-annotation: `@ApplicationModuleTest` + `@DbTest` |
| `@DbTest` | Tags test as `"db"`, activates `"test"` profile |
| `DbContainerTestExecutionListener` | Auto-starts PostgreSQL (local or Testcontainers) for `@DbTest` tests |
| `RpmSoftAssertions` | AssertJ soft assertions + domain event / violation helpers |
| `AggregateRootAssert` | Fluent assertions for aggregate domain events |
| `ConstraintViolationExceptionAssert` | Fluent assertions for `ConstraintViolationException` |
| `DefaultViolations` | Factory for common violation expectations (`notNull()`, `notBlank()`, `email()`) |
