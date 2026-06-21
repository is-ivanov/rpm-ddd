# Design-Preview Formats

Reference formats for the `/design-preview` skill: option-presentation depth, the two `AskUserQuestion` prompts (option choice + ADR decision), and the completion-outcome table. The skill keeps the decision flow inline and points here for these format details.

## Option Presentation Depth

Show each option as a labelled block: title, summary, pros, cons. Include the recommendation rationale below the recommended option.

- **Simple scenarios**: each option in 3-5 lines.
- **Complex scenarios**: each option with method signatures/pseudocode, domain model changes, pipeline/sequence diagrams, port changes.

## AskUserQuestion — Option Choice

- First answer = the recommended option, label suffixed with "(Recommended)"
- Other answers = alternative options (one per non-recommended option, up to 3 alternatives)
- Last answer = "Reject all — escalate to `/architecture`"

Option labels must fit in a chip (12 chars). Keep answer descriptions to one short line.

If user rejects all → invoke `/architecture` and STOP. Do not proceed to the ADR question.

## AskUserQuestion — ADR Decision

After an option is chosen, ask separately whether to capture the decision as an ADR:

- "Write ADR" — recommended when the user picked a non-recommended option, when the trade-offs are non-obvious, or when downstream scenarios will likely revisit the choice
- "Skip ADR" — recommended for trivial scenarios (single-option flow) or mechanical choices with no real trade-off

Pick which option to mark "(Recommended)" based on the choice the user just made in the option-choice step.

## Completion Outcome Table

| Outcome | Action |
|---------|--------|
| Option chosen, no ADR | Mark `design` step as `[x]`. No files created. |
| Option chosen, write ADR | Mark `design` step as `[x]`, then write an ADR using `.claude/templates/spec/adr-format.md` to the story's `decisions/` subfolder. Pre-populate the ADR's "Rejected" table with the un-chosen options from the present-options step. Commit includes the ADR file. |
| Rejected all → `/architecture` | Do NOT mark `design`. `/architecture` runs and decides the ADR. Mark `design` only after the ADR lands. |
