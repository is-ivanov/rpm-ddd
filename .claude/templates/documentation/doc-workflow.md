# /doc Workflow — Detailed Procedure

The `/doc` skill captures knowledge into `ProductSpecification/` in three rounds.

## Round 1: What and Where

**Gather from user:**

1. **What are we documenting?** Summarize the findings in 1-2 sentences. If the current conversation already contains research/findings, extract the summary automatically and confirm with the user.

2. **Where should it live?** Propose a location based on the topic:
   - Existing folder (e.g., `api-reference/`) → add a new `.md` file or append to existing
   - New folder under `ProductSpecification/` → create `{topic-slug}/README.md`
   - Inside a task folder (e.g., `tasks/35-bug-.../research.md`) → already scoped

   **Decision aid:** If the findings are specific to one task/story, keep them in that folder. If they're reusable across multiple stories or tasks, create a shared location.

3. **Filename:** Propose based on content (e.g., `research.md`, `README.md`, `{topic}.md`). Confirm with user.

## Round 2: Cross-References

**Identify automatically, confirm with user:**

1. **What should reference this doc?** (inbound links)
   - Search for related story `endpoints.md` files, task `spec.md` files, and `CLAUDE.md` that would benefit from a link to this document.
   - Use `Grep` to find files mentioning the key concepts being documented.
   - Propose specific files and the line/section where the link should be added.

2. **What should this doc reference?** (outbound links)
   - Scan the conversation for source materials: story interviews, task specs, external URLs, code files.
   - Search `ProductSpecification/` for related existing docs.
   - List proposed references for user confirmation.

Present both lists to the user. They can add/remove entries.

## Round 3: Write and Link

1. **Write the document** with this structure:
   - Title and summary
   - Findings organized in tables and sections (prefer tables for structured data)
   - Links to source materials (outbound references from Round 2)
   - External URLs where applicable

2. **Update referencing docs** — add links in the files identified in Round 2 (inbound references). Use relative paths. Add links in contextually appropriate sections — don't just append to the end.

3. **Update indexes:**
   - If a new folder was created → update `ProductSpecification/CLAUDE.md` structure tree
   - If the doc is in a task folder → no index update needed

4. **Commit** all changes with message: `docs: {short description}`
