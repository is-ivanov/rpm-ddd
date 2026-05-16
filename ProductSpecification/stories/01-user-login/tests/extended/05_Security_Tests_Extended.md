> These are additional edge case tests. Implement after core tests pass.

# Security Tests — Extended Edge Cases

## JWT algorithm confusion attack (alg: none) is rejected

```gherkin
Given a forged token with the algorithm field set to "none"
When the forged token is used to access a protected endpoint
Then the request is rejected
And the response returns status 401
```

## Login with special characters in login field does not cause errors

```gherkin
Given the login endpoint is available
When a login request is submitted with special characters in the login field
Then the response returns a normal authentication error
And no internal server error occurs
```

## Concurrent activation attempts with same token — only one succeeds

```gherkin
Given a valid activation token exists for a pending user
When two concurrent activation requests use the same token
Then exactly one request succeeds
And the other request returns an error
```

## Password containing only whitespace is rejected

```gherkin
Given the activation page is available
When a user submits a password consisting only of whitespace characters
Then the response returns a validation error
And the account is not activated
```
