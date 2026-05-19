# Usecase Test Template -- Java/Spring

> Universal rules: `.claude/templates/tdd/red-usecase.md`

## Architecture

| Tier | Class | Purpose |
|------|-------|---------|
| Test Class | `{Service}Test.java` | Thin DSL, plain JUnit 5 (no base class) |
| Statements | `{Feature}Statements.java` | Setup methods + in-memory fakes |
| InMemory Fakes | `InMemory{Port}.java` | In-memory implementations of domain ports |
| Request DTO | `{Action}Command.java` | Request data, use Instancio to create |

## Tech-Specific Rules

- Plain JUnit 5 — no Spring context, no base class
- Add `@Disabled` annotation in RED
- `@Nested` inner classes grouping tests by method
- `@DisplayName` with `WHEN ... EXPECT ...` pattern
- `// GIVEN:`, `// WHEN:`, `// THEN:` section comments
- `sut` naming for system under test
- Use `catchException()` for exception assertions, `then()` for value assertions (AssertJ BDD)
- Use Instancio for test data generation (`Instancio.of(Command.class).set(...).create()`)
- Not-implemented marker: `throw new UnsupportedOperationException("Not implemented yet")`

## Statements Class

- Plain Java class (no annotation needed)
- Exposes in-memory fakes as public fields
- Setup methods named `given*()` — create and save domain objects
- Uses Instancio to generate default-filled domain objects, overriding only relevant fields

## InMemory Fakes

- Placed in `infrastructure/` subpackage of the subdomain's test sources (same package as the port interface)
- Implement domain repository/port interfaces using `Map` or `List` storage
- Functional in RED — not stubbed

## Reference (read before generating)

- Example test: `src/test/java/by/iivanov/rpm/iam/user/application/AuthenticationServiceTest.java`
- Example Statements: `src/test/java/by/iivanov/rpm/iam/user/fixtures/UserStatements.java`
- Example InMemory fake: `src/test/java/by/iivanov/rpm/iam/user/infrastructure/InMemoryUserRepository.java`
- Example request DTO: `src/main/java/by/iivanov/rpm/iam/user/application/RegisterUserCommand.java`
- RpmSoftAssertions: `src/test/java/by/iivanov/rpm/testing/assertj/RpmSoftAssertions.java`
- AggregateRootAssert: `src/test/java/by/iivanov/rpm/testing/assertj/AggregateRootAssert.java`

## Key Paths

- Tests: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/application/`
- Statements: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/fixtures/`
- InMemory fakes: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/`
- Production services: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/application/`
- Request DTOs: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/application/`
