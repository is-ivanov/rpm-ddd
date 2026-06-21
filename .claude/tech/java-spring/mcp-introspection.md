# MCP Code Introspection — Tool Mapping (Java/Spring: amplicode + idea)

Two MCP servers expose structured introspection — prefer them over reconstructing facts
from raw text with `Read`/`Grep`. Load schemas via `ToolSearch` (keyword search or
`select:<name>`), then call. See `.claude/rules/mcp-introspection.md` for the universal
principle and the fallback policy.

**Division of labour:**
- **amplicode** — the Spring/JPA *semantic* model: entities, repositories, DTOs, mappers,
  endpoints, beans & the DI graph, security roles, profiles, properties, migrations. Ask
  it *"what does the framework see"*.
- **idea** (JetBrains) — generic IDE intelligence: symbol resolution & docs, code problems
  (inspections), call-flow diagrams, the **live database** (schema + data), safe rename
  refactoring, module/dependency graph. Ask it *"what do the compiler, IDE, and DB see"*.

## amplicode — Spring/JPA semantics

| When you need…                | Prefer (`mcp__amplicode__…`)                                          | Instead of                       |
|-------------------------------|----------------------------------------------------------------------|----------------------------------|
| project overview / modules    | `get_project_summary`, `list_module_dependencies`                    | grep `pom.xml`, dir listing      |
| domain entities & fields      | `list_all_domain_entities`, `get_entity_details`, `get_jdbc_entity_details` | grep `@Entity`             |
| repositories / DTOs / mappers | `list_entity_repositories`, `list_entity_dtos`, `list_entity_mappers` | grep                             |
| REST endpoints                | `list_project_endpoints`, `get_endpoint_info`                        | grep `@GetMapping`/`@RestController` |
| Spring beans / DI graph       | `list_spring_beans`, `get_bean_injection_info`                       | grep `@Service`/`@Component`     |
| security config / roles       | `list_security_configurations`, `list_spring_security_roles`         | grep `SecurityFilterChain`       |
| profiles / datasources        | `list_spring_profiles`, `list_project_datasources`                   | reading config manually          |
| configuration properties      | `list_application_properties_files`, `get_properties_values`         | reading `application.yml` by hand |
| DB migration files            | `list_db_migration_files`                                            | `ls` the migration folder        |
| read a compiled/class file    | `read_class_file`, `analyze_files`                                   | —                                |

## idea — IDE intelligence

| When you need…                              | Prefer (`mcp__idea__…`)                                     | Instead of                          |
|---------------------------------------------|-------------------------------------------------------------|-------------------------------------|
| find a class/method/field by name           | `search_symbol`                                             | grep `class X` / `X(`               |
| a symbol's type / signature / docs / declaration | `get_symbol_info` (file + 1-based line/column)         | reading the file to infer it        |
| does a file compile / its errors + warnings | `get_file_problems` (`errorsOnly` toggles warnings)         | a full `./mvnw` build to surface issues |
| a method's call flow                        | `generate_sequence_diagram` (returns Mermaid)               | manually tracing calls              |
| modules / dependencies / VCS roots          | `get_project_modules`, `get_project_dependencies`, `get_repositories` | grep build files           |
| **live DB** schema (columns / keys / indexes) | `list_database_connections` → `list_schema_objects`, `get_database_object_description` | reading migration files |
| **live DB** rows / ad-hoc query             | `preview_table_data`, `execute_sql_query`                   | guessing the data state             |

## Notes

- **Live DB vs migrations:** amplicode lists migration *files* (static intent); idea queries
  the *running* database (actual schema + data) through its DB connections. Use idea to
  confirm what the DB really contains. DB tools require a configured connection — start from
  `list_database_connections`.
- **Validate without a full build:** prefer `get_file_problems` (IDE inspections) to check a
  single file for errors/warnings instead of running the whole `./mvnw verify`. The real
  build/CI commands (see Conventions table in `ProductSpecification/technology.md`) remain the
  authoritative gate before commit.
- **Safe rename:** to rename a programmatic symbol (class/method/field), prefer
  `mcp__idea__rename_refactoring` — it updates ALL references project-wide — over `Edit` with
  `replace_all`, which is text-only and silently misses references or over-matches. Fall back
  to `Edit` only when the IDE is unavailable.
- **Plain text / regex search stays on `Grep`/`Glob`** (ripgrep-backed — fast, integrated with
  the permission UI). Reach for idea's `search_symbol` / `get_symbol_info` when you need
  *symbol-level* semantics, not literal text.
- **Formatting:** do NOT use `mcp__idea__reformat_file` — this project's canonical format is
  Spotless + Palantir (`./mvnw spotless:apply`); IDE reformat may diverge.
- **Runtime debugging** (breakpoints, stepping, evaluate, stacks) is exposed by both servers
  (`mcp__idea__xdebug_*` and amplicode debug tools) — use the `ij-debugger` skill, not this table.
- **Availability:** every tool needs the IDE open with the plugin active (DB tools also need a
  live connection). If unavailable or a call fails, fall back to `Read`/`Grep`/build commands.
