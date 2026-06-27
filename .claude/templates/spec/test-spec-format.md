# Test Spec — Category Formats & Ordering

## 01_API_Tests.md (5-8 tests)

Tests are ordered for **sequential TDD implementation** — each section builds on the previous one. You can implement group N without needing group N+1.

Start every generated file with this header:
```markdown
> **Implementation Order**: Tests are numbered for sequential TDD implementation.
> Start with [story-specific progression summary].
```

**Test Pyramid Alignment (CRITICAL):**

Follow `TESTING.md`. Acceptance tests (Level 1, e2e) cover **happy path only**. Do NOT generate a separate acceptance scenario for every business variant that shares the same HTTP code path.

| Rule | Example |
|------|---------|
| One happy path per endpoint | POST /login → 200 with session |
| One representative error per endpoint (if the endpoint can fail) | POST /login → 401 (wrong password) |
| Multiple business variants of the SAME error → merge into ONE scenario, test variants at lower levels (domain/usecase) | PENDING/LOCKED/INACTIVE all return 401 with different messages → ONE acceptance test "Login with non-ACTIVE user returns 401", individual messages tested at domain level |
| Validation errors with different field combinations → ONE scenario per endpoint, test individual constraint violations at DTO/web level | Password too short + missing uppercase + missing digit → ONE "Password policy violation" scenario |
| Multiple observable consequences of the SAME action → ONE scenario: one When + multiple Then (response + side effects). Extend the existing acceptance test rather than adding a parallel one | POST register → 201 AND activation email delivered → ONE scenario asserting both, extending the existing registration acceptance test |

**Acceptance ≠ exhaustive.** If 5 different domain states produce the same HTTP response (same status, same error structure, different message), that is ONE acceptance scenario — not 5. The per-state message variations belong in domain unit tests (Level 4).

**Per-scenario Level tag + overlap pass (MANDATORY — applies to every backend-style scenario: API, Security, Integration, Infrastructure).**

The single most common spec defect is leaving a scenario's **test level implicit**, so the later bootstrap step defaults it to a Level-1 acceptance test even when a cheaper level is correct. Prevent it at authoring time: before writing each `### N.M` scenario, run an **overlap pass** and record the chosen level as the first line of the scenario block.

```markdown
### 2.1 Create with a duplicate login returns a field-level 422
**Level:** L2 web-slice  <!-- L1 acceptance | L2 web-slice | L3 usecase | L4 domain | db-adapter -->
```

Levels follow the pyramid in `tdd-rules.md` ("Test Pyramid Alignment") — classify against it, do not restate it:
- **L1 acceptance** — happy path only (1-2 per use case); full-context black-box. Also the level for full-context **resilience/outage** scenarios (DB unavailable → 500) — they need the real app + broken infra.
- **L2 web-slice** — validation errors and **business-exception → HTTP-status / Problem-Detail mapping** (duplicate-key 4xx, invalid-field 422, mass-assignment binding, CSRF 403). **This is the default for error / validation / per-status categories.**
- **L3 usecase** / **L4 domain** — business corner cases, value-object / entity / policy rules.
- **db-adapter** (`@DataJpaTest`) — persistence-query behavior only: complex `@Query` / native SQL, and **SQL-injection literal-treatment** (prove via `findByX` returning empty + a control row — asserting a 4xx at L1 proves nothing). See `tdd-rules.md` → "Infra (ad-hoc)".

**Overlap pass — pick the cheapest level that recovers the NEW behavior:**
1. Ask: *is the underlying domain/business rule already covered by a lower level or an existing scenario* (this story or an earlier one)? If yes, the only new behavior is usually the HTTP mapping → tag **L2 web-slice**, never L1. (Example: the duplicate-login rule is already tested at L3/L4, so the 422 scenario is L2 — only the exception→status mapping is new.)
2. If the action already has an L1 acceptance scenario and this scenario only adds another observable consequence of the SAME action → **extend that scenario**, don't add a new one (see the table above).
3. Reserve L1 for the happy path. An error category gets its own L1 scenario only when that error path itself is the new behavior AND it is the one representative error per endpoint — otherwise it is L2.

Diagnostic: *"If I deleted this scenario, what coverage is actually lost — and what is the cheapest test that recovers it?"* Tag that level.

