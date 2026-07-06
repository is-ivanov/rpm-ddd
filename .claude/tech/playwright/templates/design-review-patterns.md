# Design Review Patterns

Reference content for `design-review-agent`: BAD → GOOD examples, the review output format, and verdict examples. The agent runs two checks — **Check A** classifies every string literal as OK (same value for every user, always) or FAIL (user-, time-, or state-specific), and **Check B** confirms every interactive control in the mockup is rendered in the component. This file shows what the fixes look like and how to report them.

## BAD → GOOD Examples

### Email copied from mockup

```tsx
// BAD — mockup placeholder hardcoded into the component
<span data-testid="profile-email">user@example.com</span>

// GOOD — sourced from auth context / API response
<span data-testid="profile-email">{user.email}</span>
```

### Date hardcoded

```tsx
// BAD — fixed date from the mockup
<p data-testid="renews-on">Renews on Feb 15, 2026</p>

// GOOD — formatted from API data
<p data-testid="renews-on">Renews on {formatDate(subscription.renewsAt)}</p>
```

### Price / amount hardcoded

```tsx
// BAD — plan price baked in
<span data-testid="plan-price">$9.99/mo</span>

// GOOD — from plan config / API response
<span data-testid="plan-price">{formatPrice(plan.price)}/mo</span>
```

### Count tied to user state

```tsx
// BAD — count from the mockup
<Badge data-testid="task-count">3 tasks in progress</Badge>

// GOOD — derived from fetched data
<Badge data-testid="task-count">{inProgressTasks.length} tasks in progress</Badge>
```

### OK — static UI text stays inline

```tsx
// OK — same value for every user, every time. Do NOT flag.
<label htmlFor="email">Email</label>
<button data-testid="submit">Save</button>
```

## Check B — Missing Mockup Control (BAD → GOOD)

```tsx
// BAD — the mockup shows a filter input on every text column, but the component
// wires one only on "Full name"; the other columns render as bare headers.
const COLUMNS = [
  { label: 'Full name', filterTestId: 'users-filter-name' },
  { label: 'Login' },   // <-- mockup has a filter here; component omits it
  { label: 'Email' },   // <-- and here
];

// GOOD — every column the mockup gives a control gets one in the component.
const COLUMNS = [
  { label: 'Full name', filterTestId: 'users-filter-name' },
  { label: 'Login', filterTestId: 'users-filter-login' },
  { label: 'Email', filterTestId: 'users-filter-email' },
];
```

The same applies to sortable headers, dropdowns, and date pickers: if the mockup shows the affordance, the component renders it — the test spec covering only one column is NOT a licence to drop the others.

## Output Format

Print TWO tables — Check A (placeholder data) and Check B (control completeness) — then one combined verdict. Quote literals exactly and cite source lines; for Check B, enumerate every mockup control and mark it Present or MISSING.

```
## Design Review: {ComponentName}.tsx

### Check A — Placeholder data
| String literal | Line | Verdict | Category | Should come from |
|----------------|------|---------|----------|------------------|
| `user@example.com` | 42 | FAIL | Email | Auth context |
| `Email` | 18 | OK | UI label | — |
| `Save` | 63 | OK | Button text | — |

### Check B — Mockup control completeness
| Mockup control | Verdict | Notes |
|----------------|---------|-------|
| Filter input on Full name | Present | — |
| Filter input on Login | MISSING | mockup shows it; component omits |
| Sort on Created / Updated | MISSING | no sortable affordance rendered |
| Register user button | Present | — |

Verdict: FAIL — 1 placeholder value + 2 missing controls.
```

## Verdict Examples

- **PASS** — every string literal is OK (Check A) AND every mockup control is rendered (Check B). No user-, time-, or state-specific value is hardcoded and no affordance is missing.

  ```
  Verdict: PASS — no placeholder data, all mockup controls present. Proceed to /refactor.
  ```

- **FAIL** — one or more literals are user-, time-, or state-specific, OR one or more mockup controls are missing. List each violation. The workflow must NOT proceed to `/refactor` until every flagged value is made dynamic, every missing control is rendered (or a recorded scope decision covers it), and the review is re-run.

  ```
  Verdict: FAIL — 1 placeholder value + 2 missing controls. Fix, then re-run /design-review.
  ```
