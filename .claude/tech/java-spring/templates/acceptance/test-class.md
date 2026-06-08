# Acceptance Test Template -- Java/Spring

> Universal structure and rules: `.claude/templates/tdd/red-acceptance.md`

## Framework Rules

- Extends `AbstractApplicationIntegrationTest`
- `@ExpectedToFail(value = "TDD Red Phase - Not yet implemented", withExceptions = AssertionError.class)` on each new RED test method (method-only marker — see `testing/red-phase-formats.md`)
- Test class named `*IntegrationTest.java`
- `@DisplayName` with scenario title from spec
- `// GIVEN:`, `// WHEN:`, `// THEN:` section comments

## Test Architecture

| Tier | Class | Purpose |
|------|-------|---------|
| Test Class | `*IntegrationTest.java` | Thin DSL, extends `AbstractApplicationIntegrationTest` |
| Test API | `*Api.java` extends `AbstractApi` | HTTP calls via `RestTestClient`, annotated `@WebApi` |
| Session Factory | `AuthSessionFactory` | Login flow, returns `SessionContext` |
| Session Context | `SessionContext` | Holds `sessionId` + `csrfToken`, passed to API methods |

## Session-First E2E Pattern

```java
@ApplicationIntegrationTest  // via AbstractApplicationIntegrationTest
class MyFeatureIntegrationTest extends AbstractApplicationIntegrationTest {
    private final SomeApi someApi;
    private final AuthSessionFactory authSessionFactory;

    MyFeatureIntegrationTest(SomeApi someApi, AuthSessionFactory authSessionFactory) {
        this.someApi = someApi;
        this.authSessionFactory = authSessionFactory;
    }

    @Test
    @DisplayName("Scenario title from spec")
    void should_expectedBehavior() {
        var admin = authSessionFactory.loginAsAdmin();
        // GIVEN: ...
        // WHEN:
        var response = someApi.someMethod(request, admin);
        // THEN:
        response.assertOk();
    }
}
```

## Reference Paths

- Base class: `src/test/java/by/iivanov/rpm/testing/AbstractApplicationIntegrationTest.java`
- `@ApplicationIntegrationTest`: `src/test/java/by/iivanov/rpm/testing/ApplicationIntegrationTest.java`
- `AbstractApi`: `src/test/java/by/iivanov/rpm/testing/api/AbstractApi.java`
- `AssertionResponse`: `src/test/java/by/iivanov/rpm/testing/api/AssertionResponse.java`
- `@WebApi`: `src/test/java/by/iivanov/rpm/testing/api/WebApi.java`
- `SessionContext`: `src/test/java/by/iivanov/rpm/testing/session/SessionContext.java`
- Example test: `src/test/java/by/iivanov/rpm/iam/auth/LoginStatusValidationIntegrationTest.java`
- Example API class: `src/test/java/by/iivanov/rpm/iam/auth/fixtures/AuthApi.java`
- Example session factory: `src/test/java/by/iivanov/rpm/iam/auth/fixtures/AuthSessionFactory.java`

## Key Paths

- Integration tests: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/`
- Test API classes: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/fixtures/`
- Production controllers: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/web/`
- DTOs: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/web/`
