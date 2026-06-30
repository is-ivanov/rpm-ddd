# Scenario 4.2 — Password strength indicator

## align-design (2026-06-21)

**Decision:** Scrapped the committed aggregate weak/medium/strong indicator and replaced it with a per-rule contract — each complexity rule renders `complexity-rule-{key}` + a `data-met` attribute.
**Why:** Mockup `04-activation-form.html` (the design source of truth) shows per-rule green highlighting, not an aggregate widget — the mismatch surfaced only at align-design, superseding the committed red/green work.
**Where applied:** `password-strength.logic.ts` (`evaluateComplexityRules`), `ActivationPage.vue`, `activation-strength.spec.ts`.

**Quirk:** One DOM element cannot carry two `data-testid`s, so the redesign deleted the legacy aggregate `data-testid="password-complexity-rule"` that the already-green §4.1 Statements depended on.
**Where:** `activation-page.statements.ts`.
**Implication:** When a redesign changes a testid an earlier green scenario uses, migrate that scenario's Statements onto the new locator — don't double-tag the element.
