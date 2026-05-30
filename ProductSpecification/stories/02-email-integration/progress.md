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
- [~] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Integration Scenarios

### Scenario 6.1: A successful registration delivers exactly one activation email
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 7.1: Incomplete publications older than 24 hours are not resubmitted
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
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
