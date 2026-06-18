# rpm-ddd

A SaaS platform for **Remote Patient Monitoring (RPM)** serving medical agencies, physicians, and
insurance companies. The system supports continuous patient monitoring with automated alerts on
abnormal readings — reducing manual effort and improving quality of care.

Agency staff onboard patients, create monitoring orders, issue IoT blood-pressure devices, plan
home-visit routes, and generate insurance documents. Physicians upload medical orders confirming the
need for remote monitoring. Patient vitals are collected automatically from IoT devices.

## Architecture

The project is a **modular monolith** with DDD boundaries enforced through **Spring Modulith** first,
with Maven modules extracted later. The codebase follows Clean Architecture, splitting each bounded
context by layer:

```
{base}.{bounded-context}.{subdomain}.{layer}
by.iivanov.rpm.iam.user.domain | .application | .infrastructure
```

- `domain` — rich domain model
- `application` — application services orchestrating domain logic, depend only on domain
- `infrastructure` — adapters: `web`, `persistence`, `security`, `events`, `notification`
- `shared` — cross-cutting domain concepts and infrastructure utilities

Module boundaries are enforced by Spring Modulith and ArchUnit. See `AGENTS.md` and
`.claude/rules/coding-rules.md` for the full architectural ruleset.

## Tech Stack

| Concern         | Technology                                              |
|-----------------|---------------------------------------------------------|
| Backend         | Java 25, Spring Boot 4 (single Maven module)            |
| Modularity      | Spring Modulith + ArchUnit                              |
| Persistence     | JPA / Hibernate, PostgreSQL, Liquibase migrations       |
| Mail            | Spring Mail                                             |
| IoT protocol    | MQTT (Spring Integration / Eclipse Paho)                |
| Frontend        | Vue 3 (Composition API, `<script setup>`), TypeScript   |
| Frontend build  | Vite, Vitest, MSW                                       |
| Styling         | Tailwind CSS                                            |
| E2E testing     | Playwright                                              |
| Test reporting  | Allure                                                  |

See `ProductSpecification/technology.md` for the complete technology profile.

## Prerequisites

- JDK 25
- Node.js v22.13.0 / npm 10.9.0 (managed automatically by the `frontend` Maven profile)
- Docker (for PostgreSQL and supporting services)

## Build, Test & Run

Use the Maven wrapper:

```bash
./mvnw verify -B              # full CI build: tests, Spotless, SpotBugs
./mvnw test                   # JUnit test suite only
./mvnw spring-boot:run        # start the application locally
./mvnw spotless:apply         # reformat Java sources (Palantir Java Format)
```

### Code quality

```bash
./mvnw checkstyle:check -B    # Checkstyle (code-quality-config/checkstyle/my_checks.xml)
./mvnw pmd:check -B           # PMD
./mvnw spotbugs:check -B      # SpotBugs + find-sec-bugs (effort=Max, threshold=Medium)
```

### Frontend

```bash
cd frontend
npm run dev                   # Vite dev server
npm test                      # Vitest unit tests
npm run test:e2e              # Playwright E2E (chromium)
npm run lint                  # oxlint + eslint + prettier + type-check
```

Full-stack E2E (real frontend + backend + Postgres + Mailpit) runs nightly — see
`frontend/acceptance/tests/fullstack/README.md` for the local recipe.

## Testing

The project follows strict Red-Green-Refactor TDD with a 4-level test pyramid (e2e acceptance → web
slice → usecase unit → domain unit). **Read `TESTING.md` before writing any test** — it defines the
pyramid, conventions, and exact patterns to follow.

## Development Workflow

Story development uses the **Continue framework** (TDD/ATDD), living under `.opencode/` and `.claude/`.
Key entry points:

- `/continue` — central dispatcher: reads `progress.md`, executes the next atomic work unit
- `progress.md` per story — single source of truth for state
- `ProductSpecification/` — stories, tasks, specs, and progress tracking

See `AGENTS.md` for the full repository guidelines, branch policy, and commit conventions.

> **Branch policy:** never commit directly to `main`. Work on `story/N-slug` or `task/N-slug` branches.
