# Java/Spring Coding Idioms

Tech binding for `coding-rules.md`. Shared section structure: `.claude/templates/coding/coding-sections.md`.

## Deployment

- In-memory state to avoid: `ConcurrentHashMap`, `static` fields, local caches.

## Clean Architecture

- `domain`: only Lombok. No Spring annotations.
- `usecase`: only `@Service` and `@Transactional`.
- `adapters`: REST controllers, Kafka listeners/publishers, JPA repositories, external API clients.

## Domain-Driven Design

- Validation: throws `ValidationException`.
- Enum: `value()` for lowercase, `from(String)` for parsing.
- Optional: `Optional<T>`, collections: `List<T>`, boundary: `null` ↔ `Optional`.
- Forbidden dispatch: `instanceof`, `if/instanceof`. Adapters dispatch from JPA entities.
- Typed list: `List<ArchivedTask>` not `List<BaseInterface>` + `instanceof`.
- Parameter object: `record`.
- Immutable: Java `record`, owns `toX()` transitions.

## Code Generation

- `@Data` for DTOs, `@Builder` for construction, `@RequiredArgsConstructor` for DI, `@Value` for immutable.

## Naming

- JPA entities: `{Name}Entity`. Converters: `toDto()` / `toEntity()` / `toDomain()`.

## Immutability

- `final` fields, defensive copies. Repeated `X.builder()...build()` → factory method on X.

## Accessor Chains

- `a.getB().getC().toString()` → convenience method `a.cValue()`.

## Optional API

- `ifPresentOrElse` / `map` / `filter` — never `isPresent()` + `get()`.

## Null Boundary

- Controllers: `Optional.ofNullable()` for `@RequestParam`.
- Request DTOs: `Optional` with `@Builder.Default Optional.empty()`.

## Request DTO Conversions

- Examples: `LocalDate` → `Instant`, `String` → enum.
- Methods: `fromInstant(Clock)`, `parsedActionType()`.

## Branching

- `switch` expressions (arrow `->`) for enums/sealed types. Pattern matching for sealed subtypes.

## Controllers

- `toUsecaseRequest()` on request DTO.
- `ResponseEntity`: `ok()` → 200, `status(CREATED).body()` → 201, `noContent().build()` → 204.
- Errors via `GlobalExceptionHandler`.

## Storage Adapters

- JPA entities ≠ domain. `from(domain)` + `toDomain()`. Never expose outside adapter.
- No `Collectors.groupingBy()`, `Map.Entry`, or Row DTOs — use JPA relationships.
- Trivial: `repository.findAll().stream().map(Entity::toDomain).toList()`.
- Query objects: `class` with `protected` fields for `Specification`/`CriteriaQuery`.

## Refactor Agent — Java Terms

| Generic term (in agent) | Java equivalent |
|--------------------------|-----------------|
| Qualified enum references in logic | `static import` enum values |
| Type-checking/type dispatch in domain or usecase | `instanceof` and `if/instanceof` patterns |
| Base-type list re-partitioned with type checks | `List<BaseInterface>` + `instanceof` |
| Immutable data class | Java `record` |
| Collection pipeline terminal operation | `.collect(toList())` / `.toList()` |
| Manual per-field assertion for immutable data types | Applies to records and value objects |

## Scan Checklist — JPA Grep Patterns

| # | Grep pattern / indicator |
|---|--------------------------|
| A33 | `Collectors.groupingBy`, `Map.Entry`, `Map<..., List<...>>`, Row/Projection DTOs |
| A34 | Count `JpaRepository` / `EntityManager` fields per storage class |
| A42 | Static methods returning `Specification` or `CriteriaQuery` |
| A43 | `CriteriaQuery<Object[]>`, `multiselect(`, `result[0]`, `((Number) result[N])` |
| A44 | `Specification`/`PageRequest`/`CriteriaQuery` inline >5 lines |

## HTTP Clients

- Production: framework HTTP client. Tests: mock at adapter boundary, not transport.

## Error Handling

- Domain exceptions extend `RuntimeException`. Bubble to `GlobalExceptionHandler`.
- Library: `io.github.wimdeblauwe:error-handling-spring-boot-starter`. Configured in `application.yml` under `error.handling.*`.

## MCP Tool Preferences

When performing project-specific operations, prefer MCP tools over generic file search.
Amplicode for Spring artifacts, IntelliJ IDEA for file/editor operations.

### Amplicode (Spring Artifacts)

| Operation | Preferred Tool |
|-----------|---------------|
| Find Spring beans | `mcp__amplicode__list_spring_beans` |
| List endpoints | `mcp__amplicode__list_project_endpoints` |
| Get entity details | `mcp__amplicode__get_entity_details` |
| Bean injection graph | `mcp__amplicode__get_bean_injection_info` |
| Module dependencies | `mcp__amplicode__list_module_dependencies` |
| Find repositories | `mcp__amplicode__list_entity_repositories` |
| List datasources | `mcp__amplicode__list_project_datasources` |
| List domain entities | `mcp__amplicode__list_all_domain_entities` |
| List DTOs | `mcp__amplicode__list_entity_dtos` |
| List mappers | `mcp__amplicode__list_entity_mappers` |
| List security configs | `mcp__amplicode__list_security_configurations` |
| List security roles | `mcp__amplicode__list_spring_security_roles` |
| List Kafka consumers | `mcp__amplicode__list_kafka_consumers` |
| List Kafka producers | `mcp__amplicode__list_kafka_producers` |
| Run tests | `mcp__amplicode__run_tests` |
| Project summary | `mcp__amplicode__get_project_summary` |
| Rebuild project | `mcp__amplicode__rebuild_project` |
| Analyze files | `mcp__amplicode__analyze_files` |
| Read class file (bytecode) | `mcp__amplicode__read_class_file` |

### IntelliJ IDEA (Files & Editor)

| Operation | Preferred Tool |
|-----------|---------------|
| Search files by name | `mcp__idea__search_file` |
| Search classes/methods/symbols | `mcp__idea__search_symbol` |
| Read file content | `mcp__idea__get_file_text_by_path` |
| Edit file in IDE | `mcp__idea__replace_text_in_file` |
| Open file in editor | `mcp__idea__open_file_in_editor` |
| Create new file | `mcp__idea__create_new_file` |
| Build project | `mcp__idea__build_project` |
| Get file problems/warnings | `mcp__idea__get_file_problems` |
| Reformat file | `mcp__idea__reformat_file` |
| Execute terminal command | `mcp__idea__execute_terminal_command` |
| Rename refactoring | `mcp__idea__rename_refactoring` |
| Get project dependencies | `mcp__idea__get_project_dependencies` |
| Get project modules | `mcp__idea__get_project_modules` |

### Usage Rules

- Use Amplicode for Spring-specific queries (beans, endpoints, entities, repositories)
  instead of `Grep` with patterns like `@Service`, `@RestController`, `@Entity`
- Use IntelliJ IDEA for symbol search instead of `Grep` when looking for
  class/method definitions
- Fall back to `Grep`/`Glob`/`Read` only when the MCP tool returns no results or errors
