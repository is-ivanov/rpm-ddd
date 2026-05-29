# Extract Test Data Builder

When to use: the same domain entity is constructed via raw `Instancio.of(Entity.class)`
in 3+ call sites across test files.

## Before — raw Instancio scattered across tests

```java
// In UserStatements.java
User existingUser = Instancio.of(User.class)
        .set(field(User::getLogin), new Login(login))
        .build();

// In AuthResourceTest.java
User user = Instancio.of(User.class)
        .set(field(User::getLogin), new Login("testuser"))
        .set(field(User::getEmail), new EmailAddress("test@example.com"))
        .create();

// In ActivationStatements.java
User user = Instancio.of(User.class)
        .set(field(User::getLogin), new Login(login))
        .set(field(User::getStatus), status)
        .build();
```

## After — extracted builder in fixtures package

```java
// {subdomain}/fixtures/UserBuilder.java
public class UserBuilder {

    private final InstancioApi<User> builder = Instancio.of(User.class);

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public UserBuilder withLogin(String login) {
        builder.set(field(User::getLogin), new Login(login));
        return this;
    }

    public UserBuilder withEmail(String email) {
        builder.set(field(User::getEmail), new EmailAddress(email));
        return this;
    }

    public UserBuilder withStatus(UserStatus status) {
        builder.set(field(User::getStatus), status);
        return this;
    }

    public User build() {
        return builder.create();
    }
}
```

```java
// Usage
import static by.iivanov.rpm.iam.user.fixtures.UserBuilder.aUser;

User user = aUser().withLogin("admin").withStatus(UserStatus.ACTIVE).build();
```

## Steps

1. **Count call sites** — grep `Instancio.of(Entity.class` across `src/test/`. If 3+ files: proceed.
2. **Survey fields** — for each call site, list which fields are `.set()`. Union all fields → `with*()` methods.
3. **Create builder** — in `{subdomain}.fixtures.{Entity}Builder`. One `with{Field}()` per unique field.
   - Accept `String` for value object fields (builder handles VO construction).
   - Accept the domain type directly for enum fields (`withStatus(UserStatus)`).
4. **Static import** — add `import static ...{Entity}Builder.a{Entity};` to each call site file.
5. **Migrate call sites** — replace each raw Instancio chain with `a{Entity}().with...().build()`.
6. **Run tests** — verify all tests still pass.
7. **Re-scan** — confirm no raw Instancio calls remain for that entity type.

## Naming Rules

- Class: `{Entity}Builder`
- Factory method: `a{Entity}()` / `an{Entity}()` — correct English article by phonetics
- With-methods: `with{FieldName}(param)` — name matches the entity getter
- Terminal: `build()`

## Threshold

Do NOT extract if:
- Entity appears in fewer than 3 test files — the builder adds indirection without value
- The entity is a DTO or simple value object — use Instancio directly
