> These are additional edge case tests. Implement after core tests pass.

# API Tests — Extended Edge Cases

## Activate with already-used token returns error

```gherkin
Given a user has already activated their account with a valid token
When the same activation token is used again
Then the response returns an error indicating the token is invalid or expired
```

## Activate account that is already ACTIVE returns error

```gherkin
Given a user with status ACTIVE exists
When an activation request is submitted for that user's token
Then the response returns an error indicating the account is already active
```

## Login with empty login field returns validation error

```gherkin
Given the login endpoint is available
When a login request is submitted with an empty login field
Then the response returns a validation error for the login field
```

## Login with empty password field returns validation error

```gherkin
Given the login endpoint is available
When a login request is submitted with an empty password field
Then the response returns a validation error for the password field
```

## /me after logout returns 401

```gherkin
Given a user is logged in
And the user logs out
When a request is made to the /me endpoint
Then the response returns status 401
```
