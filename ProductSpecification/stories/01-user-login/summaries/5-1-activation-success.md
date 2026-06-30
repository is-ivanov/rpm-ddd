# Scenario 5.1 — Successful activation shows success message

## green-playwright (2026-06-07)

**Quirk:** Playwright `not.toBeEmpty()` asserts on an element's text, so on an SVG-only icon wrapper (no text) it can never pass — a guaranteed-green dud.
**Where:** activation/login icon Statements (`assertSuccessIconIsVisible`, later the shared `assertScreenIconIsVisible` helper).
**Implication:** Assert an icon is rendered via `expect(wrapper.locator('svg > *').first()).toBeAttached()`, not text emptiness.
