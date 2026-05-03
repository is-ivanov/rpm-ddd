# Repository Guidelines

## Project Structure & Module Organization
`rpm-ddd` is a Maven-based Spring Boot 4 project targeting Java 26. The intended architecture is a modular monolith with DDD boundaries enforced through Spring Modulith first, with Maven modules extracted later.  Code-quality rules are stored in `code-quality-config/checkstyle/`.

Older experimental folders such as `agency-management/`, `patient-management/`, `full-app/`, `configuration/`, and `tech-shared/` came from earlier modularization attempts. Treat them as historical or temporary unless a task explicitly targets them.

## Build, Test, and Development Commands
Use the Maven wrapper:

- `./mvnw verify -B` runs the full CI build, including tests and Spotless checks.
- `./mvnw test` runs the JUnit test suite only.
- `./mvnw checkstyle:check -B` runs Checkstyle with `code-quality-config/checkstyle/my_checks.xml`.
- `./mvnw pmd:check -B` runs PMD and fails on violations.
- `./mvnw spotless:apply` reformats Java sources with Palantir Java Format.
- `./mvnw spring-boot:run` starts the application locally.

## Comments Policy
**Do not add comments unless explicitly asked.** However, **always preserve existing comments** when editing files — comments are part of the codebase and must not be silently removed during refactoring, rewriting, or any other file modification.

## Coding Style & Naming Conventions
Follow `.editorconfig`: UTF-8, spaces, 4-space indentation, final newline, and a 120-character line limit. Java formatting is enforced by Spotless with Palantir Java Format, so run `./mvnw spotless:apply` before a PR. Keep package names lowercase, classes in `UpperCamelCase`, methods and fields in `lowerCamelCase`, and tests named `*Test` or `*IntegrationTest`. Preserve the package split by responsibility, for example `iam.application`, `iam.domain`, and `iam.web`.

**Always use import statements for types — never use fully-qualified class names in code.** For example, write `Stream<Arguments>` with `import java.util.stream.Stream;` and `import org.junit.jupiter.params.provider.Arguments;`, not `Stream<org.junit.jupiter.params.provider.Arguments>`.

## Testing Guidelines
**Read `TESTING.md` before writing any test.** It defines the test pyramid, conventions, and exact patterns to follow. When writing tests, follow the strategy in `TESTING.md` — do not invent new patterns or deviate from what it describes.

## Commit & Pull Request Guidelines
Recent history uses short imperative subjects such as `Add architecture tests and some shared.web classes`; dependency bumps follow Dependabot’s `Bump ... from ... to ...` pattern. Keep commit titles concise and scoped to one change. For pull requests, include the problem, approach, linked issue if one exists, and verification run (`./mvnw test`, `./mvnw verify`, `./mvnw checkstyle:check`, or `./mvnw pmd:check`). Screenshots are only needed for HTTP or UI-facing changes.

## Continue Framework (TDD/ATDD Workflow)
The project uses the Continue framework (ported from Claude Code) for structured TDD-driven story development. The framework lives in `.opencode/` and provides:

- **`/continue`** — Central dispatcher. Reads `progress.md`, executes next atomic work unit.
- **28 slash commands** — `/story`, `/task`, `/interview`, `/refactor`, `/architecture`, etc. See `.opencode/commands/`.
- **8 subagents** — TDD phase agents (red, green, refactor, coverage, test-review, etc.) dispatched via Task tool.
- **Rules** — Auto-loaded from `.opencode/rules/` via `opencode.json` instructions (workflow, TDD, coding, frontend rules).
- **Templates** — Reference files in `.opencode/templates/` (refactoring patterns, spec formats, TDD templates).
- **Tech profiles** — Pluggable tech stacks in `.opencode/tech/` (java-spring is the active profile).
- **ProductSpecification/** — Stories, tasks, and progress tracking files.

Key concepts: strict Red-Green-Refactor TDD cycles, `progress.md` as single source of truth, one work unit per `/continue` invocation, 3-tier test architecture (Test Class → Statements → Scope).
