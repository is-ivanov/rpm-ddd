# Plan: red-acceptance for Scenario 2.1 — Valid activation token returns user info

## Context

Story 1 (User login) is at 25% (3/12 backend scenarios done). Scenarios 1.1-1.3 are complete. The next work unit is `red-acceptance` for Scenario 2.1: **Valid activation token returns user info**. The API endpoint `GET /api/auth/activate?token={token}` does not exist yet — it needs to be tested (red) before implementation.

The activation token infrastructure already exists in the domain:
- `JwtActivationTokenGenerator` generates JWT tokens with userId as subject, JTI, and `typ=activation` claim
- `UserRegisteredEventListener` generates tokens on user registration and sends them via `EmailNotificationSender`
- `NoOpEmailNotificationSender` (active in tests) logs tokens but doesn't deliver them

The test must register a user, obtain a valid activation token, call `GET /api/auth/activate?token={token}`, and assert 200 with login and email.

## Key Design Decision: Token Acquisition in Acceptance Tests

The acceptance test needs a valid JWT activation token. Options:

1. **Register user via API, capture token from NoOpEmailNotificationSender** — The NoOp sender only logs the token. We'd need to make it capturable (e.g., store last sent token in a thread-local field). This is the most realistic flow.

2. **Generate token directly using JwtActivationTokenGenerator** — Inject the generator into a test fixture, generate a token for a known userId. Simpler but bypasses the event-driven flow.

**Chosen: Option 2** — Use `JwtActivationTokenGenerator` directly in a test fixture. Reason: acceptance tests are black-box via HTTP, but the token is an internal implementation detail. The test validates the GET endpoint's behavior (token → user info), not the full registration→email→activation flow. The token format is a JWT that the test can generate using the same infrastructure the production code uses. This avoids modifying production code (NoOpEmailNotificationSender) just to support tests.

## Implementation Steps

### Step 1: Add `validateActivationToken` method to AuthApi

File: `src/test/java/by/iivanov/rpm/iam/auth/fixtures/AuthApi.java`

Add method:
```java
public AssertionResponse validateActivationToken(String token) {
    return get(BASE_URI + "/activate?token=" + token);
}
```

### Step 2: Create ActivationTokenFixture

File: `src/test/java/by/iivanov/rpm/iam/auth/fixtures/ActivationTokenFixture.java`

A `@Component` that injects `JwtActivationTokenGenerator` and provides:
```java
public String generateValidToken(UserId userId)
```
Uses `JtiGenerator.generate()` for JTI, delegates to `JwtActivationTokenGenerator.generateToken()`.

### Step 3: Write the acceptance test class

File: `src/test/java/by/iivanov/rpm/iam/auth/ActivationTokenValidationIntegrationTest.java`

```java
@Disabled("TDD Red Phase - Not yet implemented")
class ActivationTokenValidationIntegrationTest extends AbstractApplicationIntegrationTest {

    private final AuthApi authApi;
    private final AuthSessionFactory authSessionFactory;
    private final UserApi userApi;
    private final ActivationTokenFixture tokenFixture;

    // constructor injection

    @Test
    @DisplayName("Valid activation token returns user info")
    void should_returnUserInfo_when_validActivationToken() {
        // GIVEN: a pending user with a valid activation token
        var admin = authSessionFactory.loginAsAdmin();
        var uniqueSuffix = UUID.randomUUID().toString();
        var login = "activate_test_" + uniqueSuffix;
        var email = "activate_test_" + uniqueSuffix + "@example.com";
        var request = new RegisterUserRequest("Test", null, "User", login, email);
        var userId = userApi.registerUser(request, admin)
                .extractCreatedId("/api/admin/users/");
        var token = tokenFixture.generateValidToken(new UserId(UUID.fromString(userId)));

        // WHEN: the activation token is validated
        var response = authApi.validateActivationToken(token);

        // THEN: response status is 200 and contains login and email
        response.assertOk("""
                {
                  "login": "%s",
                  "email": "%s"
                }
                """.formatted(login, email));
    }
}
```

### Step 4: Run the test, verify it fails as predicted

**Prediction:** 404 Not Found — the `GET /api/auth/activate` endpoint does not exist yet. The Spring MVC dispatcher will return 404 for unmapped URLs.

### Step 5: Sub-skills chain

After red-agent completes the test:
1. `/test-review` — review assertions for strictness
2. `/refactor` — structural cleanup
3. Commit with message: `Story 1: red-acceptance for scenario 2.1 (valid activation token returns user info)`

## Critical Files

| File | Action |
|------|--------|
| `src/test/java/by/iivanov/rpm/iam/auth/fixtures/AuthApi.java` | Add `validateActivationToken()` method |
| `src/test/java/by/iivanov/rpm/iam/auth/fixtures/ActivationTokenFixture.java` | **New** — token generation helper |
| `src/test/java/by/iivanov/rpm/iam/auth/ActivationTokenValidationIntegrationTest.java` | **New** — acceptance test class |
| `ProductSpecification/stories/01-user-login/progress.md` | Update after completion |

## Existing Code to Reuse

- `AuthApi` — existing API fixture for `/api/auth` endpoints
- `AuthSessionFactory` — login as admin for test setup
- `UserApi` + `RegisterUserRequest` — register a pending user
- `JwtActivationTokenGenerator` — generate valid JWT activation tokens
- `JtiGenerator` — generate unique token IDs
- `AbstractApplicationIntegrationTest` — base class with Spring Boot test context
- `AssertionResponse.assertOk(String)` — assert 200 with JSON body match

## Verification

1. Run the single test: `./mvnw test -pl backend -Dtest=ActivationTokenValidationIntegrationTest`
2. Verify it fails with 404 (endpoint not yet implemented)
3. Confirm the test is disabled with `@Disabled` after verification
4. Full module test suite should still pass (new test is disabled)
