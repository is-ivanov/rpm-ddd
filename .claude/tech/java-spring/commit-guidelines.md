# Commit & Pull Request Guidelines for Java Spring Backend

## Commit Format
- Use short imperative subjects such as "Add architecture tests and some shared.web classes"
- Keep commit titles concise and scoped to one change
- Dependency bumps follow Dependabot's pattern: "Bump ... from ... to ..."

## Pull Request Requirements
Every pull request must include:
- **Problem**: Description of what was fixed/implemented
- **Approach**: How the solution was implemented
- **Linked issue**: Reference to any related GitHub issue (if exists)
- **Verification run**: One of the following commands must pass:
  - `./mvnw test` - runs the JUnit test suite only
  - `./mvnw verify` - runs the full CI build, including tests and Spotless checks
  - `./mvnw checkstyle:check -B` - runs Checkstyle with `code-quality-config/checkstyle/my_checks.xml`
  - `./mvnw pmd:check -B` - runs PMD and fails on violations

## Screenshots
Screenshots are only needed for HTTP or UI-facing changes. For backend-only changes (domain logic, services, repositories), screenshots are not required.

## Commit Title Examples
- `Add architecture tests and some shared.web classes`
- `Implement user authentication via JWT tokens`
- `Bump spring-boot from 3.2.0 to 3.2.1`
- `Fix null pointer exception in task service`