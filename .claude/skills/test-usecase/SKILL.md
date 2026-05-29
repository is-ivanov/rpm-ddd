---
name: test-usecase
description: Run use-case module tests quickly. Use when user wants to run use-case tests or mentions /test-usecase command.
---

# Run Use-Case Tests

## Action

Read `ProductSpecification/technology.md` Conventions table for the backend test command.

This is a single-module Maven project — there is no `usecase` module. Use-case tests live in `*.application` packages and are selected by class-name pattern (`*ServiceTest`, `*CommandTest`, `*RequestTest`); there is no JUnit tag for them.

Without argument — run all usecase tests by pattern:
```
{Backend test command} -Dtest='*ServiceTest,*CommandTest,*RequestTest'
```

With argument (test filter) — run filtered to the single class:
```
{Backend test command} -Dtest='*{argument}*'
```

## Output

Report the test results from output.
