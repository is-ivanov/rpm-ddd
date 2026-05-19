# DTO Validation Test Template -- Java/Spring

> TESTING.md Level 2: For complex/nested DTOs, test ALL Bean Validation rules using `Validator`.
> Flat DTOs (all fields validatable in one request) don't need this — one web test covers everything.

## When to Create

Create a DTO validation test when the web request DTO has **3+ distinct validation constraints** or **nested objects with their own validation**. Flat DTOs with only `@NotBlank`/`@NotNull` on top-level fields don't need separate tests.

## Test Class Rules

- Plain JUnit 5 — no Spring context
- `@Nested` grouping by validation category
- `@DisplayName` with `WHEN ... EXPECT ...` pattern
- `// GIVEN:`, `// WHEN:`, `// THEN:` section comments
- Create `Validator` via `jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator()`
- Use `catchException()` + `then()` for exception assertions (matching project convention)
- Test EACH validation constraint individually — one test per constraint

## Pattern

```java
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RegisterUserRequestTest {

    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Nested
    @DisplayName("Bean Validation")
    class BeanValidationTest {

        @Test
        @DisplayName("WHEN login is blank EXPECT ConstraintViolationException")
        void when_loginIsBlank_expect_violation() {
            var request = new RegisterUserRequest("Ivan", null, "Ivanov", "", "ivan@example.com");
            var violations = validator.validate(request);
            then(violations).isNotEmpty();
            // assert specific violation
        }
    }
}
```

## Placement

Place DTO validation tests in the SAME package as the DTO — `src/test/java/.../infrastructure/web/`:

```
src/test/java/.../infrastructure/web/
├── RegisterUserResourceTest.java    ← @WebTest slice
└── RegisterUserRequestTest.java     ← DTO validation unit test
```

## Reference

- Example: search for existing `*RequestTest.java` or `*CommandTest.java` in `src/test/java/`
- REST template (strategy): `.claude/tech/java-spring/templates/rest/test-class.md`
