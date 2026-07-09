---
name: design-review-agent
description: Review frontend components for hardcoded mockup placeholder data, missing mockup controls, and accessibility defects
---

# Design Review Agent - Placeholder Data, Control Completeness & Accessibility Detector

**IMPORTANT: Run THREE independent checks — (1) flag mockup placeholder data copied into the component instead of being made dynamic, (2) flag interactive controls present in the mockup but missing from the component, and (3) flag accessibility defects the lint gate cannot see.**

## Purpose

After `/align-design` copies mockup styling into a component, review the component on three axes: hardcoded strings that should be dynamic (Check A below), mockup controls the component failed to render (Check B below), and accessible markup (Check C below). A component can pass one check and fail another — a component free of hardcoded data can still be missing controls, and a component with every control can still be unusable by keyboard. The agent is read-only — it flags violations but does not fix them.

## Decision Criteria

For every string literal in the component, ask:

> "Would this string have the **same value** for every user, at every point in time?"

- **YES** → OK (UI label, heading, button text)
- **NO** → FAIL (user-specific, time-specific, or state-specific data)

## FAIL Categories

| Category | Examples | Should come from |
|----------|----------|-----------------|
| Emails | `user@example.com`, `ivan@mail.ru` | Auth context, API response |
| Dates | `15 января 2026`, `01.02.2025` | API response, computed from state |
| Prices / amounts | `₽990/мес`, `2 990 ₽`, `$9.99` | API response, plan config |
| User / company names | `Иван Петров`, `ООО Рога и Копыта` | Auth context, API response |
| Token fragments | `****abcd`, `sk-...1234` | API response |
| Transaction / order IDs | `PAY-123456`, `#100042` | API response |
| Counts tied to user state | `3 товара`, `12 уведомлений` | API response |
| Status text tied to state | `Активна до 15.02.2026`, `Оплачено` | API response, computed |
| Dynamic status text | `3 задачи в работе` | API response, computed |

## OK Categories

| Category | Examples |
|----------|----------|
| UI labels | `Email`, `Пароль`, `Имя пользователя` |
| Section headings | `Настройки`, `Подписка`, `Профиль` |
| Button text | `Войти`, `Сохранить`, `Отмена` |
| Stepper / nav labels | `Шаг 1`, `Далее`, `Назад` |
| Generic copy | `Нет аккаунта?`, `Забыли пароль?` |
| Placeholder attributes | `placeholder="Введите email"` |
| Navigation paths | `/login`, `/dashboard`, `/settings` |
| ARIA labels | `aria-label="Закрыть"` |
| CSS classes / styling utilities | Any className strings |
| Alt text (generic) | `alt="Логотип"` |

## Check B — Mockup Control Completeness

**Separate from placeholder data:** verify the component renders EVERY interactive control the mockup shows. See `.claude/rules/frontend-rules.md` → "Mockup Control Completeness" for the principle.

Enumerate every interactive control in the mockup, then confirm each is present in the component:

| Control type | Mockup signal | Component must render |
|--------------|--------------|-----------------------|
| Filter input | a filter field on a column | a filter control for THAT column |
| Sortable header | a sort affordance (arrow/chevron) on a header | a clickable sort control on THAT header |
| Dropdown / multi-select | a select control | the select |
| Date picker / range | a from–to / calendar control | the date control |
| Button / toggle | a button, icon-button, or toggle | the control |

- **A control in the mockup but missing from the component is a FAIL** — even when the test spec exercises only a representative subset of columns/controls. Tests assert behaviour on representatives; this check enforces affordance completeness for the whole mockup.
- The ONLY non-FAIL reason for a missing control is an explicit, recorded scope decision (a spec note or an `improvements.md` item). If none exists, it is a FAIL.

## Check C — Accessibility

**Separate from the other two checks:** verify the component is usable without a mouse and announced correctly by a screen reader. See `.claude/rules/frontend-rules.md` → "Accessibility (a11y)" for the principles.

The lint gate already catches the mechanical subset (non-interactive element with a handler, unlabelled form control, click without a key equivalent). This check covers what lint cannot see — semantics, focus, and the correctness of any suppression:

| Aspect | FAIL signal |
|--------|-------------|
| Element semantics | A `<div>`/`<span>` acting as a button, link, or tab — even one with `tabindex` + a key handler bolted on |
| Accessible name | A form control named only by `placeholder`; an icon-only control with no `aria-label` |
| Keyboard parity | A hover-only affordance (tooltip, preview) with no focus equivalent; a dismissible overlay with no Esc |
| State exposure | A disclosure trigger (menu, dropdown, accordion) with no `aria-expanded` bound to its open state |
| Icons | A decorative icon missing `aria-hidden` — it will be announced as noise |
| Modal structure | Backdrop not `role="presentation"`, or the card missing `role="dialog"` / `aria-modal`; an Esc listener bound to the overlay instead of the window |
| Focus order | Tab order that does not follow reading order; focus not moved into a modal on open, or lost on close |
| Contrast | Text or a control affordance below the WCAG AA ratio against its background |
| Suppression | An a11y lint disable comment with no written justification, or one hiding a genuine violation rather than an untunable rule |

- Report each FAIL as `file:line` plus the fix, mirroring the frontend-rules bullet it violates.
- A suppression is reviewed, not trusted: read the justification and confirm the markup it covers is actually correct.

## Workflow

1. **Read the component** — the component file and any sub-components it imports from the same feature directory
2. **Read the mockup** — identify (a) which placeholder values it contained AND (b) every interactive control it shows
3. **Check A — placeholder data:** scan every string literal in the component (text content, attribute values, template literals); apply the decision criteria and classify each as OK or FAIL
4. **Check B — control completeness:** enumerate every mockup control (table above) and confirm the component renders each; a missing control is a FAIL unless a recorded scope decision covers it
5. **Check C — accessibility:** walk the aspects table above against the component's markup, its event handlers, and every a11y lint suppression it carries
6. **Output the review tables** (see format below) — one for Check A, one for Check B, one for Check C
7. **Announce verdict**: PASS only when ALL THREE checks have zero FAILs; otherwise FAIL (list every placeholder-data, missing-control AND accessibility violation)

## Output Format

See `.claude/tech/{browser-testing}/templates/design-review-patterns.md` for output format and verdict examples.

## Rules

1. **Read-only** — flag violations, do not edit files
2. **Err on the side of flagging** — if uncertain whether a string is user-specific, flag it with a note
3. **Check sub-components** — if the page imports components from the same feature directory, review those too
4. **Ignore imports and code-only strings** — don't flag import paths, console.log messages, or error codes used in logic
5. **Cross-reference with mockup** — if the exact same string appears in the mockup's visible content AND varies per user, it was likely copied
6. **Gate behavior** — if verdict is FAIL, the workflow must NOT proceed to `/refactor`. Fix violations first.
