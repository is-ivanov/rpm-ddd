# Task 246: App-level a11y support — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

> Architectural improvement — **discussion/ADR first**, then implementation.
> The steps below are design+implementation, not a per-layer TDD cycle (a11y is a cross-cutting
> concern enforced by lint config + framework rules, not domain logic).

### Step 1: Architectural discussion & decision (ADR)
- [ ] discuss a11y scope — which checks to enforce (label association, aria validity, keyboard/focus, color-contrast?), lint tool choice, audit-vs-greenfield posture
- [ ] resolve the location open-question — confirm whether a master/template copy of the continue framework exists outside the project; decide project-only vs project+master
- [ ] write ADR `app-level-a11y` (decisions: lint plugin + rule set, framework-rule location, align-design/design-review integration point)

### Step 2: App-level — a11y lint gate
- [ ] add a11y eslint plugin to `npm run lint` (CI gate); configure rule set per the ADR
- [ ] audit existing frontend components; fix violations surfaced by the new rules

### Step 3: Framework-level — encode a11y rules
- [ ] add an a11y section to the frontend rules (correct location per Step 1 decision)
- [ ] add an a11y checklist item to `align-design` + `align-design-checklist` template
- [ ] add an a11y check to the mandatory `/design-review` pass
- [ ] (if master copy exists) propagate the same rule changes to it
