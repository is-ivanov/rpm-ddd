# Technology Profile

tech-profile:
  backend: java-spring
  frontend: vue-ts
  css: tailwind
  browser-testing: playwright

## Application

| Concern | Value |
|---------|-------|
| Language | English |

## Backend

| Concern | Technology |
|---------|-----------|
| Language | Java 25 (planned upgrade to 26) |
| Framework | Spring Boot 4 |
| Build tool | Maven (single module; modularity enforced by Spring Modulith + ArchUnit, not Maven modules) |
| DI | Spring (@Service, @RequiredArgsConstructor) |
| Web | Spring Web (controllers, ResponseEntity) |
| Persistence | JPA / Hibernate |
| Database | PostgreSQL (dev & prod) |
| Migrations | Liquibase |
| Mail | Spring Mail |
| IoT protocol | MQTT (via Spring Integration / Eclipse Paho) |

## Frontend

| Concern | Technology |
|---------|-----------|
| Language | TypeScript |
| Framework | Vue 3 (Composition API, <script setup>) |
| Build tool | Vite |
| Test runner | Vitest |
| HTTP mocking | MSW (Mock Service Worker) |

## CSS

| Concern | Technology |
|---------|-----------|
| Framework | Tailwind CSS |
| Icons | lucide-vue-next |

## Browser Testing

| Concern | Technology |
|---------|-----------|
| Framework | Playwright |
| Assertions | Playwright assertions (expect) |

## Testing (Backend)

| Concern | Technology |
|---------|-----------|
| Unit/integration | JUnit 5 |
| Assertions | AssertJ |
| Mocking | Mockito |
| Coverage | JaCoCo |
| Frontend tests | Vitest |

## Infrastructure

| Concern | Technology |
|---------|-----------|
| Containerization | Docker / docker-compose |
| Mail server (dev) | Mailpit (via Testcontainers) |
| MQTT broker (dev) | Mosquitto |

## Conventions

### Backend

| Concern | Convention |
|---------|-----------|
| Test disable marker | @Disabled |
| Not-implemented marker | throw UnsupportedOperationException() |
| Run command | ./mvnw spring-boot:run |
| Test command | ./mvnw test (single class: -Dtest='*ClassName*'; DB/acceptance group: -Dgroups=db) |
| Acceptance test command | ./mvnw verify -B |
| Coverage report | JaCoCo XML in target/site/jacoco/ |
| Health endpoint | /actuator/health |
| Spring config syntax | ${VAR:fallback} |
| Docker config syntax | ${VAR:-fallback} |
| Commit format | Short imperative subjects (e.g., "Add architecture tests") |
| PR requirements | Problem, approach, linked issue, verification commands |
| Verification commands | ./mvnw test, ./mvnw verify, ./mvnw checkstyle:check, ./mvnw pmd:check |
| Screenshots for | HTTP or UI-facing changes only |

### Frontend

| Concern | Convention |
|---------|-----------|
| Test skip marker | .skip |
| Dev command | npm run dev |
| Test command | npx vitest run |
| Node config syntax | process.env.VAR \|\| 'fallback' |

### Browser Testing

| Concern | Convention |
|---------|-----------|
| Acceptance test command | ./mvnw verify -B -Pfrontend |
