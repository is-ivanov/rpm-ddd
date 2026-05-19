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

### 5. DTO validation tests not in workflow (MODERATE)
**Gap:** TESTING.md Level 2 calls for `Validator` tests for complex DTOs; not in workflow.
**Files to fix:**
- `.claude/tech/java-spring/templates/rest/test-class.md` — add DTO validation test rule
**Story 1 impact:** Scenario 3.1 (activation with password policy) — `ActivateAccountRequest` DTO may be complex.

### 6. @Nested, Instancio, GIVEN/WHEN/THEN conventions (LOW)
**Gap:** Templates don't reference these conventions already used in project tests.
**Files to fix:**
- `.claude/tech/java-spring/tdd.md` — add sections
**Story 1 impact:** Minor — new tests should follow existing conventions.

### 7. Parallel execution rules (LOW)
**Gap:** TESTING.md specifies parallel for unit/web, sequential for e2e/@DataJpaTest.
**Files to fix:**
- `.claude/tech/java-spring/tdd.md`
**Story 1 impact:** None — test execution is already configured via meta-annotations.

### 8. Test API class reuse check in red-agent (MODERATE)
**Gap:** Red-agent doesn't check for existing `@WebApi`/`AbstractApi` classes before creating new ones.
**Files to fix:**
- `.claude/agents/red-agent.md` — add `@WebApi` check to existence check
**Story 1 impact:** Scenarios 2.1-6.1 — `AuthApi` already exists, new endpoints should reuse it.

### 9. Test naming convention alignment (LOW)
**Gap:** TESTING.md says `*Test.java` and `*IntegrationTest.java`. Templates use different suffixes.
**Files to fix:**
- All test-class templates
**Story 1 impact:** Minor — existing tests already follow `*IntegrationTest.java` for e2e and `*Test.java` for others.
