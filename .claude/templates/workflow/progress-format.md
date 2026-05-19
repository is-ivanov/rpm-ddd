# Progress File Format

## Story

```markdown
# Story N: Story Title — Progress

## Spec
- [x] interview
- [x] story
- [x] mockups
- [x] api-spec
- [x] test-spec

## Backend Scenarios (01_API_Tests.md)

### Scenario 1: Scenario title
- [x] red-acceptance
- [~] design               <- MANDATORY for every scenario needing new implementation
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain (optional: created only when coverage or design-preview identifies need)
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

#### After adapters-discovery resolves (example):
- [S] red-domain (no testable domain logic)
- [S] green-domain
- [x] adapters-discovery (db, rest)
- [ ] red-adapter db
- [ ] green-adapter db
- [ ] red-adapter rest
- [ ] green-adapter rest
- [ ] green-acceptance

## Integration Scenarios (06_Integration_Tests.md)

### Scenario title
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1: Scenario title
- [ ] red-selenium
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-selenium
- [ ] demo

## Security Scenarios (05_Security_Tests.md)

### Scenario title
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Load Scenarios (03_Load_Tests.md)

### Scenario title
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Infrastructure Scenarios (04_Infrastructure_Tests.md)

### Scenario title
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance
```

## Task (bug)

```markdown
# Task N: Title — Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: Bug description
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [~] green-usecase                   <- CURRENT
- [ ] adapters-discovery
- [ ] green-acceptance
```

## Task (refactoring)

```markdown
# Task N: Title — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Step description
- [~] red-adapter db                  <- CURRENT
- [ ] green-adapter db

### Step 2: Step description
- [ ] refactor usecase
- [ ] refactor (cleanup)
- [ ] green-acceptance
```
