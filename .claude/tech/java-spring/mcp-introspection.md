# MCP Code Introspection — Tool Mapping (Java/Spring: amplicode + idea)

Prefer these amplicode MCP tools over ad-hoc `Read`/`Grep` for structured facts about the
project. Load schemas via `ToolSearch "select:<name>,<name>"` (or a keyword search), then
call. See `.claude/rules/mcp-introspection.md` for the universal principle and the
fallback policy.

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
| DB migrations                 | `list_db_migration_files`                                            | `ls` the migration folder        |
| read a compiled/class file    | `read_class_file`, `analyze_files`                                   | —                                |

## Notes

- **Runtime debugging** (breakpoints, stepping, `evaluate_expression`, stack traces) is a
  separate workflow — use the `ij-debugger` skill, not this table.
- **Test execution:** `run_tests`/`list_test_files` exist, but this project standardizes
  test runs through its test skills / the Conventions table in `ProductSpecification/technology.md`.
  Prefer those for consistency; reach for the MCP runner only when a skill doesn't fit.
- **Availability:** every tool requires the IDE open with the amplicode plugin active.
  If unavailable or a call fails, fall back to `Read`/`Grep` (see the rules file).
