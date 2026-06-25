# UI Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Status multi-select filter lists statuses in lifecycle order

```gherkin
Given the Users page shows users of varied statuses
When the user opens the Status column filter
Then the status options are listed in lifecycle order: Pending, Active, Locked, Inactive
When the user selects Pending and Locked
Then only rows with those statuses remain visible
```

## E2. Filtering with no matches shows an empty-result state

```gherkin
Given the Users page shows multiple users
When the user types a filter value that matches no rows
Then the grid shows an empty-result message
And clearing the filter restores all rows
```

## E3. Date-range filter on Created narrows by the underlying instant

```gherkin
Given the Users page shows users created across several days
When the user sets a Created from–to date range
Then only rows whose underlying created instant falls within the range remain visible
And the range filter operates on the absolute instant, not the relative label
```

## E4. Cancelling the modal discards input and keeps the grid unchanged

```gherkin
Given the Register user modal is open with partially entered values
When the user clicks "Cancel"
Then the modal closes
And no new row is added to the grid
```

## E5. Collapsed sidebar restores without a flicker on reload

```gherkin
Given the sidebar was collapsed and the page is reloaded
When the Users page loads
Then the sidebar renders collapsed immediately
And it does not briefly flash expanded before collapsing
```

## E6. Mobile layout renders the grid and modal

```gherkin
Given a mobile viewport
When the user opens the Users page
Then the grid is usable on the narrow viewport
And the Register user modal fits the viewport
```

## DSL Technical Reference

| Scenario | Page | Key Elements | Assertions |
|----------|------|-------------|------------|
| E1 | Users | Status filter dropdown | options in lifecycle order; multi-select filters client-side |
| E2 | Users | filter input, empty-result message | no-match shows empty state; clearing restores rows |
| E3 | Users | Created from/to date inputs | filter on absolute instant within range |
| E4 | Modal | "Cancel" button, grid | modal closes; grid unchanged |
| E5 | Dashboard | sidebar | collapsed state renders immediately, no expand flicker |
| E6 | Users (mobile) | grid, modal | usable on mobile viewport |
