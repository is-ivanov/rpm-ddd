# Controller Test Template -- Universal

## Test Pyramid — What to Test at This Level

Per project `TESTING.md`, this is **Level 2 (Web Slice)**. Each level tests only what is NOT covered by the level above.

| Level | What | Where |
|-------|------|-------|
| Level 1 (e2e) | Happy path | `@ApplicationIntegrationTest` |
| **Level 2 (this level)** | **Validation errors + error response mapping** | `@WebTest` |
| Level 3 (usecase) | Corner cases of business logic | Unit tests with InMemory fakes |
| Level 4 (domain) | Value object validation, state transitions | Unit tests, no mocks |

**Test at this level:**
- Request DTO validation (422 with field errors)
- Business exception → HTTP status code mapping (401, 404, 422)
- Error response body format (Problem Detail / RFC 9457)

**Do NOT test at this level:**
- Happy path — already covered by acceptance tests (Level 1)
- Business logic correctness — covered by usecase tests (Level 3)
- Response DTO field content for successful responses — covered by acceptance tests

**Skip `[S]` when** the endpoint is a simple delegation: controller extracts params → calls usecase → returns response. No validation, no error mapping. The acceptance test alone is sufficient.

## Test Pattern

1. **Create test API method** (if new endpoint): add method to existing `@WebApi` class, or create new API class extending `AbstractApi`.
2. **Setup**: no explicit setup needed — `@WebTest` auto-mocks all controller dependencies via `ControllerDependencyAutoMockRegistrar`.
3. **Execute**: HTTP request via the test API class (`AbstractApi.post()` / `.get()` → `AssertionResponse`).
4. **Verify**: `AssertionResponse.assertBindingError()` for validation, `.assertOk()` / `.assertCreated()` for success, `.unwrap()` for raw access.

## Test API Class — Reuse Check

Before creating a new `@WebApi` class:
1. Search `fixtures/` packages for existing API classes for the same controller (grep for the controller's `BASE_URI`).
2. If found → add the new endpoint method to the existing class.
3. If not found → create a new `@WebApi` class extending `AbstractApi`.

## Request DTO

Create in the controller's DTO directory. Request DTO must have a `toUsecaseRequest()` conversion method (or equivalent factory).

## HTTP Status Code Mapping

| Scenario Type | Expected Status |
|--------------|-----------------|
| Successful creation | `201 Created` |
| Successful query | `200 OK` |
| Successful action (no body) | `200 OK` |
| Validation error | `422 Unprocessable Content` |
| Authentication error | `401 Unauthorized` |
| Not found | `404 Not Found` |

## DTO Validation Strategy

**Flat DTO**: test ALL validation rules in ONE web test — send an invalid JSON body that triggers all constraints simultaneously.

**Complex/Nested DTO**: web test verifies `@Valid` presence (one invalid variant → 422). All individual validation rules tested in a separate unit test using `Validator`.

## JSON Fixture Files

Place expected response bodies in `src/test/resources/__files/{context}/{subdomain}/web/`.

- Validation errors: use Problem Detail format (RFC 9457) with `fieldErrors` array.
- Success responses: include all fields the acceptance test asserts.
