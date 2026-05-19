# DB Storage Implementation Template

## Rules

- Replace stub implementation with actual logic using JPA repository
- Map between entity and domain objects using existing conversion methods (`toDomain()` / `from(domain)`)
- Use `@Transactional(propagation = Propagation.MANDATORY)` for write operations
- Database: PostgreSQL via Testcontainers in tests, PostgreSQL in production

## Reference (read before generating)

- Storage implementation: search `src/main/java/` for classes implementing domain storage ports
- Entity example: search `src/main/java/` for `@Entity` classes
- Repository example: search `src/main/java/` for `@Repository` or `extends JpaRepository`

## JPA Repository Query Methods

Spring Data JPA derives queries from method names. If repository doesn't have required method, add it:

| Method Name | Generated Query |
|------------|-----------------|
| `findByEmail(String)` | `WHERE email = ?1` |
| `findByEmailAndPassword(String, String)` | `WHERE email = ?1 AND password = ?2` |
| `existsByEmail(String)` | `SELECT COUNT(*) > 0 WHERE email = ?1` |

## Entity Conversion

- `Entity.toDomain()` — convert entity to domain object
- `Entity.from(domain)` or `new Entity(domain)` — convert domain to entity

## Key Paths

- Storage adapters: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/persistence/`
- Entities: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/domain/` (active persistence) or `infrastructure/persistence/`
- Repositories: `src/main/java/by/iivanov/rpm/{context}/{subdomain}/infrastructure/persistence/`
