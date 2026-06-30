# Scenario 5.1 (Security) — SQL injection in login field

## red-acceptance (2026-06-13)

**Mistake:** Wrote a Level-1 acceptance test (`LoginSqlInjectionIntegrationTest`: 401 + no JSESSIONID) to prove SQLi protection.
**Why wrong:** Level 1 can't distinguish protected from vulnerable — `x' OR '1'='1` matches no user → 401 like any nonexistent login; and login-field injection can never bypass auth because the password is BCrypt-verified separately after `findByLogin`.
**Correct location/approach:** Prove literal-treatment at the db-adapter level (`@DataJpaTest` `findByLogin("admin' OR '1'='1")` → empty, with a control row); the acceptance test was removed.
