---
name: doc
description: Document findings, research, or knowledge into ProductSpecification/ with proper cross-references. Use when the user wants to write up research results, create a reference doc, consolidate scattered knowledge, or mentions /doc command. Also triggers on "document this", "write this up", "let's capture what we learned", "create a reference for".
---

# /doc - Document Findings

Capture knowledge from a session into a well-structured document under `ProductSpecification/`, with bidirectional cross-references to related specs, stories, and tasks.

## Why This Exists

Research, API discoveries, and architectural findings get lost across conversations. This skill ensures knowledge is captured once, in one canonical location, with links from every document that needs it — so future sessions find the information without re-discovering it.

## Usage

```
/doc                              # Interactive — asks what to document
/doc "task ordering strategy"     # Topic hint
```

## Workflow

Three rounds — full procedure in `.claude/templates/documentation/doc-workflow.md`:

1. **Round 1 — What and Where:** summarize the findings; propose location (existing folder, new `{topic-slug}/README.md`, or task folder) and filename; confirm with user.
2. **Round 2 — Cross-References:** identify inbound links (docs that should reference this) and outbound links (sources this references); confirm both lists with the user.
3. **Round 3 — Write and Link:** write the doc (title, summary, tables, links), update referencing docs with relative-path links, update indexes if a new folder was created, then commit `docs: {short description}`.

## Rules

- All documentation in English (project convention)
- Use relative paths for cross-references (`../../tasks/35-bug-.../research.md`)
- Prefer tables for structured data (endpoints, permissions, field mappings)
- Include external URLs where relevant (API docs, third-party references)
- Don't duplicate content that already exists — link to it instead
- Keep each document focused on one topic; create separate docs for separate topics
