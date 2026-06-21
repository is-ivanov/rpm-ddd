# Structured Code Introspection (IDE / MCP)

When you need **structured facts** about the codebase — project/module structure, domain
entities and their fields, REST endpoints, Spring beans and the DI graph, security
configuration, configuration properties, DB migrations, the **live database** schema and
data, a symbol's type/usages, or a method's call flow — prefer the IDE introspection tools
exposed over MCP instead of reconstructing the same facts from raw text with `Read`/`Grep`.
Introspection returns typed, complete results; text search returns fragments you must
reassemble and can miss generated or framework-derived structure.

This project exposes such tools via the **amplicode** (and JetBrains **idea**) MCP
servers. They are *deferred* tools — discover and load their schemas with
`ToolSearch` (e.g. `ToolSearch "amplicode entities"`, `ToolSearch "amplicode endpoints"`),
then call. The concrete situation→tool mapping lives in
`.claude/tech/{backend}/mcp-introspection.md` — consult it the first time you reach for
project structure in a session.

The same IDE MCP also exposes two adjacent capabilities worth preferring: **validating a
file** (compiler/inspection errors+warnings without a full build) and **safe project-wide
symbol rename** (over a text-only `Edit replace_all`). Both are in the binding above.

Keep using `Read`/`Grep` for: non-Java files, plain text/regex search, reading one
already-known file, exact test contents, and anything the introspection tools do not
model.

**Fallback (mandatory):** the IDE MCP is available only while the IDE is open with the
plugin active. In headless/cron runs, or if a `ToolSearch`/MCP call fails, fall back to
`Read`/`Grep` — never block on the MCP being present.
