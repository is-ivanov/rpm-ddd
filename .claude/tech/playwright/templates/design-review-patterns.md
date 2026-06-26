# Design Review Patterns

Reference content for `design-review-agent`: BAD → GOOD examples, the review output format, and verdict examples. The agent classifies every string literal in a component as OK (same value for every user, always) or FAIL (user-, time-, or state-specific). This file shows what the fixes look like and how to report them.

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

## Output Format

Print one row per reviewed string literal, grouped by verdict. Quote the literal exactly; cite the source line.

```
## Design Review: {ComponentName}.tsx

| String literal | Line | Verdict | Category | Should come from |
|----------------|------|---------|----------|------------------|
| `user@example.com` | 42 | FAIL | Email | Auth context |
| `Renews on Feb 15, 2026` | 51 | FAIL | Date | API response |
| `Email` | 18 | OK | UI label | — |
| `Save` | 63 | OK | Button text | — |

Verdict: FAIL — 2 placeholder values must be made dynamic.
```

## Verdict Examples

- **PASS** — every string literal is OK (UI labels, headings, button text, ARIA labels, nav paths). No user-, time-, or state-specific value is hardcoded.

  ```
  Verdict: PASS — no hardcoded placeholder data found. Proceed to /refactor.
  ```

- **FAIL** — one or more literals are user-, time-, or state-specific. List each with its category and dynamic source. The workflow must NOT proceed to `/refactor` until the flagged values are made dynamic and the review re-run.

  ```
  Verdict: FAIL — 2 placeholder values must be made dynamic. Fix, then re-run /design-review.
  ```
