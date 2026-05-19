# Domain Implementation Template

## Rules

- Implement domain classes with validation in constructors
- Value objects: validate invariants, throw domain exception on invalid input
- Entities: encapsulate state changes in methods, publish domain events
- No framework annotations except persistence annotations for active persistence pattern
- No nulls — use Optional, empty collections, or Null Object pattern

## Value Object Checklist

- Constructor validates all invariants (non-null, format, range)
- Factory method `of(value)` or `create(value)` for creation
- `equals()` and `hashCode()` for equality
- `toString()` for debugging
- Immutable — all fields final

## Entity Checklist

- Factory method `create(...)` for new entities, `from(...)` for reconstitution
- State change methods named after business action (`activate()`, `lock()`, `deactivate()`)
- Publish domain events via the framework's event mechanism
- References to other aggregates by ID only

## Enum Checklist (when carrying domain logic)

- Behavior methods on the enum (e.g., `isTerminal()`, `authenticationErrorMessage()`)
- Serialization helper `value()` for lowercase string, `from(String)` for parsing
- Use in domain — no type-checking dispatch on enum values
