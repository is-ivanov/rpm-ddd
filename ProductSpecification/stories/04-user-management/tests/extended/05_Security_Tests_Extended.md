# Security Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Oversized timezone string is rejected

```gherkin
Scenario: An over-length timezone value is rejected
  Given an authenticated admin
  When the admin submits a create-user request with a 1000-character timezone value
  Then the response status is 422
  And the response is an RFC 9457 Problem Detail with a field error for timezone
```

## E2. XSS payload in login and email is escaped in the grid

```gherkin
Scenario: Script payloads in login and email render inert in the grid
  Given a user was created with script payloads in their login and email
  When an admin opens the Users page and the grid renders that row
  Then the login and email cells show the payloads as inert text
  And no script executes in the browser
```

## E3. Filter input is never sent to the server

```gherkin
Scenario: Client-side filtering issues no network request
  Given the Users page has loaded the full user list
  When the user types any value, including injection-like text, into a column filter
  Then no network request is issued
  And filtering happens entirely client-side over the already-fetched rows
```

### DSL Technical Reference

| Scenario | Surface | Key Assertions |
|----------|---------|----------------|
| E1 | POST /api/admin/users | 422 Problem Detail with a `timeZone` field error for an over-length value |
| E2 | GET render → grid | login/email script payloads render inert; no execution |
| E3 | Users page filter | no network request on filter input; pure client-side filtering |
