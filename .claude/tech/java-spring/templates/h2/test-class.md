# H2 Storage Test Template

> TESTING.md: Infrastructure tests are ad-hoc — only for complex queries (`@Query`, native SQL, Specifications, JOIN FETCH).
> Simple Spring Data derived queries (`findByXxx`) are framework boilerplate — skip them.

## When to Create H2 Tests

Create H2 adapter tests ONLY when the storage adapter has:
- Custom `@Query` with JPQL — syntax errors are easy to miss
- Native SQL queries — no type safety
- JPA Specifications — complex composition logic
- Queries with JOIN FETCH — risk of N+1 or incorrect joins

**Skip (`[S]`)** when: the repository method is a simple Spring Data derived query (`findByLogin`, `findByActivationToken`, `existsByEmail`).

## Adapter Discovery Filter

When `adapters-discovery` Check 1 produces a potential H2 step, apply this filter:
1. Read the repository method the adapter calls.
2. If it's a simple derived query → mark `[S]` with reason "simple Spring Data derived query".
3. If it's custom `@Query`, native SQL, Specification, or JOIN FETCH → add `red-adapter h2` / `green-adapter h2`.

## Test Class Rules

- Annotate with `@DataJpaTest` + `@DbTest` + `@Execution(SAME_THREAD)`
  (or use `@RepositoryTest` meta-annotation when it becomes available)
- Autowire the Spring Data repository under test
- Use `@DisplayName` with Gherkin-style description
- `@Disabled` in RED phase

## H2-Specific Failure Patterns

| Current Implementation | Expected Test Failure |
|----------------------|----------------------|
| `return Optional.empty();` | `Expecting Optional to contain a value but was empty` |
| `return Collections.emptyList();` | `Expecting actual not to be empty` |

## Naming Convention

- Test class: `{Entity}RepositoryTest.java`
- Test method: `should{ExpectedBehavior}()`

## Reference (read before generating)

- `@DbTest`: `src/test/java/by/iivanov/rpm/testing/DbTest.java`
- `Constants.DB_TEST_TAG`: `src/test/java/by/iivanov/rpm/testing/Constants.java`
- Example entity: search `src/main/java/` for `@Entity` classes
- Example repository: search `src/main/java/` for `@Repository` or `extends JpaRepository`

## Key Paths

- Tests: `src/test/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/`
- Production entities: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/domain/` or `infrastructure/persistence/`
- Production repositories: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/persistence/`
