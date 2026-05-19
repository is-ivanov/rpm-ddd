# Controller Implementation Template -- Universal

## Rules

- Replace the not-implemented marker with actual logic
- Implement MINIMAL code -- only what's needed for this test to pass
- Thin controllers per coding-rules.md: accept request, convert DTO, call usecase, return response
- Constants for magic numbers (e.g., token expiry times)

## Implementation Pattern

1. **Controller method**: receive `@Valid` request DTO, convert to usecase request, call application service, return `ResponseEntity`.
2. **Global error handler** (if new exception): map domain exception to Problem Detail response in the existing error handler.
3. **Response DTO** (if acceptance test expects response body): create DTO with static factory `from(domainObject)`.

## Controller Conventions (from coding-rules.md)

- Thin: accept request → convert → call usecase → return response
- NO business logic
- HTTP status: 200 with body, 201 for creation, 204 for no body
- Errors via centralized exception handler
