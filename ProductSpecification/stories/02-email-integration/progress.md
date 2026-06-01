# Story 2: Email integration — send email on user registration — Progress

## Spec
- [x] interview
- [x] story
- [S] mockups (backend/integration story — no UI; activation link targets Story 1's existing frontend page)
- [S] api-spec (no new HTTP endpoints — email is an async side-effect of existing registration flow; `/activate` endpoints exist in Story 1)
- [x] test-spec

## Backend Scenarios

### Scenario 1.1: Registering a user delivers an activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (listener→port flow reused unchanged from Story 1; zero usecase/application files change — see ADR test-layering)
- [S] green-usecase (no usecase/application production code — UserRegisteredEventListener already calls EmailNotificationSender.sendActivationToken)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): email — only NoOpEmailNotificationSender (logs, no real send); Mailpit acceptance needs SmtpEmailNotificationSender + ActivationEmailRenderer → red/green-adapter email
  - Check 2 (exceptions): [S] — listener happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — inbound adapter is an event listener, no HTTP response
- [x] red-adapter email
- [x] green-adapter email
- [x] green-acceptance

## Integration Scenarios

### Scenario 6.1: A successful registration delivers exactly one activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (resubmit is pure Modulith infra; zero usecase/application files change — see design)
- [S] green-usecase (no usecase/application production code — ResubmitIncompletePublicationsJob delegates to IncompleteEventPublications)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): [S] — only collaborator is the Modulith-provided IncompleteEventPublications bean (framework infra, not our adapter); resubmit() is simple delegation
  - Check 2 (exceptions): [S] — resubmit happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — resubmit job invoked directly, no HTTP response; simple delegation → body created in green-acceptance
- [x] green-acceptance

### Scenario 7.1: Incomplete publications older than 24 hours are not resubmitted
- [x] red-acceptance
- [x] design (see ADR resubmit-job-placement: relocate job to shared + predicate age cutoff)
- [S] red-usecase (cutoff lives in the infra events adapter; zero usecase/application files change — mirrors 6.1)
- [S] green-usecase (no usecase/application production code — age cutoff is in ResubmitIncompletePublicationsJob)
- [S] red-domain
- [S] green-domain
- [~] refactor (relocate ResubmitIncompletePublicationsJob to shared.infrastructure.events — see ADR)
- [ ] adapters-discovery
- [ ] green-acceptance

## Frontend Scenarios
(none — no UI in this story)

## Security Scenarios

### Scenario 5.1: User-controlled login is escaped in the rendered email
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Load Scenarios
(none — negligible email volume, async send; see tests/03_Load_Tests.md)

## Infrastructure Scenarios

### Scenario 4.1: SMTP unavailable does not fail registration
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.1: Activation email is delivered after SMTP recovers
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance
