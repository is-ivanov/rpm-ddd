# Red Phase -- Java/JUnit Conventions

Universal formats and rules: `.claude/templates/workflow/red-phase-formats.md`

## @Disabled Marker Syntax

```java
@Disabled("TDD Red Phase - findByEmail returns Optional.empty()")
```

Class-level (adapter tests):

```java
@Disabled("TDD Red Phase - TaskStorage.findByBoardId not implemented")
class TaskStorageFindByBoardIdTest {
```
