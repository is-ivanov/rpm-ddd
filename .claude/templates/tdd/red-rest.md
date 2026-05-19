# Controller Test Template -- Universal

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
