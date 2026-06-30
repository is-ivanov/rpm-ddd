# UI Tests — User Management (Admin User Grid & Create User)

> **Implementation Order**: Admin Center navigation → grid display → loading → client-side filter/sort → relative-time tooltip → create modal → submit loading → server response → duplicate field errors → sidebar collapse persistence.

All user data shown in the grid comes from the API response only — never from mockup placeholders.

## 1. Admin Center Navigation

### 1.1 Sidebar shows an Admin Center group with a Users item

```gherkin
Given an authenticated user is on the dashboard
Then the sidebar displays an "Admin Center" group
And the group contains a "Users" item
```

### 1.2 Clicking Users navigates to the Users page inside the shell

```gherkin
Given an authenticated user is on the dashboard
When the user clicks the "Users" sidebar item
Then the Users page is displayed inside the same top bar and sidebar shell
And the page shows a "Register user" button
```

## 2. Users Grid Display

### 2.1 Grid renders all columns and rows from the API

```gherkin
Given the admin user list returns several users
When the user opens the Users page
Then the grid displays columns: Full name, Login, Email, Status, Created, Created by, Updated, Updated by
And each row shows the user's full name, login, and email from the API
And each row shows a status badge (Active, Pending, Locked, or Inactive)
And each audit actor is shown abbreviated as "J. Doe"
And the seed actor is shown as "System"
```

### 2.2 Grid shows a loading state while fetching

```gherkin
Given the admin user list request is in flight
When the user opens the Users page
Then the grid shows a loading state
And the rows render once the response arrives
```

## 3. Client-Side Filter & Sort

### 3.1 Typing in a column filter narrows the rows client-side

```gherkin
Given the Users page shows multiple users
When the user types text into the Full name column filter
Then only rows whose Full name contains that text remain visible
And no additional network request is made
```

### 3.2 Clicking a column header sorts the rows

```gherkin
Given the Users page shows multiple users
When the user clicks the Login column header
Then the rows are sorted ascending by Login
When the user clicks the Login column header again
Then the rows are sorted descending by Login
And the Status column sorts by lifecycle order (Pending, Active, Locked, Inactive), not alphabetically
```

### 3.3 Timestamps show relative time with an absolute-on-hover tooltip

```gherkin
Given the Users page shows a user created in the past
Then the Created column shows a relative time (e.g. "2 weeks ago")
When the user hovers over the relative time
Then a tooltip shows the full absolute time rendered in the viewer's profile timezone
And the tooltip includes the date, time, timezone label, and IANA zone id
```

**Relative-time scale (contract).** The label is computed on a fixed bucket scale against the
current instant, with **floor** rounding and singular at a count of 1:

| Bucket | Range (age) | Label |
|--------|-------------|-------|
| just now | `< 60s` | `just now` |
| minutes | `60s … < 60m` | `N minute ago` / `N minutes ago` |
| hours | `60m … < 24h` | `N hour ago` / `N hours ago` |
| days | `24h … < 7d` | `N day ago` / `N days ago` |
| weeks | `7d … < 30d` | `N week ago` / `N weeks ago` |
| months | `30d … < 365d` | `N month ago` / `N months ago` |
| years | `≥ 365d` | `N year ago` / `N years ago` |

(Approximate divisors: 1 month = 30 days, 1 year = 365 days. The viewer's profile timezone never
affects the *relative* label — only the absolute tooltip — because the elapsed instant is timezone
independent.)

**Tooltip parts (contract).** The absolute tooltip renders the UTC instant in the viewer's profile
timezone (always present and valid — the profile enforces it) under an **en-GB** locale, exposing
four parts: `date` (`yyyy-MM-dd`), `time` (`HH:mm`, 24h), `tzLabel` (short zone abbreviation —
`CEST` in summer, `CET` in winter for Europe/Berlin; zones without a letter abbreviation fall back
to a GMT offset), and `ianaZone` (the IANA id verbatim). The en-GB locale is load-bearing: under
en-US, `Intl` emits `GMT+2` instead of `CEST`. Converting a UTC instant into a far-eastern zone can
roll the calendar date forward (e.g. `22:47Z` → next-day `07:47` in `Asia/Tokyo`).

## 4. Create User Modal

### 4.1 Register user opens a modal with the timezone pre-filled

```gherkin
Given the user is on the Users page
When the user clicks the "Register user" button
Then a modal opens with fields: First name, Middle name, Last name, Login, Email, Timezone
And the Timezone field is pre-filled with the app default (Central Europe)
And the modal shows a "Register" submit button and a "Cancel" button
```

### 4.2 Modal shows a loading state during submission

```gherkin
Given the Register user modal is filled with valid values
When the user clicks "Register"
Then the Register button shows a loading indicator
And the form fields become disabled during submission
```

## 5. Server Response & Validation

### 5.1 Successful create closes the modal and refreshes the grid

```gherkin
Given the Register user modal is filled with valid values
When the user clicks "Register" and the request succeeds
Then the modal closes
And the grid refreshes and shows the newly created user with status Pending
```

### 5.2 Duplicate login or email shows a field-level error

```gherkin
Given the Register user modal is filled with a login that already exists
When the user clicks "Register" and the server returns a duplicate-login error
Then a field-level error message is shown under the Login field
And the modal stays open with the entered values preserved
```

## 6. Sidebar Collapse

### 6.1 Collapse toggle persists across reload

```gherkin
Given an authenticated user is on the dashboard with the sidebar expanded
When the user clicks the sidebar collapse toggle
Then the sidebar collapses
When the user reloads the page
Then the sidebar is still collapsed
```

---

## DSL Technical Reference

| Scenario | Page | Key Elements | Assertions |
|----------|------|-------------|------------|
| 1.1 | Dashboard | sidebar "Admin Center" group, "Users" item | group and sub-item visible |
| 1.2 | Dashboard → Users | "Users" sidebar item, top bar, sidebar, "Register user" button | Users page renders inside the shared shell; "Register user" visible |
| 2.1 | Users | grid header cells, rows, status badge, audit actor cells | 8 columns present; name/login/email from API; status badge text; actor "J. Doe"; seed = "System" |
| 2.2 | Users | grid loading indicator, rows | loading state visible while in flight; rows render after response |
| 3.1 | Users | Full name filter input | rows filter client-side on "contains"; no network request fired |
| 3.2 | Users | Login header, Status header | click toggles asc/desc on Login; Status sorts by lifecycle order Pending→Active→Locked→Inactive |
| 3.3 | Users | Created cell relative label, hover tooltip | relative label shown; hover reveals absolute time in viewer timezone with date/time/tz-label/IANA id |
| 4.1 | Users → modal | "Register user" button, modal fields, Timezone field, "Register"/"Cancel" buttons | modal opens; 6 fields visible; Timezone pre-filled with app default |
| 4.2 | Modal | "Register" button, form fields | on submit: button shows loading indicator, fields disabled during in-flight request |
| 5.1 | Modal → Users | "Register" button, grid | on success: modal closes, grid refreshes, new Pending row visible |
| 5.2 | Modal | Login field, field-level error | duplicate-login error shown under Login; modal stays open; values preserved |
| 6.1 | Dashboard | sidebar collapse toggle | toggle collapses sidebar; state persists in localStorage across reload |
