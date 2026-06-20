> These are additional edge case tests. Implement after core tests pass.
>
> **PROMOTED 2026-06-20 (issue #189):** all four cases below are now first-class Story 1 UI
> scenarios in the core spec `../02_UI_Tests.md` and tracked in `../../progress.md`:
> - "Login page shows loading state during submission" → core **2.2**
> - "Activation page shows password strength indicator updating in real-time" → core **4.2**
> - "Activation page shows error when passwords do not match" → core **4.3**
> - "Error banner dismiss button closes the banner" → core **3.3**
>
> The gherkin below is retained as history; the core spec is now authoritative.

# UI Tests — Extended Edge Cases

## Login page shows loading state during submission

```gherkin
Given the login page is displayed
When the user submits valid credentials
Then the login button shows a loading indicator
And the form fields become disabled during submission
```

## Activation page shows password strength indicator updating in real-time

```gherkin
Given the activation page is displayed
When the user types a weak password
Then the password strength indicator shows weak
When the user updates the password to a strong value
Then the password strength indicator updates to strong in real-time
```

## Activation page shows error when passwords do not match

```gherkin
Given the activation page is displayed
When the user enters different values in the password and confirm password fields
Then an error message is displayed indicating the passwords do not match
```

## Error banner dismiss button closes the banner

```gherkin
Given an error banner is visible on the page
When the user clicks the dismiss button on the banner
Then the error banner is no longer visible
```
