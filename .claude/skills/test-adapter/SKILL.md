---
name: test-adapter
description: Run adapter tests. First argument is adapter name (rest | db | email), which maps to a test class-name pattern or JUnit tag. Use when user wants to run adapter tests or mentions /test-adapter command.
---

# /test-adapter - Run Adapter Tests

## Usage
```
/test-adapter rest
/test-adapter db LoginStorageTest
/test-adapter email
/test-adapter db TaskStorageTest
```

## Convention

Read `ProductSpecification/technology.md` Conventions table for the backend test command.

This is a single-module Maven project — there are no adapter module directories. The adapter name maps to a test selector instead:

| Adapter | Selector | Notes |
|---------|----------|-------|
| `rest`  | `-Dtest='*ResourceTest,*ControllerAdviceTest'` | Web-slice tests (`@WebTest`) in `*.infrastructure.web` |
| `db`    | `-Dgroups=db` | DB adapter tests carry the JUnit `db` tag (`@DbTest`) |
| `email` | `-Dtest='*EmailClientTest,*NotificationAdapterTest'` | Email adapter tests in `*.infrastructure.notification` |

## Action

Parse arguments: first word is adapter name, optional second word is test filter.

Without test filter — run the adapter's selector from the table above. For example, `rest`:
```
{Backend test command} -Dtest='*ResourceTest,*ControllerAdviceTest'
```
For `db`, use the tag instead:
```
{Backend test command} -Dgroups=db
```

With test filter — narrow to the single class regardless of adapter:
```
{Backend test command} -Dtest='*{filter}*'
```

## Output

Report the test results from output.