**Ordering principles (apply in this priority):**
1. **Prerequisite guards first** — generate guard scenarios for every prerequisite listed in the story spec. See Prerequisite Guard Checklist below. Do NOT include generic auth (401 for unauthenticated) tests — those are tested globally by the security filter, not per-story.
2. **Read operations before writes** — GET before POST/PUT/DELETE. Less infrastructure needed. **Exception:** if a GET scenario's Given clause requires a write operation (e.g., "Given a task exists"), move that scenario after the write scenarios that create the precondition. A scenario must never depend on capabilities not yet implemented at its position in the TDD sequence.
3. **Validation before happy path** — within a write group, reject bad input first (needs only validation), then test success (needs full pipeline).
4. **Simple states before complex states** — default/initial state before edge cases (e.g., empty board before board with tasks).
5. **Verification/confirmation flows last** — depend on prior operations succeeding (e.g., email verification after registration, webhook after checkout).

**Structure:** Use `## N. Section Title` for groups, `### N.M Scenario Title` for individual tests — each scenario block opens with its `**Level:**` tag (see "Per-scenario Level tag + overlap pass" above). Separate sections with `---`.

**Typical section progression** (adapt to story — skip irrelevant sections, add story-specific ones):
```
## 1. Security Foundation
## 2. Read Current State (GET)
## 3. Create/Submit (POST) — Validation
## 4. Create/Submit (POST) — Happy Path
## 5. Verification/Confirmation/Callback
## 6. Additional Operations (PUT/DELETE)
```

Reference: `ProductSpecification/stories/01-create-task/tests/01_API_Tests.md`

### Prerequisite Guard Checklist

Stories declare prerequisites (e.g., "board must exist", "column must exist"). Each prerequisite MUST produce guard scenarios in BOTH `01_API_Tests.md` and `02_UI_Tests.md`.

**How to extract prerequisites:** Read the story's Prerequisites section and Validation Rules table. Each entry that gates the feature is a prerequisite.

| Prerequisite | API test scenarios | UI test scenarios |
|-------------|-------------------|-------------------|
| Board exists | Task created on non-existent board → 404 | Error message with "create board" CTA + navigation to boards |
| Column exists | Task created in non-existent column → 404 | Error message with "add column" CTA + navigation to board settings |
| Task ownership | Task not owned by user → 404 | (no UI test — user only sees own tasks) |
| CSRF token | Request without CSRF token → 403 | (handled by browser, no UI test needed) |

**Rules:**
- Generate one API scenario per prerequisite per endpoint (e.g., if story has PATCH and GET, test each)
- Generate UI blocker scenarios in a `## 0. Prerequisite Guards` section — display + navigation per blocker
- Cross-reference existing stories for established blocker patterns (e.g., Story 5 `02_UI_Tests.md` section 0)
- If a prerequisite has two states (unlinked vs expired), generate separate scenarios for each

## 02_UI_Tests.md (5-8 tests)

Same TDD-sequential ordering philosophy. Start with what needs the least code, build up.

Start every generated file with the same `> **Implementation Order**` header.

**Ordering principles:**
0. **Prerequisite guards first** — blocker pages for missing board, missing column. See Prerequisite Guard Checklist above.
1. **Page display first** — render, layout, fields visible. Just needs the component, no logic.
2. **Basic interaction before submission** — focus, toggles, input. Needs handlers, no API.
3. **Form submission with loading state** — submit button, spinner. First test triggering API.
4. **Client-side validation display** — inline errors on blur. Needs validation logic wired.
5. **Server response handling** — success/error messages. Needs API integration.
6. **Navigation and post-action flows** — redirects, links to related pages.

**Navigation coverage rule:** Every link, back-link, and navigation button that appears in a display scenario must have a corresponding scenario that **clicks it and verifies the destination**. Visibility-only assertions ("link is visible") are insufficient — users need confidence the link actually works. Group these click-and-navigate scenarios in the Navigation section. Examples:
- "back to login" link visible → separate scenario: click it → verify URL is `/login`
- "Go to dashboard" button visible → separate scenario: click it → verify dashboard page loads
- Step indicator / breadcrumb clickable → separate scenario: click step → verify navigation and state
These navigation scenarios are pure frontend (no API calls), so their TDD cycle skips `red-frontend-api` / `green-frontend-api` steps.

