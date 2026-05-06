> These are additional edge case tests. Implement after core tests pass.

# Load Tests — Extended Edge Cases

## Sustained load on /me endpoint (100 requests over 30 seconds)

```gherkin
Given a user is authenticated
When 100 requests are sent to the /me endpoint over 30 seconds
Then all requests return status 200
And the average response time is under 200ms
And no request returns an error
```

## Login with maximum-length password (128 chars) under load

```gherkin
Given a user has a 128-character password
When 50 concurrent login requests are submitted with that password
Then all requests complete successfully
And the average response time is under 500ms
```
