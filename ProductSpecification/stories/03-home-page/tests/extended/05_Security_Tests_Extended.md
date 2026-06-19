> These are additional edge case tests. Implement after core tests pass.

# Security Tests (Extended) — Home Page

## 1. XSS in Profile Name

### 1.1 A profile name containing markup is rendered as inert text

```gherkin
Given an authenticated user whose first name is "<script>alert(1)</script>"
When the dashboard is displayed
Then the top bar shows the name as literal text
And no script executes
```

> Defence-in-depth only: the rendering framework auto-escapes text interpolation, and the name is self-scoped (a user sees only their own profile). See `../05_Security_Tests.md` for why no main-suite scenario is warranted.