**Typical section progression:**
```
## 0. Prerequisite Guards (if story has prerequisites)
## 1. Page Display
## 2. User Interaction
## 3. Form Submission
## 4. Validation Feedback
## 5. Server Response Display
## 6. Navigation
```

## 03_Load_Tests.md (3-5 tests)
1. Single request response time (<200ms)
2. Concurrent requests (50 users, <500ms)
3. Queue/batch processing performance
4. Volume tests — large data sets (e.g., board with 1000 tasks, task with max-length fields, bulk operations). Verify the system handles high data volumes without timeout or memory issues.

## 04_Infrastructure_Tests.md (2-3 tests)
1. Database connection failure handling
2. Database recovery after failure
3. External service unavailable handling

## 05_Security_Tests.md (6-10 tests)

Generate **only scenarios relevant to the story's actual attack surface**. Do not include generic OWASP items that don't apply.

**Relevance filtering — skip if:**
- Technology not in stack (NoSQL injection when using JPA, LDAP when no LDAP, XXE when JSON-only)
- Attack has no surface (SSRF when endpoint doesn't fetch external resources, session fixation with JWT cookies)
- Concern is cross-cutting and tested globally (generic 401 for unauthenticated, security headers, CORS, HTTPS — these belong in a global security hardening task, not per-story)
- Timing attacks on non-sensitive operations (registration validation timing)

**Stack-aware checklist — include when relevant to the story:**

| Category | When to include | Example |
|----------|----------------|---------|
| SQL injection | Story has user input that reaches DB queries | Registration, search, filters |
| XSS | Story stores/displays user-provided text | Company name, product names |
| CSRF | Story has state-changing POST/PUT/DELETE with cookie auth | Login, registration, task creation |
| Mass assignment | Story accepts JSON body mapped to DTO | Registration (role), task creation |
| Input length limits | Story has text fields with size constraints | Email, password, URLs |
| Rate limiting | Story has abuse-prone endpoint | Login (brute force), registration (bots) |
| Password hashing | Story handles password storage | Registration, password change |
| IDOR | Story has resource endpoints with IDs | GET/PUT/DELETE /tasks/{id}, /boards/{id} |
| JWT security | Story issues or validates JWT tokens | Login (algorithm confusion, expiration, revocation) |
| Input validation | Story accepts user text input | Task title, description |

**Merge related scenarios**: combine SQL injection across fields into one scenario, combine input length limits into one scenario. Aim for 6-10 focused tests, not 50 generic ones.

## 06_Integration_Tests.md (3-4 tests)
1. External API success flow
2. External API error handling
3. External API timeout handling
4. Token refresh flow

## 07_FullStack_Journey.md (verdict — always present)

Not a test category — a recorded decision about the top-tier full-stack journey (`tdd-rules.md` → "Top-Tier Full-Stack Journey Assessment"). Always written, even for `no-impact`. Start with the verdict on the first line so it is greppable.

**`extend` / `new` format:**
```markdown
# Full-Stack Journey — [Story Name]

**Verdict:** extend            <!-- extend | new | no-impact -->
**Journey:** frontend/acceptance/tests/fullstack/<existing-or-new>.fullstack.spec.ts

## What changes
- [The critical-lifecycle step this story adds/changes, and how the journey weaves it in]
- [Which page Statements (built in the frontend phase) it reuses]

## Journey scenario
\`\`\`gherkin
Given ...
When ...
Then ...
\`\`\`

> Executed as the story-level `fullstack-journey` step after the core frontend scenarios are green
> (workflow.md → "Full-Stack Journey Step"). Mechanics: browser-testing tech binding.
```

**`no-impact` format:**
```markdown
# Full-Stack Journey — [Story Name]

**Verdict:** no-impact
**Reason:** [one line — e.g. backend-only story, no rendered critical-path change]
```

## BDD Format Rules

1. Use Gherkin syntax with domain-specific language (DSL)
2. No technical details in scenario steps
3. Fold repeating sequences into reusable statements
4. End each file with DSL Technical Reference table:

```markdown
## DSL Technical Reference

| DSL Statement | Technical Implementation |
|---------------|-------------------------|
| `an authenticated user` | Valid JWT in Authorization header |
| `the user submits registration` | POST /api/auth/register |
```
