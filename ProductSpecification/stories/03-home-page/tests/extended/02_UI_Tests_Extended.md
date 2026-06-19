> These are additional edge case tests. Implement after core tests pass.

# UI Tests (Extended) — Home Page

## 1. Loading State

### 1.1 A loading indicator is shown while the current user is being resolved

```gherkin
Given the current-user request has not yet resolved
When the user navigates to the home page
Then the page displays a loading spinner
And neither the welcome page nor the dashboard shell is shown yet
```

## 2. Session Expiry

### 2.1 Session expiry while viewing the dashboard returns the user to the welcome page

```gherkin
Given an authenticated user is on the dashboard
When the session expires and the next current-user request is unauthenticated
Then the user is shown the welcome page
```

## 3. User Menu Dismissal

### 3.1 Clicking outside the open user menu closes it

```gherkin
Given the user menu is open on the dashboard
When the user clicks outside the menu
Then the menu closes
```

## 4. Avatar Initials

### 4.1 A single-word name produces a single initial

```gherkin
Given an authenticated user whose name is the single word "Иван"
When the dashboard is displayed
Then the avatar shows the initial "И"
```
