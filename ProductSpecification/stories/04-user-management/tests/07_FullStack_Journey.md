# Full-Stack Journey — User Management (Admin User Grid & Create User)

**Verdict:** extend
**Journey:** frontend/acceptance/tests/fullstack/account-lifecycle.fullstack.spec.ts

## What changes
- Story 4 builds the real UI the journey currently bypasses: the create-user step is presently a direct admin-API call (`realAuthBackend.createUserAsAdmin`). Replace it with the real Story 4 UI flow — admin opens **Admin Center → Users**, clicks **Register user**, fills the modal, submits — and assert the new user appears in the grid with status **Pending**.
- The activate→login tail of the journey is unchanged (read the activation token from Mailpit, activate via UI, log in via UI).
- Reuses the page Statements built during the frontend phase: the Users-grid page object and the Register-user modal Statements, plus the existing `LoginPageStatements`, `ActivationPageStatements`, and `MailpitStatements`.

## Journey scenario
```gherkin
Given the pre-seeded ACTIVE admin is logged in via the UI against the real backend
When the admin opens Admin Center → Users
And the admin clicks "Register user", fills the modal, and submits
Then the newly created user appears in the grid with status Pending
And the new user receives an activation email in Mailpit
And the new user activates the account via the activation link and logs in via the UI
```

> Executed as the story-level `fullstack-journey` step after the core frontend scenarios (sections
> 1–6 of `02_UI_Tests.md`) are green (workflow.md → "Full-Stack Journey Step"). Mechanics:
> `.claude/tech/playwright/tdd.md` → "Full-Stack Journey"; run recipe in the journey's README.
