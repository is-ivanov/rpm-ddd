# Task 223: DB baseline cleanup as a JUnit extension -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Create the cleanup extension (JdbcClient, BeforeEachCallback)
- [x] refactor (new extension resolving DataSource from ApplicationContext, JdbcClient delete)

### Step 2: Wire into @ApplicationIntegrationTest, slim the base
- [x] refactor (add @ExtendWith; remove the second @BeforeEach + JdbcTemplate field)
- [x] green-acceptance (full suite green; SonarLint S8745 clear)
