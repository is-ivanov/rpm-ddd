# Controller Test Template -- Java/Spring

> Universal rules: `.claude/templates/tdd/red-rest.md`

## Test Pattern: @WebTest + RestTestClient

Project uses `@WebTest` meta-annotation that auto-mocks ALL controller dependencies via `ControllerDependencyAutoMockRegistrar`. Test API classes (`@WebApi`) encapsulate HTTP calls through `AbstractApi` → `RestTestClient`.

**No `@MockBean` declarations needed** — dependencies are auto-mocked when no bean exists.

## Architecture

| Tier | Class | Location |
|------|-------|----------|
| Test Class | `{Controller}Test.java` | same package as controller in `src/test/java/` |
| Test API | `{Controller}Api.java` extends `AbstractApi` | `fixtures/` package in subdomain |
| Expected JSON | `{feature}_{method}_out.json` | `src/test/resources/__files/{context}/{subdomain}/web/` |

## Test Class Rules

- Annotate with `@WebTest`
- Inject `@WebApi`-annotated API class via constructor
- Use `@Nested` inner classes to group by endpoint
- Use `@DisplayName` with `WHEN ... EXPECT ...` pattern
- `@Test` method: call API → `AssertionResponse.assert*()`
- ONE `@Test` method per test class in RED; mark it `@ExpectedToFail(withExceptions = ...)` (method-only marker — see `testing/red-phase-formats.md`)

**What to test at this level** (Level 2 — Web Slice, per `TESTING.md`):
- Validation errors: `assertBindingError("__files/.../out.json")`
- Business exception → HTTP status code mapping: mock service to throw exception, assert status + error body
- Do NOT test happy path — covered by acceptance tests (Level 1, `@ApplicationIntegrationTest`)

## Test API Class Rules

- Extend `AbstractApi`, annotate with `@WebApi`
- Inject `RestTestClient` via constructor, pass to `super(restClient)`
- Define `BASE_URI` constant for the controller path
- One method per endpoint, returns `AssertionResponse`
- Methods accept `SessionContext` for authenticated requests

## Expected Response JSON

Place in `src/test/resources/__files/{context}/{subdomain}/web/`. Use exact Problem Detail format for error responses (RFC 9457).

For validation errors: `assertBindingError("__files/.../out.json")` — uses json-unit with `IGNORING_ARRAY_ORDER`.

For success responses: `assertOk("__files/.../out.json")` or inline JSON string.

## DTO Validation — Complex vs Flat

**Flat DTO** (all fields validatable in one request):
- ONE web test method with `@ExpectedToFail(withExceptions = ...)` covers all validation rules.
- Use a JSON request body that violates all constraints at once.

**Nested/Complex DTO** (nested objects, custom validators, 3+ distinct constraints):
- Web test: verify exactly ONE invalid variant — confirms `@Valid` is present and returns error response.
- DTO validation test: create `*RequestTest.java` in same package, using `Validator validator = Validation.buildDefaultValidatorFactory().getValidator()` to test ALL validation rules individually.
- See `.claude/tech/java-spring/templates/rest/dto-validation-test.md` for the full template.

## Reference (read before generating)

- `@WebTest`: `src/test/java/by/iivanov/rpm/testing/WebTest.java`
- `AbstractApi`: `src/test/java/by/iivanov/rpm/testing/api/AbstractApi.java`
- `AssertionResponse`: `src/test/java/by/iivanov/rpm/testing/api/AssertionResponse.java`
- `@WebApi`: `src/test/java/by/iivanov/rpm/testing/api/WebApi.java`
- Example API class: `src/test/java/by/iivanov/rpm/iam/auth/fixtures/AuthApi.java`
- JSON template: `src/test/resources/__files/iam/auth/web/login_beanValidation_out.json`

**Note:** `AuthResourceTest.java` contains a happy-path test at Level 2 — this pattern is being corrected. Do NOT use it as a template for new web slice tests. New tests at this level should cover validation errors and error response mapping only.

## Key Paths

- Tests: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/web/`
- API classes: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/fixtures/`
- Production controllers: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/web/`
- DTOs: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/web/`
- JSON templates: `src/test/resources/__files/{context}/{subdomain}/web/`
