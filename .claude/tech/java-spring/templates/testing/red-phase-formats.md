# Red Phase -- Java/JUnit Conventions

Universal formats and rules: `.claude/templates/workflow/red-phase-formats.md`

## `@ExpectedToFail` Marker Syntax

junit-pioneer's `@ExpectedToFail` replaces `@Disabled` as the RED-phase marker. The test **runs on every build**: it aborts (reported skipped) while it fails with the predicted exception, and FAILS the build the moment it passes — forcing marker removal at GREEN. **`withExceptions` is mandatory** — it pins the predicted exception type so an infrastructure error (connection refused, etc.) is a real failure, not a silent abort. Full mechanics: `.claude/tech/java-spring/tdd.md` → "RED-Phase Marker".

Import (never fully-qualified):

```java
import org.junitpioneer.jupiter.ExpectedToFail;
```

`value` is the reason text (like `@Disabled("...")`); `withExceptions` pins the prediction:

```java
@Test
@ExpectedToFail(value = "TDD Red Phase - findByEmail returns Optional.empty()", withExceptions = AssertionError.class)
void when_emailExists_expect_userReturned() { ... }
```

**Method-only — no class-level marker.** `@ExpectedToFail` is `@Target({METHOD, ANNOTATION_TYPE})`; it cannot go on a test class the way `@Disabled` could. RED adapter tests keep ONE `@Test` per class, so place the marker on that single method:

```java
@Test
@ExpectedToFail(value = "TDD Red Phase - TaskStorage.findByBoardId not implemented", withExceptions = UnsupportedOperationException.class)
void when_boardHasTasks_expect_tasksReturned() { ... }
```

`withExceptions` accepts one type or a list: `withExceptions = AssertionError.class` or `withExceptions = {AssertionError.class, ValidationException.class}`.

### Picking `withExceptions`

Pin the type the prediction says the test fails with:

| RED failure cause | `withExceptions` |
|-------------------|------------------|
| Assertion on a missing/wrong value (stub returns null/empty/wrong) | `AssertionError.class` |
| Stub throws the not-implemented marker (`throw new UnsupportedOperationException()`) | `UnsupportedOperationException.class` |
| Predicted domain exception (e.g. validation) | that exception, e.g. `ValidationException.class` |
