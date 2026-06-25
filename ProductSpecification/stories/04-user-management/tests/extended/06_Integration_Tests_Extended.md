# Integration Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Create with a timezone still triggers the activation email

```gherkin
Scenario: Adding the timezone field does not break the activation email pipeline
  Given an authenticated admin
  When the admin creates a user with timezone "Europe/Berlin"
  Then a UserRegisteredEvent is published
  And an activation email is delivered to the new user's email address
  And the activation link is present in the email
```

## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|--------------------------|
| `the admin creates a user with timezone` | POST /api/admin/users with a valid body including `timeZone` |
| `a UserRegisteredEvent is published` | Event captured via the test event listener / publication registry |
| `an activation email is delivered` | Captured by the test SMTP fake (GreenMail); assert recipient and link present |
