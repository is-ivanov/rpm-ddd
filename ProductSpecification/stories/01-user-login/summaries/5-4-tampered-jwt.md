# Scenario 5.4 — Tampered JWT is rejected

## green-adapter rest (2026-06-14)

**Quirk:** `io.jsonwebtoken.security.SignatureException` was unmapped and fell through to 500 — the error-handling starter does not match exception super-class hierarchy, and `SignatureException` is not a subtype of the already-mapped `MalformedJwtException`.
**Where:** `application.yml` `error.handling.http-statuses`.
**Implication:** Every jjwt exception type must be mapped individually → 422; a parent mapping never covers its siblings.

## red-adapter rest (2026-06-14)

**Quirk:** `AuthResourceTest` shares one `activationService` mock across its nested test classes, so new per-method stubbing raced `ValidateActivationTokenTest` until the class was pinned `@Execution(SAME_THREAD)`.
**Where:** `AuthResourceTest`.
**Implication:** New stubbing in this web-slice class must run SAME_THREAD or it flakes against sibling tests' shared mock.
