# Acceptance Test Template -- Universal

## Architecture

Three tiers:

| Tier | Purpose |
|------|---------|
| Test Class | Disabled test, scenario in description, extends base test class |
| Test API Classes (`@WebApi`) | Raw HTTP operations, no assertions — extend `AbstractApi`, use `RestTestClient` |
| Session Factory | Login flow orchestration, returns `SessionContext` |

## Test Class Rules

- Multi-step test bodies with setup/action/assertion phases
- Extends the project's integration test base class (tech profile provides exact name)
- **Test API classes must be FULLY FUNCTIONAL in RED** — real HTTP methods, real `RestTestClient` calls
- Gherkin-style scenario in `@DisplayName`
- **NEVER call HTTP client directly** — all HTTP calls go through `@WebApi`-annotated API class methods
- Use `// GIVEN:`, `// WHEN:`, `// THEN:` section comments

## Session-First Pattern

Keep login flow separate from controller APIs:
- `AuthSessionFactory` performs login → returns `SessionContext` (sessionId + csrfToken)
- Pass `SessionContext` explicitly to API methods that require authentication
- This keeps API classes reusable across authenticated and unauthenticated tests

## Test Types

Two test categories with separate base classes:

| Type | Purpose |
|------|---------|
| Backend API | HTTP endpoint tests against the running backend |
| Frontend UI | Browser tests via Playwright/Selenium |

Tech profiles specify exact base class names, annotation, and directory structure.

## Reference Files (read before generating)

Before writing a new acceptance test, read existing examples in the project:

1. **Test example** — an existing integration test class to follow for structure and conventions
2. **Base class** — the abstract base providing test infrastructure (clock, etc.)
3. **Test API example** — an existing `@WebApi` class for HTTP call patterns
4. **Session factory** — the authentication orchestration class
5. **SessionContext** — session data class

Tech profiles provide exact paths for each reference file.

Story mapping: see `ProductSpecification/stories.md`
