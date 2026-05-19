# Alignment Plan: TESTING.md → Continue Framework

## Completed Analysis

Compared TESTING.md (test strategy) with continue framework rules, agents, templates, and skills.
Verified against actual project test code in `src/test/java/by/iivanov/rpm/`.
Verified against current story progress (`ProductSpecification/stories/01-user-login/progress.md`).

## Items

### 1. REST adapter template — wrong test pattern ✅ DONE
**Status:** Fixed. Templates now reference `@WebTest`, `RestTestClient`, `AbstractApi`, `AssertionResponse`, `@WebApi`.
**Gap:** Templates use `@AutoConfigureMockMvc` + `@MockBean` but project uses `@WebTest` + `RestTestClient` + `AbstractApi`.
**Files to fix:**
- `.claude/tech/java-spring/templates/rest/test-class.md`
- `.claude/templates/tdd/red-rest.md`
- `.claude/templates/tdd/green-rest.md`
**Story 1 impact:** Scenarios 2.1-6.1 will need `red-adapter rest` steps. Templates must reference `@WebTest`, `AbstractApi`, `AssertionResponse`, `@WebApi`.

### 2. Reference paths in ALL templates point to non-existent classes ✅ DONE
**Status:** Fixed. All templates now reference actual project classes: `AbstractApplicationIntegrationTest`, `@WebTest`, `@DbTest`, `AuthSessionFactory`, `SessionContext`, `InMemoryUserRepository`, `RpmSoftAssertions`.
**Files changed:**
- `.claude/tech/java-spring/templates/acceptance/test-class.md` — full rewrite
- `.claude/tech/java-spring/templates/usecase/test-class.md` — full rewrite
- `.claude/tech/java-spring/templates/db/test-class.md` — full rewrite (renamed from h2) (also added complexity filter for point 4)
- `.claude/templates/tdd/red-acceptance.md` — updated architecture description

### 3. Domain tests (Level 4) not in workflow ✅ DONE
**Status:** Fixed. Created domain template, added `domain` as optional layer in red/green agents, added step 4a in workflow.md. Domain tests default to `[S]` — activated only when coverage-agent or design-preview identifies testable domain logic.
**Files changed:**
- `.claude/tech/java-spring/templates/domain/test-class.md` — NEW
- `.claude/tech/java-spring/templates/domain/implementation.md` — NEW
- `.claude/agents/red-agent.md` — added `domain` layer + Domain Layer rules section
- `.claude/agents/green-agent.md` — added `domain` layer
- `.claude/rules/workflow.md` — added step 4a (optional domain), updated bootstrapping
- `.claude/skills/continue/SKILL.md` — added domain to dispatch table
- `.claude/templates/workflow/progress-format.md` — added domain to all backend scenario sections

### 4. H2 adapter tests for simple CRUD ✅ DONE
**Status:** Fixed. DB complexity filter added to adapter-discovery-checklist.md Check 1 step 4. Simple derived queries (`findByXxx`) → `[S]`. Only custom `@Query`, native SQL, Specifications, JOIN FETCH trigger adapter steps.
**Files changed:**
- `.claude/templates/workflow/adapter-discovery-checklist.md` — added step 4 (DB complexity filter)
- `.claude/tech/java-spring/templates/db/test-class.md` — already contains the filter (done in H2→db rename)

### 5. DTO validation tests not in workflow ✅ DONE
**Status:** Fixed. Created `dto-validation-test.md` template for Bean Validation unit tests (Validator API). Linked from REST test-class.md. DTO validation tests are created during `red-adapter rest` for complex DTOs — not a separate workflow step.
**Files changed:**
- `.claude/tech/java-spring/templates/rest/dto-validation-test.md` — NEW
- `.claude/tech/java-spring/templates/rest/test-class.md` — added reference to dto-validation-test.md

### 6. @Nested, Instancio, GIVEN/WHEN/THEN conventions ✅ DONE
**Status:** Fixed. Added Test Structure Conventions section to java-spring/tdd.md covering @Nested, GIVEN/WHEN/THEN, naming conventions, sut naming. Added Instancio section. Fixed AssertJ section to use BDD style (catchException/catchThrowable/then) instead of assertThat/assertThatThrownBy. Fixed REST adapter test pattern (@WebTest not @SpringBootTest + MockMvc).
**Files changed:**
- `.claude/tech/java-spring/tdd.md` — three sections updated/added

### 7. Parallel execution rules ✅ DONE
**Status:** Fixed. Added Parallel Execution section to java-spring/tdd.md — parallel for unit/web, sequential for e2e (@ApplicationIntegrationTest) and @DataJpaTest (@RepositoryTest). Meta-annotations enforce this, no manual @Execution needed.
**Files changed:**
- `.claude/tech/java-spring/tdd.md` — added Parallel Execution section

### 8. Test API class reuse check in red-agent ✅ DONE
**Status:** Fixed. Added @WebApi/AbstractApi check to existence check (step 3) and reuse checks (item 3). Red-agent now searches fixtures/ for existing API classes before creating new ones.
**Files changed:**
- `.claude/agents/red-agent.md` — step 3 existence check + reuse check item 3

### 9. Test naming convention alignment ✅ DONE
**Status:** Already aligned. Templates updated in points 1-2 use `*Test.java` and `*IntegrationTest.java`. Naming section added to java-spring/tdd.md in point 6.
